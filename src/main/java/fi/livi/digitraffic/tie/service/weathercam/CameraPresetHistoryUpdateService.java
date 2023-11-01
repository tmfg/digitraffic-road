package fi.livi.digitraffic.tie.service.weathercam;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.weathercam.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.weathercam.CameraPresetHistory;

@ConditionalOnNotWebApplication
@Service
public class CameraPresetHistoryUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);
    private final CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final CameraImageUpdateHandler cameraImageUpdateHandler;

    @Autowired
    public CameraPresetHistoryUpdateService(final CameraPresetHistoryRepository cameraPresetHistoryRepository,
                                            final CameraImageUpdateHandler cameraImageUpdateHandler) {
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
        this.cameraImageUpdateHandler = cameraImageUpdateHandler;
    }

    @Transactional
    public void saveHistory(final CameraPresetHistory history) {
        cameraPresetHistoryRepository.save(history);
    }

    @Transactional
    public int deleteAllWithPresetId(final String presetId) {
        return cameraPresetHistoryRepository.deleteByIdPresetId(presetId);
    }

    @Transactional
    public void updatePresetHistoryPublicityForCamera(final RoadStation rs) {
        // If statTime is null it means now -> no history to update or
        // if startTime is in the future -> no history to update
        final String cameraId = CameraHelper.convertNaturalIdToCameraId(rs.getNaturalId());
        if (rs.getPublicityStartTime() != null && !rs.getPublicityStartTime().isAfter(ZonedDateTime.now())) {
            log.info("method=updatePresetHistoryPublicityForCamera cameraId={} toPublic={} fromPublicityStartTime={}",
                cameraId, rs.internalIsPublic(), rs.getPublicityStartTime().toInstant());
            cameraPresetHistoryRepository.updatePresetHistoryPublicityForCameraId(
                cameraId, rs.internalIsPublic(), rs.getPublicityStartTime().toInstant());
        }
        // If camera is not public, hide current image
        if (!rs.isPublicNow()) {
            try {
                cameraImageUpdateHandler.hideCurrentImagesForCamera(rs);
            } catch (Exception e) {
                log.error(String.format("method=updatePresetHistoryPublicityForCamera Error while calling hideCurrentImagesForCamera " +
                                        "for cameraId: %s. History is updated but current images are not hidden in S3", cameraId), e);
            }
        }
    }

    @Transactional
    public void deleteOlderThanHoursHistory(final int hours) {
        cameraPresetHistoryRepository.deleteOlderThanHours(hours);
    }
}
