package fi.livi.digitraffic.tie.service.jms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.activemq.artemis.jms.client.ActiveMQBytesMessage;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.common.util.ThreadUtil;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.NumberConverter;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValue;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import fi.livi.digitraffic.tie.service.jms.marshaller.WeatherDataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.weather.WeatherStationService;
import jakarta.jms.JMSException;

public class WeatherJMSMessageHandlerTest extends AbstractJMSMessageHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(WeatherJMSMessageHandlerTest.class);

    private static final long NON_EXISTING_STATION_LOTJU_ID = -123456789L;

    private static float sensorValueToSet = new Random().nextInt(1000);

    @Autowired
    private WeatherStationService weatherStationService;

    @BeforeEach
    public void initData() {
        TestUtils.truncateWeatherData(entityManager);
        entityManager.flush();
        TestUtils.generateDummyWeatherStations(50).forEach(s -> entityManager.persist(s));
        entityManager.flush();
        TestUtils.commitAndEndTransactionAndStartNew();
        sensorDataUpdateService.updateStationsAndSensorsMetadata();
    }

    @AfterEach
    protected void cleanDb() {
        TestUtils.commitAndEndTransactionAndStartNew();
        TestUtils.truncateWeatherData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     */
    @Test
    public void testPerformanceForReceivedMessages() throws JMSException, IOException {
        final Map<Long, WeatherStation> weatherStationsWithLotjuId =
                weatherStationService.findAllPublishableWeatherStationsMappedByLotjuId();
        final JMSMessageHandler.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> dataUpdater =
                createTiesaaMittatietoJMSDataUpdater();
        final JMSMessageHandler<TiesaaProtos.TiesaaMittatieto> jmsMessageListener =
                createTiesaaMittatietoJMSMessageListener(dataUpdater);
        final List<RoadStationSensor> publishableSensors =
                findPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        // subset of 100 stations
        final List<Long> lotjuIds = new ArrayList<>(weatherStationsWithLotjuId.keySet())
                .subList(0, Math.min(100, weatherStationsWithLotjuId.size()));
        weatherStationsWithLotjuId.keySet().retainAll(lotjuIds);
        // Generate previous sensor values so that sensor update is mostly only updating old values
        generateSensorValuesFor(weatherStationsWithLotjuId.values(),
                publishableSensors.subList(0, publishableSensors.size() - 1),
                jmsMessageListener);

        Iterator<WeatherStation> stationsIter = weatherStationsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        // This just an value got by running tests. Purpose is only to notice if there is big change in performance.
        final long maxHandleTime = testBurstsLeft * 3000L;
        final List<TiesaaProtos.TiesaaMittatieto> data = new ArrayList<>();
        Instant time = Instant.now();

        while (testBurstsLeft > 0) {
            testBurstsLeft--;

            final StopWatch sw = StopWatch.createStarted();
            data.clear();

            while (true) {
                if (!stationsIter.hasNext()) {
                    stationsIter = weatherStationsWithLotjuId.values().iterator();
                }
                final WeatherStation currentStation = stationsIter.next();

                final List<TiesaaProtos.TiesaaMittatieto> tiesaas =
                        generateTiesaaMittatieto(time, publishableSensors, currentStation.getLotjuId(), 2);

                for (final TiesaaProtos.TiesaaMittatieto tiesaa : tiesaas) {
                    data.add(tiesaa);
                    final ActiveMQBytesMessage message = createBytesMessage(tiesaa);
                    jmsMessageListener.onMessage(message);
                }

                time = time.plusMillis(2000);

                if (data.size() >= 100 * tiesaas.size() ||
                        weatherStationsWithLotjuId.size() * tiesaas.size() <= data.size()) {
                    break;
                }
            }

            // Create data for non existing station to test that data will be updated even if there is data for non existing station.
            final List<TiesaaProtos.TiesaaMittatieto> nonExistingTiesaas =
                    generateTiesaaMittatieto(Instant.now(), publishableSensors,
                            NON_EXISTING_STATION_LOTJU_ID, 2);
            for (final TiesaaProtos.TiesaaMittatieto nonExistingTiesaa : nonExistingTiesaas) {
                data.add(nonExistingTiesaa);
                final ActiveMQBytesMessage bm = createBytesMessage(nonExistingTiesaa);
                jmsMessageListener.onMessage(bm);
            }

            sw.stop();
            log.info("Data generation tookMs={}", sw.getTime());
            final StopWatch swHandle = StopWatch.createStarted();
            jmsMessageListener.drainQueueScheduled();
            handleDataTotalTime += swHandle.getTime();

            // send data with 1 s intervall
            final long sleep = 1000 - sw.getTime();

            if (sleep < 0) {
                log.error("Data generation took too long");
            } else {
                ThreadUtil.delayMs(sleep);
            }

        }
        log.info("Handle weather data total tookMs={} and max was maxMs={} result={}",
                handleDataTotalTime, maxHandleTime, handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)");
        log.info("Check data validy");

        flushSensorBuffer(false);

        // Assert sensor values are updated to db
        final List<Long> tiesaaLotjuIds =
                data.stream().map(TiesaaProtos.TiesaaMittatieto::getAsemaId).distinct().collect(Collectors.toList());

        // Clear because data has been changed by jmsMessageListener directly to db and entity manager doesn't know about it
        entityManager.clear();

        final Map<Long, List<SensorValue>> valuesMap =
                roadStationSensorService.findNonObsoleteSensorvaluesListMappedByTmsLotjuId(tiesaaLotjuIds,
                        RoadStationType.WEATHER_STATION);

        assertData(data, valuesMap);
        assertDataIsJustUpdated();

        assertTrue(handleDataTotalTime <= maxHandleTime,
                "Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms");
    }

    private void generateSensorValuesFor(final Collection<WeatherStation> stations,
                                         final List<RoadStationSensor> publishableSensors,
                                         final JMSMessageHandler<TiesaaProtos.TiesaaMittatieto> jmsMessageHandler)
            throws IOException, JMSException {
        for (final WeatherStation station : stations) {
            final ActiveMQBytesMessage bm =
                    createBytesMessage(
                            generateTiesaaMittatieto(Instant.now(), publishableSensors, station.getLotjuId(), 1).get(
                                    0));
            jmsMessageHandler.onMessage(bm);
        }
        jmsMessageHandler.drainQueueScheduled();
    }

    private static List<TiesaaProtos.TiesaaMittatieto> generateTiesaaMittatieto(final Instant measurementTime,
                                                                                final List<RoadStationSensor> availableSensors,
                                                                                final Long currentStationLotjuId,
                                                                                final int splitToMessages) {
        final ArrayList<TiesaaProtos.TiesaaMittatieto.Builder> builders = new ArrayList<>();
        for (int i = 0; i < splitToMessages; i++) {
            final TiesaaProtos.TiesaaMittatieto.Builder builder = TiesaaProtos.TiesaaMittatieto.newBuilder();
            builders.add(builder);
            builder.setAsemaId(currentStationLotjuId);
            builder.setAika(measurementTime.plusMillis(i * 1000L).toEpochMilli());
        }

        // Generate update-data
        log.debug("Start with arvo " + sensorValueToSet);

        Iterator<TiesaaProtos.TiesaaMittatieto.Builder> iter = builders.iterator();
        for (final RoadStationSensor availableSensor : availableSensors) {
            final TiesaaProtos.TiesaaMittatieto.Anturi.Builder anturiBuilder =
                    TiesaaProtos.TiesaaMittatieto.Anturi.newBuilder();

            anturiBuilder.setArvo(NumberConverter.convertDoubleValueToBDecimal(sensorValueToSet));
            anturiBuilder.setLaskennallinenAnturiId(availableSensor.getLotjuId());
            log.debug("Asema {} set anturi {} arvo {}", currentStationLotjuId, availableSensor.getLotjuId(),
                    NumberConverter.convertAnturiValueToDouble(anturiBuilder.getArvo()));

            if (!iter.hasNext()) {
                iter = builders.iterator();
            }
            final TiesaaProtos.TiesaaMittatieto.Builder builder = iter.next();

            builder.addAnturi(anturiBuilder.build());
            // Increase value for every sensor to validate correct updates
            sensorValueToSet++;

            if (builders.stream().mapToInt(TiesaaProtos.TiesaaMittatieto.Builder::getAnturiCount).sum() >= 30) {
                break;
            }
        }
        log.debug("End with arvo={}", sensorValueToSet - 1);
        return builders.stream().map(TiesaaProtos.TiesaaMittatieto.Builder::build).collect(Collectors.toList());
    }

    private static void assertData(final List<TiesaaProtos.TiesaaMittatieto> data,
                                   final Map<Long, List<SensorValue>> valuesMap) {
        for (final TiesaaProtos.TiesaaMittatieto tiesaa : data) {
            final long asemaLotjuId = tiesaa.getAsemaId();
            // Don't check non existing station values
            if (NON_EXISTING_STATION_LOTJU_ID == asemaLotjuId) {
                continue;
            }
            final List<SensorValue> sensorValues = valuesMap.get(asemaLotjuId);
            final List<TiesaaProtos.TiesaaMittatieto.Anturi> anturit = tiesaa.getAnturiList();

            for (final TiesaaProtos.TiesaaMittatieto.Anturi anturi : anturit) {
                final Optional<SensorValue> found =
                        sensorValues
                                .stream()
                                .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() ==
                                        anturi.getLaskennallinenAnturiId())
                                .findFirst();
                assertTrue(found.isPresent());

                log.debug("asema: {} data vs db: anturi: {} vs {}, data: {} vs {}",
                        tiesaa.getAsemaId(),
                        anturi.getLaskennallinenAnturiId(), found.get().getRoadStationSensor().getLotjuId(),
                        NumberConverter.convertAnturiValueToDouble(anturi.getArvo()), found.get().getValue());

                assertEquals(NumberConverter.convertAnturiValueToDouble(anturi.getArvo()), found.get().getValue(),
                        0.05d);
            }
        }
        log.info("Data is valid");
    }

    private JMSMessageHandler<TiesaaProtos.TiesaaMittatieto> createTiesaaMittatietoJMSMessageListener(
            final JMSMessageHandler.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> dataUpdater) {
        return new JMSMessageHandler<>(JMSMessageHandler.JMSMessageType.WEATHER_DATA, dataUpdater,
                new WeatherDataJMSMessageMarshaller(), lockingService.getInstanceId());
    }

    public void assertDataIsJustUpdated() {
        final ZonedDateTime lastUpdated =
                roadStationSensorService.getLatestSensorValueUpdatedTime(RoadStationType.WEATHER_STATION);
        assertLastUpdated(lastUpdated);

        final List<SensorValueDto> updated =
                roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
                        lastUpdated.minusSeconds(1),
                        RoadStationType.WEATHER_STATION);
        assertFalse(updated.isEmpty());
    }

    private static void assertLastUpdated(final ZonedDateTime lastUpdated) {
        final ZonedDateTime limit = DateHelper.toZonedDateTimeAtUtc(ZonedDateTime.now().minusMinutes(2).toInstant());

        assertTrue(lastUpdated.isAfter(limit),
                String.format("LastUpdated not fresh %s, should be after %s", lastUpdated, limit));
    }

    private JMSMessageHandler.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> createTiesaaMittatietoJMSDataUpdater() {
        return (data) -> {
            final StopWatch sw = StopWatch.createStarted();

            final int updated = sensorDataUpdateService.updateWeatherValueBuffer(data);

            log.info("handleData tookMs={}", sw.getTime());
            return updated;
        };
    }
}
