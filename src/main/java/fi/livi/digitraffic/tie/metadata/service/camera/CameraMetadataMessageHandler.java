package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.conf.jms.CameraMetadataJMSListenerConfiguration;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.metadata.service.CameraMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

/**
 * Service to handle JMS metadata updated messages
 */
@ConditionalOnNotWebApplication
@Service
public class CameraMetadataMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(CameraMetadataJMSListenerConfiguration.class);
    private final CameraStationUpdater cameraStationUpdater;
    private final DataStatusService dataStatusService;

    public CameraMetadataMessageHandler(final CameraStationUpdater cameraStationUpdater,
                                        final DataStatusService dataStatusService) {
        this.cameraStationUpdater = cameraStationUpdater;
        this.dataStatusService = dataStatusService;
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
            case ROAD_ADDRESS:
                u.getAsemmaLotjuIds().forEach(lotjuId -> {
                    if ( cameraStationUpdater.updateCameraStation(lotjuId, u.getUpdateType()) ) {
                        updateCount.incrementAndGet();
                    }
                });
                break;
            case MASTER_STORAGE:
            case VIDEO_SERVER:
            case CAMERA_CONFIGURATION:
                // no handle
                break;
            default:
                throw new IllegalArgumentException("Unknown EntityType " + type);
            }

        });

        if (updateCount.get() > 0) {
            dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA);
        }
        return updateCount.get();
    }
}
