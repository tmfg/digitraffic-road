package fi.livi.digitraffic.tie.data.jms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;

@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WeatherJmsMessageListenerTest extends AbstractJmsMessageListenerTest {
    
    private static final Logger log = LoggerFactory.getLogger(WeatherJmsMessageListenerTest.class);

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private WeatherStationService weatherStationService;

    @Autowired
    private SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    LockingService lockingService;

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    private Marshaller jaxbMarshaller;

    @After
    public void restoreData() {
        restoreGeneratedLotjuIdsWithJdbc();
    }

    @Before
    public void initData() throws JAXBException {

        generateMissingLotjuIdsWithJdbc();
        fixDataWithJdbc();

        jaxbMarshaller = JAXBContext.newInstance(Tiesaa.class).createMarshaller();

        log.info("Add available sensors for weather stations");
        if (!TestTransaction.isActive()) {
            TestTransaction.start();
        }

        String merge =
                "MERGE INTO ROAD_STATION_SENSORS TGT\n" +
                "USING (\n" +
                "  SELECT RS.ID ROAD_STATION_ID, S.ID ROAD_STATION_SENSOR_ID\n" +
                "  FROM ROAD_STATION_SENSOR S, ROAD_STATION RS\n" +
                "  WHERE S.OBSOLETE = 0\n" +
                "    AND S.ROAD_STATION_TYPE = 'WEATHER_STATION'\n" +
                "    AND EXISTS (\n" +
                "      SELECT NULL\n" +
                "      FROM ALLOWED_ROAD_STATION_SENSOR ALLOWED\n" +
                "      WHERE ALLOWED.NATURAL_ID = S.NATURAL_ID\n" +
                "        AND ALLOWED.ROAD_STATION_TYPE = S.ROAD_STATION_TYPE\n" +
                "   )\n" +
                "   AND RS.ROAD_STATION_TYPE = 'WEATHER_STATION'\n" +
                "   AND RS.OBSOLETE_DATE IS NULL\n" +
                ") SRC\n" +
                "ON (SRC.ROAD_STATION_ID = TGT.ROAD_STATION_ID AND SRC.ROAD_STATION_SENSOR_ID = TGT.ROAD_STATION_SENSOR_ID)\n" +
                "WHEN NOT MATCHED THEN INSERT (TGT.ROAD_STATION_ID, TGT.ROAD_STATION_SENSOR_ID)\n" +
                "     VALUES (SRC.ROAD_STATION_ID, SRC.ROAD_STATION_SENSOR_ID)";
        jdbcTemplate.execute(merge);

        log.info("Commit changes");
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertFalse(TestTransaction.isActive());
        log.info("Commit done");
    }

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     * @throws JAXBException
     * @throws DatatypeConfigurationException
     */
    @Test
    public void test1PerformanceForReceivedMessages() throws JAXBException, DatatypeConfigurationException {

        Map<Long, WeatherStation> weatherStationsWithLotjuId = weatherStationService.findAllPublicNonObsoleteWeatherStationsMappedByLotjuId();

        JMSMessageListener.JMSDataUpdater<Tiesaa> dataUpdater = (data) -> {
            long start = System.currentTimeMillis();
            if (TestTransaction.isActive()) {
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
            TestTransaction.start();
            Assert.assertTrue("Update failed", sensorDataUpdateService.updateWeatherData(data.stream().map(o -> o.getLeft()).collect(Collectors.toList())));

            TestTransaction.flagForCommit();
            TestTransaction.end();
            long end = System.currentTimeMillis();
            log.info("handleData took " + (end-start) + " ms");
        };

        JMSMessageListener jmsMessageListener = new JMSMessageListener(Tiesaa.class, dataUpdater, true, log);

        DatatypeFactory df = DatatypeFactory.newInstance();
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        XMLGregorianCalendar xgcal = df.newXMLGregorianCalendar(gcal);

        // Generate update-data
        final float minX = 0.0f;
        final float maxX = 100.0f;
        Random rand = new Random();
        float arvo = rand.nextFloat() * (maxX - minX) + minX;
        log.info("Start with arvo " + arvo);

        final List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllNonObsoleteRoadStationSensors(RoadStationType.WEATHER_STATION);

        Iterator<WeatherStation> stationsIter = weatherStationsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        long maxHandleTime = testBurstsLeft * (long)(1000 * 2.5);
        final List<Pair<Tiesaa, String>> data = new ArrayList<>();
        while(testBurstsLeft > 0) {
            testBurstsLeft--;

            long start = System.currentTimeMillis();
            data.clear();
            while (true) {
                if (!stationsIter.hasNext()) {
                    stationsIter = weatherStationsWithLotjuId.values().iterator();
                }
                WeatherStation currentStation = stationsIter.next();

                Tiesaa tiesaa = new Tiesaa();
                data.add(Pair.of(tiesaa, null));

                tiesaa.setAsemaId(currentStation.getLotjuId());
                tiesaa.setAika((XMLGregorianCalendar) xgcal.clone());
                Tiesaa.Anturit tiesaaAnturit = new Tiesaa.Anturit();
                tiesaa.setAnturit(tiesaaAnturit);
                List<Tiesaa.Anturit.Anturi> anturit = tiesaaAnturit.getAnturi();
                for (RoadStationSensor availableSensor : availableSensors) {
                    Tiesaa.Anturit.Anturi anturi = new Tiesaa.Anturit.Anturi();
                    anturit.add(anturi);

                    anturi.setArvo(arvo);
                    // Increase value for every sensor to validate correct updates
                    arvo = arvo + 0.1f;
                    anturi.setLaskennallinenAnturiId(availableSensor.getLotjuId());
                    if (anturit.size() >= 30) {
                        break;
                    }
                }

                xgcal.add(df.newDuration(1000));

                StringWriter xmlSW = new StringWriter();
                jaxbMarshaller.marshal(tiesaa, xmlSW);
                jmsMessageListener.onMessage(createTextMessage(xmlSW.toString(), "Tiesaa " + currentStation.getLotjuId()));

                if (data.size() >= 100 || weatherStationsWithLotjuId.size() <= data.size()) {
                    break;
                }
            }
            long end = System.currentTimeMillis();
            long duration = (end - start);
            log.info("Data generation took " + duration + " ms");
            long startHandle = System.currentTimeMillis();
            jmsMessageListener.drainQueueScheduled();
            long endHandle = System.currentTimeMillis();
            handleDataTotalTime = handleDataTotalTime + (endHandle-startHandle);

            try {
                // send data with 1 s intervall
                long sleep = 1000 - duration;
                if (sleep < 0) {
                    log.error("Data generation took too long");
                } else {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("End with arvo " + arvo);
        log.info("Handle weather data total took " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms " + (handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)"));

        log.info("Check data validy");
        // Assert sensor values are updated to db
        List<Long> tiesaaLotjuIds = data.stream().map(p -> p.getLeft().getAsemaId()).collect(Collectors.toList());
        Map<Long, List<SensorValue>> valuesMap =
                    roadStationSensorService.findNonObsoleteSensorvaluesListMappedByTmsLotjuId(tiesaaLotjuIds, RoadStationType.WEATHER_STATION);

        for (Pair<Tiesaa, String> pair : data) {
            Tiesaa tiesaa = pair.getLeft();
            long asemaLotjuId = tiesaa.getAsemaId();
            List<SensorValue> sensorValues = valuesMap.get(asemaLotjuId);
            List<Tiesaa.Anturit.Anturi> anturit = tiesaa.getAnturit().getAnturi();

            for (Tiesaa.Anturit.Anturi anturi : anturit) {
                Optional<SensorValue> found =
                        sensorValues
                                .stream()
                                .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() != null)
                                .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() == anturi.getLaskennallinenAnturiId())
                                .findFirst();
                Assert.assertTrue(found.isPresent());
                Assert.assertEquals(found.get().getValue(), (double) anturi.getArvo(), 0.05d);
            }
        }
        log.info("Data is valid");
        Assert.assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms", handleDataTotalTime <= maxHandleTime);
    }

    @Test
    public void test2LastUpdated() {
        ZonedDateTime lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.WEATHER_STATION);
        assertTrue(lastUpdated.isAfter(ZonedDateTime.now().minusMinutes(2)));

        List<SensorValueDto> updated = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(lastUpdated.minusSeconds(1), RoadStationType.WEATHER_STATION);
        assertFalse(updated.isEmpty());
    }
}
