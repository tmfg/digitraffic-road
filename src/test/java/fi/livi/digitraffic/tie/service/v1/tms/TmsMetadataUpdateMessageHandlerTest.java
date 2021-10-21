package fi.livi.digitraffic.tie.service.v1.tms;

import static fi.livi.digitraffic.tie.TestUtils.getRandomLotjuId;
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
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.v1.AbstractMetadataUpdateMessageHandlerTest;

public class TmsMetadataUpdateMessageHandlerTest extends AbstractMetadataUpdateMessageHandlerTest {

    public static final long STATION_LOTJU_ID1 = 10L;
    public static final long STATION_LOTJU_ID2 = 20L;
    public static final long SENSOR_LOTJU_ID1 = 1L;
    public static final long SENSOR_CONSTANT_LOTJU_ID = 2L;
    public static final long SENSOR_CONSTANT_VALUE_LOTJU_ID = 3L;

    @Autowired
    private TmsMetadataUpdateMessageHandler tmsMetadataUpdateMessageHandler;

    @AfterEach
    protected void verifyNoMoreInteractionsAndResetMocks() {
        verifyNoMoreInteractions(tmsSensorUpdater);
        verifyNoMoreInteractions(tmsStationUpdater);
        verifyNoMoreInteractions(tmsStationSensorConstantUpdater);

        reset(tmsSensorUpdater);
        reset(tmsStationUpdater);
        reset(tmsStationSensorConstantUpdater);
    }

    @Test // TMS_STATION
    public void tmsStationMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyTmsStationMessageTriggersUpdate(updateType);
        }
    }

    @Test // TMS_SENSOR
    public void tmsSensornMessagesShouldNotTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyMessageWontTriggersUpdate(updateType, EntityType.TMS_SENSOR);
        }
    }

    @Test // TMS_COMPUTATIONAL_SENSOR
    public void tmsComputationalSensorMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyTmsComputationalSensorMessagesTriggersUpdate(updateType);
            verifyNoMoreInteractionsAndResetMocks();
        }
    }

    @Test // TMS_SENSOR_CONSTANT
    public void tmsSensornConstantMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyTmsSensorConstantMessagesTriggersUpdate(updateType);
        }
    }

    @Test // TMS_SENSOR_CONSTANT_VALUE
    public void tmsSensornMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyTmsSensorConstantValueMessagesTriggersUpdate(updateType);
        }
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
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(getRandomLotjuId(), entityType, updateType));
        verifyNoInteractions(tmsStationUpdater);
        verifyNoInteractions(tmsSensorUpdater);
    }

    private void verifyTmsStationMessageTriggersUpdate(final UpdateType updateType) {
        when(tmsStationUpdater.updateTmsStationAndSensors(STATION_LOTJU_ID1, updateType)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(STATION_LOTJU_ID1, EntityType.TMS_STATION, updateType));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(STATION_LOTJU_ID1), eq(updateType));
    }

    private void verifyTmsComputationalSensorMessagesTriggersUpdate(final UpdateType updateType) {
        when(tmsSensorUpdater.updateTmsSensor(SENSOR_LOTJU_ID1, updateType)).thenReturn(true);
        when(tmsStationUpdater.updateTmsStationAndSensors(STATION_LOTJU_ID1, UpdateType.UPDATE)).thenReturn(true);
        when(tmsStationUpdater.updateTmsStationAndSensors(STATION_LOTJU_ID2, UpdateType.UPDATE)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(SENSOR_LOTJU_ID1, EntityType.TMS_COMPUTATIONAL_SENSOR, updateType));
        verify(tmsSensorUpdater, times(1)).updateTmsSensor(eq(SENSOR_LOTJU_ID1), eq(updateType));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(STATION_LOTJU_ID1), eq(UpdateType.UPDATE));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(STATION_LOTJU_ID2), eq(UpdateType.UPDATE));
    }

    private void verifyTmsSensorConstantMessagesTriggersUpdate(final UpdateType updateType) {
        when(tmsStationSensorConstantUpdater.updateTmsStationsSensorConstant(SENSOR_CONSTANT_LOTJU_ID, updateType)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(SENSOR_CONSTANT_LOTJU_ID, EntityType.TMS_SENSOR_CONSTANT, updateType));
        verify(tmsStationSensorConstantUpdater, times(1)).updateTmsStationsSensorConstant(eq(SENSOR_CONSTANT_LOTJU_ID), eq(updateType));
    }

    private void verifyTmsSensorConstantValueMessagesTriggersUpdate(final UpdateType updateType) {
        when(tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantValue(SENSOR_CONSTANT_VALUE_LOTJU_ID, updateType)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(SENSOR_CONSTANT_VALUE_LOTJU_ID, EntityType.TMS_SENSOR_CONSTANT_VALUE, updateType));
        verify(tmsStationSensorConstantUpdater, times(1)).updateTmsStationsSensorConstantValue(eq(SENSOR_CONSTANT_VALUE_LOTJU_ID), eq(updateType));
    }

    private void verifyRoadAddressMessageTriggersUpdate(final UpdateType updateType) {
        when(tmsStationUpdater.updateTmsStationAndSensors(STATION_LOTJU_ID1, updateType)).thenReturn(true);
        when(tmsStationUpdater.updateTmsStationAndSensors(STATION_LOTJU_ID2, updateType)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(getRandomLotjuId(), EntityType.ROAD_ADDRESS, updateType));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(STATION_LOTJU_ID1), eq(UpdateType.UPDATE));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(STATION_LOTJU_ID2), eq(UpdateType.UPDATE));
    }

    private List<TmsMetadataUpdatedMessageDto> createMessage(final long entityLotjuId, final EntityType entityType, final UpdateType updateType) {
        return Collections.singletonList(
            new TmsMetadataUpdatedMessageDto(entityLotjuId, Set.of(STATION_LOTJU_ID1, STATION_LOTJU_ID2), updateType, Instant.now(), entityType));
    }
}
