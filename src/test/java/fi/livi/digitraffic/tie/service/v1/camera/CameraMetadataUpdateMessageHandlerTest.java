package fi.livi.digitraffic.tie.service.v1.camera;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutLocalStack;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;

public class CameraMetadataUpdateMessageHandlerTest extends AbstractDaemonTestWithoutLocalStack {

    @Autowired
    private CameraMetadataUpdateMessageHandler cameraMetadataUpdateMessageHandler;

    @MockBean
    private CameraStationUpdater cameraStationUpdater;

    @AfterEach
    protected void verifyNoMoreInteractionsAndResetMocks() {
        verifyNoMoreInteractions(cameraStationUpdater);
        reset(cameraStationUpdater);
    }

    @Test
    public void cameraMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyCameraMessageTriggersUpdate(updateType);
        }
    }

    @Test
    public void presetMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyPresetaMessageTriggersUpdate(updateType);
        }
    }

    @Test
    public void roadAddressMessagesShouldTriggerUpdate() {
        for (final UpdateType updateType : UpdateType.values()) {
            verifyRoadAddressMessageTriggersUpdate(updateType);
            verifyNoMoreInteractionsAndResetMocks();
        }
    }

    @Test
    public void otherMessagesShouldNotTriggerUpdate() {
        final ArrayList<EntityType> entityTypes = new ArrayList<>(Arrays.asList(EntityType.values()));
        entityTypes.removeAll(Arrays.asList(EntityType.CAMERA, EntityType.PRESET, EntityType.ROAD_ADDRESS));
        for (final EntityType entityType : entityTypes) {
            for (final UpdateType updateType : UpdateType.values()) {
                verifyMessageWontTriggersUpdate(updateType, entityType);
            }
        }
    }

    private void verifyMessageWontTriggersUpdate(final UpdateType updateType,
                                                 final EntityType entityType) {
        cameraMetadataUpdateMessageHandler.updateCameraMetadataFromJms(createMessage(entityType, updateType));
        verifyNoInteractions(cameraStationUpdater);
    }

    private void verifyCameraMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraStation(1L, updateType)).thenReturn(true);
        cameraMetadataUpdateMessageHandler.updateCameraMetadataFromJms(createMessage(EntityType.CAMERA, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraStation(eq(1L), eq(updateType));
    }

    private void verifyPresetaMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraPreset(1L, updateType)).thenReturn(true);
        cameraMetadataUpdateMessageHandler.updateCameraMetadataFromJms(createMessage(EntityType.PRESET, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraPreset(eq(1L), eq(updateType));
    }

    private void verifyRoadAddressMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraStation(10L, UpdateType.UPDATE)).thenReturn(true);
        when(cameraStationUpdater.updateCameraStation(20L, UpdateType.UPDATE)).thenReturn(true);
        cameraMetadataUpdateMessageHandler.updateCameraMetadataFromJms(createMessage(EntityType.ROAD_ADDRESS, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraStation(eq(10L), eq(UpdateType.UPDATE));
        verify(cameraStationUpdater, times(1)).updateCameraStation(eq(20L), eq(UpdateType.UPDATE));
    }

    private List<CameraMetadataUpdatedMessageDto> createMessage(final EntityType entityType, final UpdateType updateType) {
        return Collections.singletonList(
            new CameraMetadataUpdatedMessageDto(1L, Set.of(10L, 20L), updateType, Instant.now(), entityType));
    }
}
