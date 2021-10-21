package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.TestUtils.getRandomLotjuId;
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

import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.service.v1.AbstractMetadataUpdateMessageHandlerTest;

public class CameraMetadataUpdateMessageHandlerTest extends AbstractMetadataUpdateMessageHandlerTest {

    public static final long CAMERA_LOTJU_ID1 = 11L;
    public static final long CAMERA_LOTJU_ID2 = 21L;
    public static final long PRESET_LOTJU_ID = 2L;

    @Autowired
    private CameraMetadataUpdateMessageHandler cameraMetadataUpdateMessageHandler;

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
            verifyPresetMessageTriggersUpdate(updateType);
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
        cameraMetadataUpdateMessageHandler.updateCameraMetadataFromJms(createMessage(getRandomLotjuId(), entityType, updateType));
        verifyNoInteractions(cameraStationUpdater);
    }

    private void verifyCameraMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraStation(CAMERA_LOTJU_ID1, updateType)).thenReturn(true);
        cameraMetadataUpdateMessageHandler.updateCameraMetadataFromJms(createMessage(CAMERA_LOTJU_ID1, EntityType.CAMERA, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraStation(eq(CAMERA_LOTJU_ID1), eq(updateType));
    }

    private void verifyPresetMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraPreset(PRESET_LOTJU_ID, updateType)).thenReturn(true);
        cameraMetadataUpdateMessageHandler.updateCameraMetadataFromJms(createMessage(PRESET_LOTJU_ID, EntityType.PRESET, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraPreset(eq(PRESET_LOTJU_ID), eq(updateType));
    }

    private void verifyRoadAddressMessageTriggersUpdate(final UpdateType updateType) {
        // RoadAddress makes always UpdateType.UPDATE for stations
        when(cameraStationUpdater.updateCameraStation(CAMERA_LOTJU_ID1, UpdateType.UPDATE)).thenReturn(true);
        when(cameraStationUpdater.updateCameraStation(CAMERA_LOTJU_ID2, UpdateType.UPDATE)).thenReturn(true);
        cameraMetadataUpdateMessageHandler.updateCameraMetadataFromJms(createMessage(getRandomLotjuId(), EntityType.ROAD_ADDRESS, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraStation(eq(CAMERA_LOTJU_ID1), eq(UpdateType.UPDATE));
        verify(cameraStationUpdater, times(1)).updateCameraStation(eq(CAMERA_LOTJU_ID2), eq(UpdateType.UPDATE));
    }

    private List<CameraMetadataUpdatedMessageDto> createMessage(final long entityLotjuId, final EntityType entityType, final UpdateType updateType) {
        return Collections.singletonList(
            new CameraMetadataUpdatedMessageDto(entityLotjuId, Set.of(CAMERA_LOTJU_ID1, CAMERA_LOTJU_ID2), updateType, Instant.now(), entityType));
    }
}
