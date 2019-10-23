package fi.livi.digitraffic.tie.metadata.service.camera;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.metadata.service.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.metadata.service.CameraMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.metadata.service.MetadataUpdatedMessageDto.UpdateType;

public class CameraMetadataMessageHandlerTest extends AbstractDaemonTestWithoutS3 {

    @Autowired
    private CameraMetadataMessageHandler cameraMetadataMessageHandler;

    @MockBean
    CameraStationUpdater cameraStationUpdater;

    @Test
    public void cameraMessagesShouldTriggerUpdate() {
        for (UpdateType updateType : UpdateType.values()) {
            verifyCameraMessageTriggersUpdate(updateType);
        }
    }

    @Test
    public void presetMessagesShouldTriggerUpdate() {
        for (UpdateType updateType : UpdateType.values()) {
            verifyPresetaMessageTriggersUpdate(updateType);
        }
    }

    @Test
    public void roadAddressMessagesShouldTriggerUpdate() {
        for (UpdateType updateType : UpdateType.values()) {
            verifyRoadAddressMessageTriggersUpdate(updateType);
        }
    }

    @Test
    public void otherMessagesShouldNotTriggerUpdate() {
        ArrayList<EntityType> entityTypes = new ArrayList<>(Arrays.asList(EntityType.values()));
        entityTypes.removeAll(Arrays.asList(EntityType.CAMERA, EntityType.PRESET, EntityType.ROAD_ADDRESS));
        for (EntityType entityType : entityTypes) {
            for (UpdateType updateType : UpdateType.values()) {
                verifyMessageWontTriggersUpdate(updateType, entityType);
            }
        }
    }

    private void verifyMessageWontTriggersUpdate(final UpdateType updateType,
                                                 final EntityType entityType) {
        cameraMetadataMessageHandler.updateCameraMetadata(createMessage(entityType, updateType));
        verifyZeroInteractions(cameraStationUpdater);

    }

    private void verifyCameraMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraStation(1L, updateType)).thenReturn(true);
        cameraMetadataMessageHandler.updateCameraMetadata(createMessage(EntityType.CAMERA, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraStation(eq(1L), eq(updateType));
        verifyNoMoreInteractions(cameraStationUpdater);
    }

    private void verifyPresetaMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraPreset(1L, updateType)).thenReturn(true);
        cameraMetadataMessageHandler.updateCameraMetadata(createMessage(EntityType.PRESET, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraPreset(eq(1L), eq(updateType));
        verifyNoMoreInteractions(cameraStationUpdater);
    }

    private void verifyRoadAddressMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraStation(2L, updateType)).thenReturn(true);
        cameraMetadataMessageHandler.updateCameraMetadata(createMessage(EntityType.ROAD_ADDRESS, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraStation(eq(2L), eq(updateType));
        verifyNoMoreInteractions(cameraStationUpdater);
    }

    private List<CameraMetadataUpdatedMessageDto> createMessage(final EntityType entityType, final UpdateType updateType) {
        return Collections.singletonList(
            new CameraMetadataUpdatedMessageDto(1L, Collections.singleton(2L), updateType, Instant.now(), entityType));
    }
}
