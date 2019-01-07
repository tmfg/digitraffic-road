package fi.livi.digitraffic.tie.data.jms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.persistence.EntityManager;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;

public abstract class AbstractWeatherJmsMessageListenerTest extends AbstractJmsMessageListenerTest {

    protected static final long NON_EXISTING_STATION_LOTJU_ID = -123456789L;

    private static final Logger log = LoggerFactory.getLogger(AbstractJmsMessageListenerTest.class);

    @Autowired
    protected RoadStationSensorService roadStationSensorService;
    @Autowired
    protected WeatherStationService weatherStationService;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected SensorDataUpdateService sensorDataUpdateService;
    @Autowired
    protected EntityManager entityManager;

    protected static TiesaaProtos.TiesaaMittatieto generateTiesaaMittatieto(final Instant measurementTime,
                                                                            final List<RoadStationSensor> availableSensors,
                                                                            final Long currentStationLotjuId) {
        final TiesaaProtos.TiesaaMittatieto.Builder tiesaaMittatietoBuilder = TiesaaProtos.TiesaaMittatieto.newBuilder();

        tiesaaMittatietoBuilder.setAsemaId(currentStationLotjuId);
        tiesaaMittatietoBuilder.setAika(measurementTime.toEpochMilli());

        // Generate update-data
        final float minX = 0.0f;
        final float maxX = 100.0f;
        final Random rand = new Random();
        float arvo = rand.nextFloat() * (maxX - minX) + minX;
        log.info("Start with arvo " + arvo);

        for (final RoadStationSensor availableSensor : availableSensors) {
            final TiesaaProtos.TiesaaMittatieto.Anturi.Builder anturiBuilder = TiesaaProtos.TiesaaMittatieto.Anturi.newBuilder();

            log.info("Asema {} set anturi {} arvo {}",currentStationLotjuId,  availableSensor.getLotjuId(), arvo);
            anturiBuilder.setArvo(NumberConverter.convertDoubleValueToBDecimal(arvo));
            anturiBuilder.setLaskennallinenAnturiId(availableSensor.getLotjuId());
            tiesaaMittatietoBuilder.addAnturi(anturiBuilder.build());

            // Increase value for every sensor to validate correct updates
            arvo = arvo + 0.1f;

            if (tiesaaMittatietoBuilder.getAnturiList().size() >= 30) {
                break;
            }
        }
        log.info("End with arvo={}", arvo);
        return tiesaaMittatietoBuilder.build();
    }

    protected static BytesMessage createBytesMessage(final TiesaaProtos.TiesaaMittatieto tiesaa) throws JMSException, IOException {
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

    protected static void assertData(List<TiesaaProtos.TiesaaMittatieto> data, Map<Long, List<SensorValue>> valuesMap) {
        for (final TiesaaProtos.TiesaaMittatieto tiesaa : data) {
            final long asemaLotjuId = tiesaa.getAsemaId();
            // Don't check non existing station values
            if (WeatherJmsMessageListenerMissingStationTest.NON_EXISTING_STATION_LOTJU_ID == asemaLotjuId) {
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

                Assert.assertEquals(found.get().getValue(), NumberConverter.convertAnturiValueToDouble(anturi.getArvo()), 0.05d);
            }
        }
        log.info("Data is valid");
    }

    protected static JMSMessageListener<TiesaaProtos.TiesaaMittatieto> createTiesaaMittatietoJMSMessageListener(
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

    protected static void assertLastUpdated(final ZonedDateTime lastUpdated) {
        final ZonedDateTime limit = DateHelper.toZonedDateTime(ZonedDateTime.now().minusMinutes(2).toInstant());

        assertTrue(String.format("LastUpdated not fresh %s, should be after %s", lastUpdated, limit), lastUpdated.isAfter(limit));

    }

    protected List<RoadStationSensor> getAvailableRoadStationSensors() {
        return roadStationSensorService
            .findAllNonObsoleteAndAllowedRoadStationSensors(RoadStationType.WEATHER_STATION);
    }

    protected JMSMessageListener.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> createTiesaaMittatietoJMSDataUpdater() {
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
            log.info("handleData tookMs={}", sw.getTime());
            return updated;
        };
    }
}
