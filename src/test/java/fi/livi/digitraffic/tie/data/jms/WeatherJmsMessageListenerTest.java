package fi.livi.digitraffic.tie.data.jms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;

@Transactional
public class WeatherJmsMessageListenerTest extends MetadataTest {
    
    private static final Logger log = LoggerFactory.getLogger(WeatherJmsMessageListenerTest.class);

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private WeatherStationService weatherStationService;

    @Autowired
    private SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    DataSource dataSource;

    @Before
    public void setUpTestData() {
        // Currently slow ass hell
        // TOD: Generate data with direct sql

        // Generate test-data: WeatherStations with sensors
        Map<Long, WeatherStation> lamsWithLotjuId = weatherStationService.findAllWeatherStationsMappedByLotjuId();
        Set<Long> usedLotjuIds = new HashSet<>(lamsWithLotjuId.keySet());

        long stationGeneratedLotjuId = -1;
        ArrayList<WeatherStation> stations = new ArrayList<>();
        Map<Long, WeatherStation> lams = weatherStationService.findAllWeatherStationsMappedByByRoadStationNaturalId();

        for (Map.Entry<Long, WeatherStation> longLamStationEntry : lams.entrySet()) {
            WeatherStation lam = longLamStationEntry.getValue();
            if (lam.getLotjuId() == null) {
                while (usedLotjuIds.contains(stationGeneratedLotjuId)) {
                    stationGeneratedLotjuId--;
                }
                usedLotjuIds.add(stationGeneratedLotjuId);
                lam.setLotjuId(stationGeneratedLotjuId);
                lam.getRoadStation().setLotjuId(stationGeneratedLotjuId);
            }
            stations.add(lam);
        }

        log.info("Found " + stations.size() + " Weather Stations");

        Assert.assertTrue(stations.size() > 100);

        long sensorGeneratedLotjuId = -1;
        List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);

        log.info("Found " + availableSensors.size() + " available sensors for Weather Stations");

        for (RoadStationSensor availableSensor : availableSensors) {
            if ( availableSensor.getLotjuId() == null ) {
                availableSensor.setLotjuId(sensorGeneratedLotjuId);
                sensorGeneratedLotjuId--;
            }
        }

        log.info("Add available sensors for Weather Stations");
        for (WeatherStation station : stations) {
            List<RoadStationSensor> currentSensors = station.getRoadStation().getRoadStationSensors();
            Map<Long, RoadStationSensor> currentStationSensorsMapById = currentSensors.stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
            for (RoadStationSensor availableSensor : availableSensors) {
                if (!currentStationSensorsMapById.keySet().contains(availableSensor.getId())) {
                    currentSensors.add(availableSensor);
                }
            }
        }
        log.info("Commit changes");
        assertTrue(TestTransaction.isActive());
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertFalse(TestTransaction.isActive());
        log.info("Commit done");
        // Now we have stations with sensors and lotjuid:s that we can update
    }

    @After
    public void setRollBackLotjuIds() {
        if (!TestTransaction.isActive()) {
            TestTransaction.start();
        }

        Iterator<WeatherStation> stationsIter =
                weatherStationService.findAllWeatherStationsMappedByLotjuId().values().iterator();
        while (stationsIter.hasNext()) {
            WeatherStation lam = stationsIter.next();
            if (lam.getLotjuId() < 0) {
                lam.setLotjuId(null);
                lam.getRoadStation().setLotjuId(null);
            }
        }

        List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);
        for (RoadStationSensor availableSensor : availableSensors) {
            if (availableSensor.getLotjuId() < 0) {
                availableSensor.setLotjuId(null);
            }
        }
        assertTrue(TestTransaction.isActive());
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertFalse(TestTransaction.isActive());
    }

    @Test
    public void testPerformanceForReceivedMessages() throws JAXBException, DatatypeConfigurationException {

        Map<Long, WeatherStation> weatherStationsWithLotjuId = weatherStationService.findAllWeatherStationsMappedByLotjuId();

        JmsMessageListener<Tiesaa> tiesaaJmsMessageListener = new JmsMessageListener<Tiesaa>(Tiesaa.class, "weatherJmsMessageListener") {
            @Override
            protected void handleData(List<Tiesaa> data) {
                long start = System.currentTimeMillis();
                if (TestTransaction.isActive()) {
                    TestTransaction.flagForCommit();
                    TestTransaction.end();
                }
                TestTransaction.start();
                try {
                    sensorDataUpdateService.updateWeatherData(data);
                } catch (SQLException e) {
                    throw new RuntimeException("FAIL" + e);
                }
                TestTransaction.flagForCommit();
                TestTransaction.end();
                long end = System.currentTimeMillis();
                log.info("handleData took " + (end-start) + " ms");
            }
        };

        DatatypeFactory df = DatatypeFactory.newInstance();
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        XMLGregorianCalendar xgcal = df.newXMLGregorianCalendar(gcal);

        // Generate update-data
        float arvo = -10;

        List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);

        Iterator<WeatherStation> stationsIter = weatherStationsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        long maxHandleTime = testBurstsLeft * 1000 + 30000;
        while(testBurstsLeft > 0) {
            testBurstsLeft--;

            long start = System.currentTimeMillis();
            List<Tiesaa> data = new ArrayList<>();
            while (true) {
                if (!stationsIter.hasNext()) {
                    stationsIter = weatherStationsWithLotjuId.values().iterator();
                }
                WeatherStation currentStation = stationsIter.next();

                Tiesaa tiesaa = new Tiesaa();
                data.add(tiesaa);

                tiesaa.setAsemaId(currentStation.getLotjuId());
                tiesaa.setAika(xgcal);
                Tiesaa.Anturit tiesaaAnturit = new Tiesaa.Anturit();
                tiesaa.setAnturit(tiesaaAnturit);
                List<Tiesaa.Anturit.Anturi> anturit = tiesaaAnturit.getAnturi();
                for (RoadStationSensor availableSensor : availableSensors) {
                    Tiesaa.Anturit.Anturi anturi = new Tiesaa.Anturit.Anturi();
                    anturit.add(anturi);

                    anturi.setArvo(arvo);
                    anturi.setLaskennallinenAnturiId(availableSensor.getLotjuId());
                }
                xgcal.add(df.newDuration(1000));
                arvo = arvo + 0.1f;

                if (data.size() >= 100) {
                    break;
                }
            }
            long end = System.currentTimeMillis();
            long duartion = (end - start);
            log.info("Data generation took " + duartion + " ms");
            long startHandle = System.currentTimeMillis();
            tiesaaJmsMessageListener.handleData(data);
            long endHandle = System.currentTimeMillis();
            handleDataTotalTime = handleDataTotalTime + (endHandle-startHandle);

            try {
                // send data with 1 s intervall
                long sleep = 1000 - duartion;
                if (sleep < 0) {
                    log.error("Data generation took too long");
                } else {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Handle data total took " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms " + (handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)"));
        Assert.assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms", handleDataTotalTime <= maxHandleTime);
    }
}
