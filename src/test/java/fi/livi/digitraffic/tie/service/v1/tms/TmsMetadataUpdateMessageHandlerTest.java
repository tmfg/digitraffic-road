package fi.livi.digitraffic.tie.service.v1.tms;

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
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType;

public class TmsMetadataUpdateMessageHandlerTest extends AbstractDaemonTest {

    @Autowired
    private TmsMetadataUpdateMessageHandler tmsMetadataUpdateMessageHandler;

    @MockBean
    private TmsStationUpdater tmsStationUpdater;

    @MockBean
    private TmsSensorUpdater tmsSensorUpdater;

    @MockBean
    private TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater;

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
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(entityType, updateType));
        verifyNoInteractions(tmsStationUpdater);
        verifyNoInteractions(tmsSensorUpdater);
    }

    private void verifyTmsStationMessageTriggersUpdate(final UpdateType updateType) {
        when(tmsStationUpdater.updateTmsStationAndSensors(1L, updateType)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(EntityType.TMS_STATION, updateType));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(1L), eq(updateType));
    }

    private void verifyTmsComputationalSensorMessagesTriggersUpdate(final UpdateType updateType) {
        when(tmsSensorUpdater.updateTmsSensor(1L, updateType)).thenReturn(true);
        when(tmsStationUpdater.updateTmsStationAndSensors(10L, UpdateType.UPDATE)).thenReturn(true);
        when(tmsStationUpdater.updateTmsStationAndSensors(20L, UpdateType.UPDATE)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(EntityType.TMS_COMPUTATIONAL_SENSOR, updateType));
        verify(tmsSensorUpdater, times(1)).updateTmsSensor(eq(1L), eq(updateType));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(10L), eq(UpdateType.UPDATE));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(20L), eq(UpdateType.UPDATE));
    }

    private void verifyTmsSensorConstantMessagesTriggersUpdate(final UpdateType updateType) {
        when(tmsStationSensorConstantUpdater.updateTmsStationsSensorConstant(1L, updateType)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(EntityType.TMS_SENSOR_CONSTANT, updateType));
        verify(tmsStationSensorConstantUpdater, times(1)).updateTmsStationsSensorConstant(eq(1L), eq(updateType));
    }

    private void verifyTmsSensorConstantValueMessagesTriggersUpdate(final UpdateType updateType) {
        when(tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantValue(1L, updateType)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(EntityType.TMS_SENSOR_CONSTANT_VALUE, updateType));
        verify(tmsStationSensorConstantUpdater, times(1)).updateTmsStationsSensorConstantValue(eq(1L), eq(updateType));
    }

    private void verifyRoadAddressMessageTriggersUpdate(final UpdateType updateType) {
        when(tmsStationUpdater.updateTmsStationAndSensors(10L, updateType)).thenReturn(true);
        when(tmsStationUpdater.updateTmsStationAndSensors(20L, updateType)).thenReturn(true);
        tmsMetadataUpdateMessageHandler.updateTmsMetadataFromJms(createMessage(EntityType.ROAD_ADDRESS, updateType));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(10L), eq(UpdateType.UPDATE));
        verify(tmsStationUpdater, times(1)).updateTmsStationAndSensors(eq(20L), eq(UpdateType.UPDATE));
    }

    private List<TmsMetadataUpdatedMessageDto> createMessage(final EntityType entityType, final UpdateType updateType) {
        return Collections.singletonList(
            new TmsMetadataUpdatedMessageDto(1L, Set.of(10L, 20L), updateType, Instant.now(), entityType));
    }
}
