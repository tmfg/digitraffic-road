package fi.livi.digitraffic.tie.data.jms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;

import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.jms.marshaller.WeatherMessageMarshaller;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.helper.NumberConverter;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;

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
    protected JdbcTemplate jdbcTemplate;

    /**
    @Before
    public void initData() {
        log.info("Add available sensors for weather stations");
        if (!TestTransaction.isActive()) {
            TestTransaction.start();
        }

        final String merge =
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
    */

    public static BytesMessage createBytesMessage(final TiesaaProtos.TiesaaMittatieto tiesaa) throws JMSException, IOException {
        final ByteArrayOutputStream bous = new ByteArrayOutputStream(0);
        tiesaa.writeDelimitedTo(bous);
        final byte[] tiesaaBytes = bous.toByteArray();

        final BytesMessage bytesMessage = mock(BytesMessage.class);

        when(bytesMessage.getBodyLength()).thenReturn((long)tiesaaBytes.length);
        when(bytesMessage.readBytes(any(byte[].class))).then(invocation -> {
            final byte[] bytes = (byte[]) invocation.getArguments()[0];
            System.arraycopy(tiesaaBytes, 0, bytes, 0, tiesaaBytes.length);
            return tiesaaBytes.length;
        });

        return bytesMessage;
    }

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     * @throws JMSException
     * @throws IOException
     */
    @Test
    public void test1PerformanceForReceivedMessages() throws JMSException, IOException {
        final Map<Long, WeatherStation> weatherStationsWithLotjuId = weatherStationService
            .findAllPublishableWeatherStationsMappedByLotjuId();

        final JMSMessageListener.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> dataUpdater = (data) -> {
            final StopWatch sw = StopWatch.createStarted();

            if (TestTransaction.isActive()) {
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
            TestTransaction.start();

            final int updated = sensorDataUpdateService.updateWeatherData(data);
            TestTransaction.flagForCommit();
            TestTransaction.end();
            log.info("handleData tookMs={}", sw.getTime());
            return updated;
        };

        final JMSMessageListener<TiesaaProtos.TiesaaMittatieto> jmsMessageListener = new JMSMessageListener(new WeatherMessageMarshaller(),
            dataUpdater, true, log);

        Instant time = Instant.now();

        // Generate update-data
        final float minX = 0.0f;
        final float maxX = 100.0f;
        final Random rand = new Random();
        float arvo = rand.nextFloat() * (maxX - minX) + minX;
        log.info("Start with arvo " + arvo);

        final List<RoadStationSensor> availableSensors = roadStationSensorService
            .findAllNonObsoleteAndAllowedRoadStationSensors(RoadStationType.WEATHER_STATION);

        Iterator<WeatherStation> stationsIter = weatherStationsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        final long maxHandleTime = testBurstsLeft * (long)(1000 * 2.5);
        final List<TiesaaProtos.TiesaaMittatieto> data = new ArrayList<>();

        while(testBurstsLeft > 0) {
            testBurstsLeft--;

            final StopWatch sw = StopWatch.createStarted();
            data.clear();

            while (true) {
                if (!stationsIter.hasNext()) {
                    stationsIter = weatherStationsWithLotjuId.values().iterator();
                }
                final WeatherStation currentStation = stationsIter.next();

                final TiesaaProtos.TiesaaMittatieto.Builder tiesaaMittatietoBuilder = TiesaaProtos.TiesaaMittatieto.newBuilder();

                tiesaaMittatietoBuilder.setAsemaId(currentStation.getLotjuId());
                tiesaaMittatietoBuilder.setAika(time.toEpochMilli());

                for (final RoadStationSensor availableSensor : availableSensors) {
                    final TiesaaProtos.TiesaaMittatieto.Anturi.Builder anturiBuilder = TiesaaProtos.TiesaaMittatieto.Anturi.newBuilder();

                    anturiBuilder.setArvo(NumberConverter.convertDoubleValueToBDecimal(arvo));

                    // Increase value for every sensor to validate correct updates
                    arvo = arvo + 0.1f;
                    anturiBuilder.setLaskennallinenAnturiId(availableSensor.getLotjuId());

                    tiesaaMittatietoBuilder.addAnturi(anturiBuilder.build());

                    if (tiesaaMittatietoBuilder.getAnturiList().size() >= 30) {
                        break;
                    }
                }

                TiesaaProtos.TiesaaMittatieto tiesaa = tiesaaMittatietoBuilder.build();

                data.add(tiesaa);

                time = time.plusMillis(1000);

                jmsMessageListener.onMessage(createBytesMessage(tiesaa));

                if (data.size() >= 100 || weatherStationsWithLotjuId.size() <= data.size()) {
                    break;
                }
            }

            log.info("Data generation tookMs={}", sw.getTime());
            StopWatch swHandle = StopWatch.createStarted();
            jmsMessageListener.drainQueueScheduled();
            handleDataTotalTime += swHandle.getTime();

            try {
                // send data with 1 s intervall
                long sleep = 1000 - sw.getTime();

                if (sleep < 0) {
                    log.error("Data generation took too long");
                } else {
                    Thread.sleep(sleep);
                }
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("End with arvo={}", arvo);
        log.info("Handle weather data total tookMs={} and max was maxMs={} result={}",
                 handleDataTotalTime, maxHandleTime, handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)");
        log.info("Check data validy");
        // Assert sensor values are updated to db
        final List<Long> tiesaaLotjuIds = data.stream().map(p -> p.getAsemaId()).collect(Collectors.toList());
        final Map<Long, List<SensorValue>> valuesMap =
                    roadStationSensorService.findNonObsoleteSensorvaluesListMappedByTmsLotjuId(tiesaaLotjuIds, RoadStationType.WEATHER_STATION);

        for (final TiesaaProtos.TiesaaMittatieto tiesaa : data) {
            final long asemaLotjuId = tiesaa.getAsemaId();
            final List<SensorValue> sensorValues = valuesMap.get(asemaLotjuId);
            final List<TiesaaProtos.TiesaaMittatieto.Anturi> anturit = tiesaa.getAnturiList();

            for (final TiesaaProtos.TiesaaMittatieto.Anturi anturi : anturit) {
                final Optional<SensorValue> found =
                        sensorValues
                                .stream()
                                .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() != null)
                                .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() == anturi.getLaskennallinenAnturiId())
                                .findFirst();
                assertTrue(found.isPresent());

                Assert.assertEquals(found.get().getValue(), NumberConverter.convertAnturiValueToDouble(anturi.getArvo()), 0.05d);
            }
        }
        log.info("Data is valid");
        assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms", handleDataTotalTime <= maxHandleTime);
    }

    @Test
    public void test2LastUpdated() {
        final ZonedDateTime lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.WEATHER_STATION);
        assertLastUpdated(lastUpdated);

        final List<SensorValueDto> updated = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(lastUpdated.minusSeconds(1), RoadStationType.WEATHER_STATION);
        assertFalse(updated.isEmpty());
    }

    private void assertLastUpdated(final ZonedDateTime lastUpdated) {
        final ZonedDateTime limit = ZonedDateTime.now().minusMinutes(2);

        assertTrue(String.format("LastUpdated not fresh %s, should be after %s", lastUpdated, limit), lastUpdated.isAfter(limit));

    }
}
