package fi.livi.digitraffic.tie.metadata.service.camera;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.camera.PresetHistoryDataDto;
import fi.livi.digitraffic.tie.data.dto.camera.PresetHistoryDto;
import fi.livi.digitraffic.tie.data.service.CameraImageS3Writer;
import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

@Service
public class CameraPresetHistoryService {
    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);
    private CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final String s3WeathercamKeyRegexp;
    private final String s3WeathercamBucketUrl;
    private final int historyMaxAgeHours;
    private final String weathercamBaseUrl;

    public enum HistoryStatus {
        PUBLIC("History version found and it's publishable"),
        SECRET("History version found but it's not publishable"),
        NOT_FOUND("No history found for preset at all"),
        TOO_OLD("History version is over 24 h old and for that reason not publishable"),
        ILLEGAL_KEY(" presetImageName did not match correct regex format ^C([0-9]{7})\\.jpg$ for S3 key");

        private final String description;

        HistoryStatus(final String description) {
            this.description = description;
        }
    }

    @Autowired
    public CameraPresetHistoryService(final CameraPresetHistoryRepository cameraPresetHistoryRepository,
                                      @Value("${dt.amazon.s3.weathercam.bucketName}") final String s3WeathercamBucketName,
                                      @Value("${dt.amazon.s3.weathercam.region}") final String s3WeathercamRegion,
                                      @Value("${dt.amazon.s3.weathercam.key.regexp}") final String s3WeathercamKeyRegexp,
                                      @Value("${dt.amazon.s3.weathercam.history.maxAgeHours}") final int historyMaxAgeHours,
                                      @Value("${weathercam.baseUrl}") final String weathercamBaseUrl) {
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
        this.s3WeathercamKeyRegexp = s3WeathercamKeyRegexp;
        this.historyMaxAgeHours = historyMaxAgeHours;
        this.weathercamBaseUrl = weathercamBaseUrl;
        this.s3WeathercamBucketUrl = createS3WeathercamBucketUrl(s3WeathercamBucketName, s3WeathercamRegion);
    }

    private String createS3WeathercamBucketUrl(
            String s3WeathercamBucketName,
            String s3WeathercamRegion) {
        return String.format("http://%s.s3-%s.amazonaws.com", s3WeathercamBucketName, s3WeathercamRegion);
    }

    @Transactional
    public void saveHistory(final CameraPresetHistory history) {
        cameraPresetHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findHistoryInclSecret(final String presetId, final String versionId) {
        return cameraPresetHistoryRepository.findByIdPresetIdAndIdVersionId(presetId, versionId).orElse(null);
    }

    @Transactional(readOnly = true)
    public PresetHistoryDto findHistoryInclSecret(final String presetId, final ZonedDateTime atTime) {

        if (!cameraPresetHistoryRepository.existsByIdPresetId(presetId)) {
            throw new ObjectNotFoundException("CameraPresetHistory", presetId);
        }

        if (atTime != null) {
            final Optional<CameraPresetHistory> latestWithTime = cameraPresetHistoryRepository
                .findLatestPublishableByPresetIdAndTime(presetId, atTime.toInstant(), getOldestLimitNow().toInstant());

            if (latestWithTime.isPresent()) {
                return convertToPresetHistory(presetId, Collections.singletonList(latestWithTime.get()));
            } else {
                return convertToPresetHistory(presetId, Collections.emptyList());
            }

        } else {
            return convertToPresetHistory(presetId,
                cameraPresetHistoryRepository.findAllPublishableByPresetIdOrderByLastModifiedDesc(presetId, getOldestLimitNow().toInstant()));
        }
    }

    private PresetHistoryDto convertToPresetHistory(final String presetId, final List<CameraPresetHistory> history) {
        return new PresetHistoryDto(
            presetId,
            history.stream().map(h ->
                new PresetHistoryDataDto(h.getLastModified(),
                                         createPublicUrlForVersion(h.getPresetId(), h.getVersionId()),
                                         h.getSize()))
                .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findLatestWithPresetIdIncSecret(final String presetId) {
        return cameraPresetHistoryRepository.findLatestByPresetId(presetId).orElse(null);
    }

    /** Orderer from oldest to newest
     * Only for internal use */
    @Transactional(readOnly = true)
    public List<CameraPresetHistory> findAllByPresetIdInclSecretAsc(final String presetId) {
        return cameraPresetHistoryRepository.findByIdPresetIdOrderByLastModifiedAsc(presetId);
    }

    @Transactional
    public int deleteAllWithPresetId(final String presetId) {
        return cameraPresetHistoryRepository.deleteByIdPresetId(presetId);
    }

    @Transactional
    public void updatePresetHistoryPublicityForCamera(final RoadStation rs) {
        // If statTime is null it means now -> no history to update or
        // if startTime is in the future -> no history to update
        if (rs.getPublicityStartTime() != null && !rs.getPublicityStartTime().isAfter(ZonedDateTime.now())) {
            final String cameraId = CameraHelper.convertNaturalIdToCameraId(rs.getNaturalId());
            cameraPresetHistoryRepository.updatePresetHistoryPublicityForCameraId(
                cameraId, rs.internalIsPublic(), rs.getPublicityStartTime().toInstant());
        }
    }

    /**
     * Resolves history status for given camera preset image version.
     *
     * @param presetImageName in regex format ^C([0-9]{7})\\.jpg$
     * @param versionId version string to check
     * @return PUBLIC - History version found and it's publishable <br />
     *         SECRET - History version found but it's not publishable <br />
     *         NOT_FOUND - No history found for preset at all <br />
     *         TOO_OLD - History version is over 24 h old and for that reason not publishable <br />
     *         ILLEGAL_KEY presetImageName did not match correct regex format ^C([0-9]{7})\\.jpg$ for S3 key
     */
    @Transactional(readOnly = true)
    public HistoryStatus resolveHistoryStatusForVersion(final String presetImageName, final String versionId) {

        if (!presetImageName.matches(s3WeathercamKeyRegexp)) {
            return HistoryStatus.ILLEGAL_KEY;
        }
        // C1234567.jpg -> C1234567
        final CameraPresetHistory history = findHistoryInclSecret(getPresetId(presetImageName), versionId);
        final ZonedDateTime oldestLimit = getOldestLimitNow();

        if (history == null) {
            return HistoryStatus.NOT_FOUND;
        } else if (!history.getPublishable()) {
            return HistoryStatus.SECRET;
        } else if (history.getLastModified().isBefore(oldestLimit)) {
            return HistoryStatus.TOO_OLD;
        }
        return HistoryStatus.PUBLIC;
    }

    private ZonedDateTime getOldestLimitNow() {
        return ZonedDateTime.now().minusHours(historyMaxAgeHours);
    }

    public URI createS3UriForVersion(final String imageName, final String versionId) {
        return URI.create(String.format("%s/%s?versionId=%s", s3WeathercamBucketUrl,
            createImageVersionKey(getPresetId(imageName)), versionId));
    }

    private String createPublicUrlForVersion(final String presetId, final String versionId) {
        return String.format("%s%s.jpg?versionId=%s", weathercamBaseUrl, presetId, versionId);
    }

    private String getPresetId(final String imageName) {
        return imageName.substring(0,8);
    }

    private String createImageVersionKey(String presetId) {
        return presetId + CameraImageS3Writer.IMAGE_VERSION_KEY_SUFFIX;
    }
}
