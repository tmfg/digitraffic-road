package fi.livi.digitraffic.tie.metadata.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.conf.jms.CameraMetadataJMSListenerConfiguration;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.service.CameraMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;

/**
 * Service to handle JMS metadata updated messages
 */
@ConditionalOnNotWebApplication
@Service
public class MetadataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraMetadataJMSListenerConfiguration.class);
    private CameraStationUpdater cameraStationUpdater;

    public MetadataUpdateService(final CameraStationUpdater cameraStationUpdater) {
        this.cameraStationUpdater = cameraStationUpdater;
    }


    public int updateCameraMetadata(List<CameraMetadataUpdatedMessageDto> cameraUpdates) {
        final AtomicInteger updateCount = new AtomicInteger();

        cameraUpdates.forEach(u -> {
            log.info("method=updateCameraMetadata {}", ToStringHelper.toStringFull(u));
            final EntityType type = u.getEntityType();

            switch (type) {
            case CAMERA:
                if ( cameraStationUpdater.updateCameraStation(u.getLotjuId(), u.getUpdateType()) ) {
                    updateCount.incrementAndGet();
                }
                break;
            case PRESET:
                if ( cameraStationUpdater.updateCameraPreset(u.getLotjuId(), u.getUpdateType()) ) {
                    updateCount.incrementAndGet();
                }
                break;
            case MASTER_STORAGE:
            case VIDEO_SERVER:
            case CAMERA_CONFIGURATION:
            case ROAD_ADDRESS:
                // no handle
                break;
            default:
                throw new IllegalArgumentException("Unknown EntityType " + type);
            }

        });

        return updateCount.get();
    }
}
