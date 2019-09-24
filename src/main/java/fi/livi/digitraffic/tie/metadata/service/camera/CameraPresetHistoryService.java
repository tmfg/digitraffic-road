package fi.livi.digitraffic.tie.metadata.service.camera;

import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.CameraImageS3Writer;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

@Service
public class CameraPresetHistoryService {

    private CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final String s3WeathercamBucketName;
    private final String s3WeathercamRegion;
    private final String s3WeathercamKeyRegexp;
    private final String s3WeathercamBucketUrl;
    private final int historyMaxAgeHours;



    public enum HistoryStatus {
        PUBLIC,
        SECRET,
        NOT_FOUND,
        TOO_OLD,
        ILLEGAL_KEY;
    }
    @Autowired
    public CameraPresetHistoryService(final CameraPresetHistoryRepository cameraPresetHistoryRepository,
                                      @Value("${dt.amazon.s3.weathercam.bucketName}") final String s3WeathercamBucketName,
                                      @Value("${dt.amazon.s3.weathercam.region}") final String s3WeathercamRegion,
                                      @Value("${dt.amazon.s3.weathercam.key.regexp}") final String s3WeathercamKeyRegexp,
                                      @Value("${dt.amazon.s3.weathercam.history.maxAgeHours}") final int historyMaxAgeHours) {
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
        this.s3WeathercamBucketName = s3WeathercamBucketName;
        this.s3WeathercamRegion = s3WeathercamRegion;
        this.s3WeathercamKeyRegexp = s3WeathercamKeyRegexp;
        this.historyMaxAgeHours = historyMaxAgeHours;
        this.s3WeathercamBucketUrl = String.format("http://%s.s3-%s.amazonaws.com", s3WeathercamBucketName, s3WeathercamRegion);
    }

    @Transactional
    public void saveHistory(final CameraPresetHistory history) {
        cameraPresetHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findHistory(final String presetId, final String versionId) {
        return cameraPresetHistoryRepository.findByIdPresetIdAndIdVersionId(presetId, versionId).orElse(null);
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findHistory(final String presetId, final ZonedDateTime atTime) {
        return cameraPresetHistoryRepository
            .findLatestByPresetIdAndTime(presetId, atTime.toInstant(), Instant.now().minus(historyMaxAgeHours, ChronoUnit.HOURS) )
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findLatestWithPresetId(final String presetId) {
        return cameraPresetHistoryRepository.findLatestByPresetId(presetId).orElse(null);
    }

    /** Orderer from oldest to newest */
    @Transactional(readOnly = true)
    public List<CameraPresetHistory> findAllByPresetId(final String presetId) {
        return cameraPresetHistoryRepository.findByIdPresetIdOrderByLastModifiedAsc(presetId);
    }

    @Transactional
    public int deleteAllWithPresetId(final String presetId) {
        return cameraPresetHistoryRepository.deleteByIdPresetId(presetId);
    }

    @Transactional
    public void updatePresetHistoryPublicityForCamera(final RoadStation rs) {
        // TODO DPO-462 get start time of public / not public state and update history acordingly
        // rs.isPublic() && rs.GetPublicStartTime() etc.?
        // getPresets and update presetHistory
    }

    public HistoryStatus resolveHistoryStatus(final String imageName, final String versionId) {

        if (!imageName.matches(s3WeathercamKeyRegexp)) {
            return HistoryStatus.ILLEGAL_KEY;
        }
        // C1234567.jpg -> C1234567
        final CameraPresetHistory history = findHistory(getPresetId(imageName), versionId);
        final ZonedDateTime oldestLimit = ZonedDateTime.now().minusHours(historyMaxAgeHours);

        if (history == null) {
            return HistoryStatus.NOT_FOUND;
        } else if (!history.getPublishable()) {
            return HistoryStatus.SECRET;
        } else if (history.getLastModified().isBefore(oldestLimit)) {
            return HistoryStatus.TOO_OLD;
        }
        return HistoryStatus.PUBLIC;
    }

    public URI createS3UriForVersion(final String imageName, final String versionId) {
        return URI.create(String.format("%s/%s?versionId=%s", s3WeathercamBucketUrl,
            createImageVersionKey(getPresetId(imageName)), versionId));
    }

    private String getPresetId(final String imageName) {
        return imageName.substring(0,8);
    }

    private String createImageVersionKey(String presetId) {
        return presetId + CameraImageS3Writer.IMAGE_VERSION_KEY_SUFFIX;
    }
}
