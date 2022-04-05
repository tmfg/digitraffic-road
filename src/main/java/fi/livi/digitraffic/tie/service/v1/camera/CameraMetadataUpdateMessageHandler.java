package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType.UPDATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.CameraMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;

/**
 * Service to handle JMS metadata updated messages
 */
@ConditionalOnNotWebApplication
@Component
public class CameraMetadataUpdateMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(CameraMetadataUpdateMessageHandler.class);
    private final CameraStationUpdater cameraStationUpdater;
    private final DataStatusService dataStatusService;

    public CameraMetadataUpdateMessageHandler(final CameraStationUpdater cameraStationUpdater,
                                              final DataStatusService dataStatusService) {
        this.cameraStationUpdater = cameraStationUpdater;
        this.dataStatusService = dataStatusService;
    }

    // Disable info logging as it can be normally over 1 s. Log only if over default warning level 5 s.
    @PerformanceMonitor(maxInfoExcecutionTime = 100000)
    public int updateCameraMetadataFromJms(List<CameraMetadataUpdatedMessageDto> cameraMetadataUpdates) {
        int updateCount = 0;

        for (CameraMetadataUpdatedMessageDto message : cameraMetadataUpdates) {
            log.info("method=updateCameraMetadataFromJms {}", ToStringHelper.toStringFull(message));
            final EntityType type = message.getEntityType();
            final UpdateType updateType = message.getUpdateType();

            // Skip messages that are older than 24 hours as metadata update job is running every 12 hours
            // so this could also be 12 h but for safety margin lets keep it in 24h. This could happen in case
            // of JMS connection problems for over 24 hours.
            if ( message.getUpdateTime().plus(1, ChronoUnit.DAYS).isAfter(Instant.now()) ) {
                try {
                    switch (type) {
                    case CAMERA:
                        if (cameraStationUpdater.updateCameraStation(message.getLotjuId(), updateType)) {
                            updateCount++;
                        }
                        break;
                    case PRESET:
                        if (cameraStationUpdater.updateCameraPreset(message.getLotjuId(), updateType)) {
                            updateCount++;
                        }
                        break;
                    case ROAD_ADDRESS:
                        // Even in case of delete of the address the update type for station is UPDATE
                        updateCount += message.getAsemaLotjuIds().stream()
                            .filter(asemaId -> cameraStationUpdater.updateCameraStation(asemaId, UPDATE)).count();
                        break;
                    case MASTER_STORAGE:
                    case VIDEO_SERVER:
                    case CAMERA_CONFIGURATION:
                        // no handle
                        break;
                    default:
                        log.error(String.format("method=updateCameraMetadataFromJms Unknown EntityType %s", type));
                    }
                } catch (final Exception e) {
                    log.error(String.format("method=updateCameraMetadataFromJms Error with %s", ToStringHelper.toStringFull(message)), e);
                }
            }
        }

        if (updateCount > 0) {
            dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA);
        }
        return updateCount;
    }
}
