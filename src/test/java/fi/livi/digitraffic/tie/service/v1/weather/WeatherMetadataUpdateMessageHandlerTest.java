package fi.livi.digitraffic.tie.service.v1.weather;

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
import fi.livi.digitraffic.tie.service.v1.AbstractMetadataUpdateMessageHandlerTest;

public class WeatherMetadataUpdateMessageHandlerTest extends AbstractMetadataUpdateMessageHandlerTest {

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
        }
    }

    @Test // WEATHER_COMPUTATIONAL_SENSOR_FORMULA
    public void otherEntitiesMessagesShouldTriggerUpdate() {
        Set.of(WEATHER_SENSOR, WEATHER_SENSOR_TYPE, SENSOR_MESSAGE, PREPROSESSING, VALUE_EQUIVALENCE, WEATHER_COMPUTATIONAL_SENSOR_FORMULA)
            .forEach(entityType -> {
                for (final UpdateType updateType : UpdateType.values()) {
                    verifyMessageWontTriggersUpdate(updateType, entityType);
                }
            });
    }

    @Test // ROAD_ADDRESS
    public void roadAddressMessagesShouldTriggerUpdate() {
        for (UpdateType updateType : UpdateType.values()) {
            verifyRoadAddressMessageTriggersUpdate(updateType);
            verifyNoMoreInteractionsAndResetMocks();
        }
    }

    private void verifyMessageWontTriggersUpdate(final UpdateType updateType,
                                                 final EntityType entityType) {
        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(entityType, updateType));
        verifyNoInteractions(weatherStationUpdater);
        verifyNoInteractions(weatherStationSensorUpdater);
    }

    private void verifyWeatherStationMessageTriggersUpdate(final UpdateType updateType) {
        when(weatherStationUpdater.updateWeatherStationAndSensors(1L, updateType)).thenReturn(true);
        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(EntityType.WEATHER_STATION, updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(1L), eq(updateType));
    }

    private void verifyWeatherStationComputationalSensorMessagesTriggersUpdate(final UpdateType updateType) {
        when(weatherStationUpdater.updateWeatherStationAndSensors(1L, updateType)).thenReturn(true);
        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(EntityType.WEATHER_STATION, updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(1L), eq(updateType));
    }

    private void verifyWeatherComputationalSensorMessagesTriggersUpdate(final UpdateType updateType) {
        when(weatherStationSensorUpdater.updateWeatherSensor(1L, updateType)).thenReturn(true);
        when(weatherStationUpdater.updateWeatherStationAndSensors(10L, UpdateType.UPDATE)).thenReturn(true);
        when(weatherStationUpdater.updateWeatherStationAndSensors(20L, UpdateType.UPDATE)).thenReturn(true);

        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(EntityType.WEATHER_COMPUTATIONAL_SENSOR, updateType));

        verify(weatherStationSensorUpdater, times(1)).updateWeatherSensor(eq(1L), eq(updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(10L), eq(UpdateType.UPDATE));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(20L), eq(UpdateType.UPDATE));
    }

    private void verifyRoadAddressMessageTriggersUpdate(final UpdateType updateType) {
        when(weatherStationUpdater.updateWeatherStationAndSensors(10L, updateType)).thenReturn(true);
        when(weatherStationUpdater.updateWeatherStationAndSensors(20L, updateType)).thenReturn(true);
        weatherMetadataUpdateMessageHandler.updateWeatherMetadataFromJms(createMessage(EntityType.ROAD_ADDRESS, updateType));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(10L), eq(UpdateType.UPDATE));
        verify(weatherStationUpdater, times(1)).updateWeatherStationAndSensors(eq(20L), eq(UpdateType.UPDATE));
    }

    private List<WeatherMetadataUpdatedMessageDto> createMessage(final EntityType entityType, final UpdateType updateType) {
        return Collections.singletonList(
            new WeatherMetadataUpdatedMessageDto(1L, Set.of(10L, 20L), updateType, Instant.now(), entityType));
    }
}
