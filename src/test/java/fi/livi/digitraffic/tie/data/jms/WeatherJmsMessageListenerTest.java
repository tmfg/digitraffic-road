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
import java.util.Arrays;
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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;

import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.jms.marshaller.WeatherMessageMarshaller;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.NumberConverter;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;

public class WeatherJmsMessageListenerTest extends AbstractJmsMessageListenerTest {

    private static final Logger log = LoggerFactory.getLogger(WeatherJmsMessageListenerTest.class);

    private static final long NON_EXISTING_STATION_LOTJU_ID = -123456789L;

    private static float sensorValueToSet = new Random().nextInt(1000);

    @Autowired
    private WeatherStationService weatherStationService;

    @Autowired
    private SensorDataUpdateService sensorDataUpdateService;

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     * @throws JMSException
     * @throws IOException
     */
    @Test
    public void testPerformanceForReceivedMessages() throws JMSException, IOException {

        final Map<Long, WeatherStation> weatherStationsWithLotjuId = weatherStationService.findAllPublishableWeatherStationsMappedByLotjuId();
        final JMSMessageListener.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> dataUpdater = createTiesaaMittatietoJMSDataUpdater();
        final JMSMessageListener<TiesaaProtos.TiesaaMittatieto> jmsMessageListener = createTiesaaMittatietoJMSMessageListener(dataUpdater);
        final List<RoadStationSensor> publishableSensors = findPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        Iterator<WeatherStation> stationsIter = weatherStationsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        final long maxHandleTime = testBurstsLeft * (long)(1000 * Math.PI);
        final List<TiesaaProtos.TiesaaMittatieto> data = new ArrayList<>();
        Instant time = Instant.now();

        while(testBurstsLeft > 0) {
            testBurstsLeft--;

            final StopWatch sw = StopWatch.createStarted();
            data.clear();

            while (true) {
                if (!stationsIter.hasNext()) {
                    stationsIter = weatherStationsWithLotjuId.values().iterator();
                }
                final WeatherStation currentStation = stationsIter.next();

                List<TiesaaProtos.TiesaaMittatieto> tiesaas =
                    generateTiesaaMittatieto(time, publishableSensors, currentStation.getLotjuId());

                for (TiesaaProtos.TiesaaMittatieto tiesaa : tiesaas) {
                    data.add(tiesaa);
                    jmsMessageListener.onMessage(createBytesMessage(tiesaa));
                }

                time = time.plusMillis(2000);

                if (data.size() >= 100 * tiesaas.size() || weatherStationsWithLotjuId.size() * tiesaas.size() <= data.size()) {
                    break;
                }
            }

            // Create data for non existing station to test that data will be updated even if there is data for non existing station.
            List<TiesaaProtos.TiesaaMittatieto> nonExistingTiesaas = generateTiesaaMittatieto(Instant.now(), publishableSensors, NON_EXISTING_STATION_LOTJU_ID);
            for (TiesaaProtos.TiesaaMittatieto nonExistingTiesaa : nonExistingTiesaas) {
                data.add(nonExistingTiesaa);
                jmsMessageListener.onMessage(createBytesMessage(nonExistingTiesaa));
            }

            sw.stop();
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
        log.info("Handle weather data total tookMs={} and max was maxMs={} result={}",
                 handleDataTotalTime, maxHandleTime, handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)");
        log.info("Check data validy");


        // Assert sensor values are updated to db
        final List<Long> tiesaaLotjuIds = data.stream().map(p -> p.getAsemaId()).distinct().collect(Collectors.toList());

        // Clear because data has been changed by jmsMessageListener directly to db and entity manager doesn't know about it
        entityManager.clear();

        final Map<Long, List<SensorValue>> valuesMap =
                    roadStationSensorService.findNonObsoleteSensorvaluesListMappedByTmsLotjuId(tiesaaLotjuIds, RoadStationType.WEATHER_STATION);

        assertData(data, valuesMap);
        assertDataIsJustUpdated();

        assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms", handleDataTotalTime <= maxHandleTime);
    }

    private static List<TiesaaProtos.TiesaaMittatieto> generateTiesaaMittatieto(Instant measurementTime,
                                                                                final List<RoadStationSensor> availableSensors,
                                                                                final Long currentStationLotjuId) {
        final TiesaaProtos.TiesaaMittatieto.Builder tiesaaMittatietoBuilder1 = TiesaaProtos.TiesaaMittatieto.newBuilder();
        final TiesaaProtos.TiesaaMittatieto.Builder tiesaaMittatietoBuilder2 = TiesaaProtos.TiesaaMittatieto.newBuilder();

        tiesaaMittatietoBuilder1.setAsemaId(currentStationLotjuId);
        tiesaaMittatietoBuilder1.setAika(measurementTime.toEpochMilli());

        tiesaaMittatietoBuilder2.setAsemaId(currentStationLotjuId);
        tiesaaMittatietoBuilder2.setAika(measurementTime.plusMillis(1000).toEpochMilli());

        // Generate update-data
        log.debug("Start with arvo " + sensorValueToSet);
        boolean odd = true;
        for (final RoadStationSensor availableSensor : availableSensors) {
            final TiesaaProtos.TiesaaMittatieto.Anturi.Builder anturiBuilder = TiesaaProtos.TiesaaMittatieto.Anturi.newBuilder();

            anturiBuilder.setArvo(NumberConverter.convertDoubleValueToBDecimal(sensorValueToSet));
            anturiBuilder.setLaskennallinenAnturiId(availableSensor.getLotjuId());
            log.debug("Asema {} set anturi {} arvo {}", currentStationLotjuId,  availableSensor.getLotjuId(), NumberConverter.convertAnturiValueToDouble(anturiBuilder.getArvo()));

            if (odd) {
                tiesaaMittatietoBuilder1.addAnturi(anturiBuilder.build());
            } else {
                tiesaaMittatietoBuilder2.addAnturi(anturiBuilder.build());
            }
            odd = !odd;
            // Increase value for every sensor to validate correct updates
            sensorValueToSet++;

            if (tiesaaMittatietoBuilder1.getAnturiCount() + tiesaaMittatietoBuilder2.getAnturiCount() >= 30) {
                break;
            }
        }
        log.debug("End with arvo={}", sensorValueToSet - 1);
        return Arrays.asList(tiesaaMittatietoBuilder1.build(), tiesaaMittatietoBuilder2.build());
    }

    private static BytesMessage createBytesMessage(final TiesaaProtos.TiesaaMittatieto tiesaa) throws JMSException, IOException {
        final ByteArrayOutputStream bous = new ByteArrayOutputStream(0);
        tiesaa.writeDelimitedTo(bous);
        final byte[] tiesaaBytes = bous.toByteArray();

        final BytesMessage bytesMessage = mock(BytesMessage.class);

        when(bytesMessage.getBodyLength()).thenReturn((long) tiesaaBytes.length);
        when(bytesMessage.readBytes(any(byte[].class))).then(invocation -> {
            final byte[] bytes = (byte[]) invocation.getArguments()[0];
            System.arraycopy(tiesaaBytes, 0, bytes, 0, tiesaaBytes.length);
            return tiesaaBytes.length;
        });

        return bytesMessage;
    }

    private static void assertData(List<TiesaaProtos.TiesaaMittatieto> data, Map<Long, List<SensorValue>> valuesMap) {
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
                        .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() != null)
                        .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() == anturi.getLaskennallinenAnturiId())
                        .findFirst();
                assertTrue(found.isPresent());

