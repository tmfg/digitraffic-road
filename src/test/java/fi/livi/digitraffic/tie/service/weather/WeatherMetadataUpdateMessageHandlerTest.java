package fi.livi.digitraffic.tie.service.weather;

import static fi.livi.digitraffic.tie.TestUtils.getRandomLotjuId;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.PREPROSESSING;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.SENSOR_MESSAGE;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.VALUE_EQUIVALENCE;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.WEATHER_COMPUTATIONAL_SENSOR_FORMULA;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.WEATHER_SENSOR;
import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType.WEATHER_SENSOR_TYPE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.lotju.AbstractMetadataUpdateMessageHandlerTest;

public class WeatherMetadataUpdateMessageHandlerTest extends AbstractMetadataUpdateMessageHandlerTest {

    public static final long STATION_LOTJU_ID1 = 10L;
    public static final long STATION_LOTJU_ID2 = 20L;
    public static final long SENSOR_LOTJU_ID = 1L;

    @Autowired
    private WeatherMetadataUpdateMessageHandler weatherMetadataUpdateMessageHandler;

    @AfterEach
    protected void verifyNoMoreInteractionsAndResetMocks() {
        verifyNoMoreInteractions(weatherStationSensorUpdater);
        verifyNoMoreInteractions(weatherStationUpdater);

        reset(weatherStationSensorUpdater);
        reset(weatherStationUpdater);
    }

    @Test // WEATHER_STATION
    public void weatherStationMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyWeatherStationMessageTriggersUpdate(updateType);
            verifyNoMoreInteractionsAndResetMocks();
        }
    }

    @Test // WEATHER_COMPUTATIONAL_SENSOR
    public void weatherComputationalSensorMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyWeatherComputationalSensorMessagesTriggersUpdate(updateType);
            verifyNoMoreInteractionsAndResetMocks();
        }
    }

    @Test // WEATHER_STATION_COMPUTATIONAL_SENSOR
    public void tmsSensornMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyWeatherStationComputationalSensorMessagesTriggersUpdate(updateType);
            verifyNoMoreInteractionsAndResetMocks();
        }
    }

    @Test
    public void otherEntitiesMessagesShouldNotTriggerUpdate() {
        Set.of(WEATHER_SENSOR, WEATHER_SENSOR_TYPE, SENSOR_MESSAGE, PREPROSESSING, VALUE_EQUIVALENCE, WEATHER_COMPUTATIONAL_SENSOR_FORMULA)
            .forEach(entityType -> {
                for (final UpdateType updateType : UpdateType.values()) {
                    verifyMessageWontTriggersUpdate(updateType, entityType);
                }
            });
    }

    @Test // ROAD_ADDRESS
    public void roadAddressMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyRoadAddressMessageTriggersUpdate(updateType);
            verifyNoMoreInteractionsAndResetMocks();
        }
    }

    private void verifyMessageWontTriggersUpdate(final UpdateType updateType,
                                                 final EntityType entityType) {
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(getRandomLotjuId(), entityType, updateType));
        verifyNoInteractions(weatherStationUpdater);
        verifyNoInteractions(weatherStationSensorUpdater);
    }

    private void verifyWeatherStationMessageTriggersUpdate(final UpdateType updateType) {
        when(weatherStationUpdater.updateWeatherStationAndSensors(STATION_LOTJU_ID1, updateType)).thenReturn(true);
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(STATION_LOTJU_ID1, EntityType.WEATHER_STATION, updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(STATION_LOTJU_ID1), eq(updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(STATION_LOTJU_ID2), eq(updateType));
    }

    private void verifyWeatherStationComputationalSensorMessagesTriggersUpdate(final UpdateType updateType) {
        when(weatherStationUpdater.updateWeatherStationAndSensors(STATION_LOTJU_ID1, updateType)).thenReturn(true);
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(STATION_LOTJU_ID1, EntityType.WEATHER_STATION, updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(STATION_LOTJU_ID1), eq(updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(STATION_LOTJU_ID2), eq(updateType));
    }

    private void verifyWeatherComputationalSensorMessagesTriggersUpdate(final UpdateType updateType) {
        when(weatherStationSensorUpdater.updateWeatherSensor(SENSOR_LOTJU_ID, updateType)).thenReturn(true);
        when(weatherStationUpdater.updateWeatherStationAndSensors(STATION_LOTJU_ID1, UpdateType.UPDATE)).thenReturn(true);
        when(weatherStationUpdater.updateWeatherStationAndSensors(STATION_LOTJU_ID2, UpdateType.UPDATE)).thenReturn(true);

        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(SENSOR_LOTJU_ID, EntityType.WEATHER_COMPUTATIONAL_SENSOR, updateType));

        verify(weatherStationSensorUpdater, times(1)).updateWeatherSensor(eq(SENSOR_LOTJU_ID), eq(updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(STATION_LOTJU_ID1), eq(UpdateType.UPDATE));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(STATION_LOTJU_ID2), eq(UpdateType.UPDATE));
    }

    private void verifyRoadAddressMessageTriggersUpdate(final UpdateType updateType) {
        when(weatherStationUpdater.updateWeatherStationAndSensors(STATION_LOTJU_ID1, updateType)).thenReturn(true);
        when(weatherStationUpdater.updateWeatherStationAndSensors(STATION_LOTJU_ID2, updateType)).thenReturn(true);
        weatherMetadataUpdateMessageHandler.updateMetadataFromJms(createMessage(getRandomLotjuId(), EntityType.ROAD_ADDRESS, updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(STATION_LOTJU_ID1), eq(UpdateType.UPDATE));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(STATION_LOTJU_ID2), eq(UpdateType.UPDATE));
    }

    private List<WeatherMetadataUpdatedMessageDto> createMessage(final long entityLotjuId, final EntityType entityType, final UpdateType updateType) {
        return Collections.singletonList(
            new WeatherMetadataUpdatedMessageDto(entityLotjuId, Set.of(STATION_LOTJU_ID1, STATION_LOTJU_ID2), updateType, Instant.now(), entityType));
    }
}