                log.info("asema:{} data vs db: anturi: {} vs {}, data: {} vs {}",
                    tiesaa.getAsemaId(),
                    anturi.getLaskennallinenAnturiId(), found.get().getRoadStationSensor().getLotjuId(),
                    NumberConverter.convertAnturiValueToDouble(anturi.getArvo()), found.get().getValue());

                Assert.assertEquals(NumberConverter.convertAnturiValueToDouble(anturi.getArvo()), found.get().getValue(), 0.05d);
            }
        }
        log.info("Data is valid");
    }

    private static JMSMessageListener<TiesaaProtos.TiesaaMittatieto> createTiesaaMittatietoJMSMessageListener(
        JMSMessageListener.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> dataUpdater) {
        return (JMSMessageListener<TiesaaProtos.TiesaaMittatieto>) new JMSMessageListener(new WeatherMessageMarshaller(),
            dataUpdater, true, log);
    }

    public void assertDataIsJustUpdated() {
        final ZonedDateTime lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.WEATHER_STATION);
        assertLastUpdated(lastUpdated);

        final List<SensorValueDto> updated = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(lastUpdated.minusSeconds(1), RoadStationType.WEATHER_STATION);
        assertFalse(updated.isEmpty());
    }

    private static void assertLastUpdated(final ZonedDateTime lastUpdated) {
        final ZonedDateTime limit = DateHelper.toZonedDateTime(ZonedDateTime.now().minusMinutes(2).toInstant());

        assertTrue(String.format("LastUpdated not fresh %s, should be after %s", lastUpdated, limit), lastUpdated.isAfter(limit));

    }

    private JMSMessageListener.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> createTiesaaMittatietoJMSDataUpdater() {
        return (data) -> {
            final StopWatch sw = StopWatch.createStarted();

            if (TestTransaction.isActive()) {
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
            TestTransaction.start();

            final int updated = sensorDataUpdateService.updateWeatherData(data);
            TestTransaction.flagForCommit();
            TestTransaction.end();
            TestTransaction.start();

            log.info("handleData tookMs={}", sw.getTime());
            return updated;
        };
    }
}
