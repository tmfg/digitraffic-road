package fi.livi.digitraffic.tie.service.weathercam;

import java.io.ByteArrayInputStream;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;

@Component
@ConditionalOnNotWebApplication
public class CameraImageS3Writer {
    private static final Logger log = LoggerFactory.getLogger(CameraImageS3Writer.class);
    public static final String IMAGE_VERSION_KEY_SUFFIX = "-versions.jpg";

    private final AmazonS3 amazonS3Client;
    private final WeathercamS3Properties weathercamS3Properties;

    public final static String LAST_MODIFIED_USER_METADATA_HEADER = "last-modified";


    CameraImageS3Writer(final AmazonS3 weathercamS3Client, final WeathercamS3Properties weathercamS3Properties) {
        this.amazonS3Client = weathercamS3Client;
        this.weathercamS3Properties = weathercamS3Properties;
    }

    public String writeImage(final byte[] currentImageData, final byte[] versionedImageData,
                             final String imageKey, final long timestampEpochMillis) {
        writeCurrentImage(currentImageData, imageKey, timestampEpochMillis);
        return writeVersionedImage(versionedImageData, imageKey, timestampEpochMillis);

    }

    /**
     * Writes given image as current weather camera image
     * @param currentImageData image bytes to write
     * @param imageKey s3 key
     * @param timestampEpochMillis image timestamp
     */
    public void writeCurrentImage(final byte[] currentImageData, final String imageKey, final long timestampEpochMillis) {
        try {
            checkS3KeyFormat(imageKey);
            final ObjectMetadata metadata = createS3Metadata(timestampEpochMillis, currentImageData.length);

            if (log.isDebugEnabled()) {
                log.debug("method=writeCurrentImage s3Key={} lastModified={}", imageKey, metadata.getUserMetaDataOf(LAST_MODIFIED_USER_METADATA_HEADER));
            }

            // Put current image
            metadata.setContentLength(currentImageData.length);
            amazonS3Client.putObject(weathercamS3Properties.getS3WeathercamBucketName(), imageKey, new ByteArrayInputStream(currentImageData), metadata);
        } catch (final Exception e) {
            throw new RuntimeException(String.format("%s method writeCurrentImage Failed to write image to S3 s3Key=%s", getClass().getSimpleName(), imageKey), e);
        }
    }

    /**
     * Writes image version for given image
     * @param versionedImageData image bytes to write
     * @param imageKey current image s3 key. Key will be appended with version suffix.
     * @param timestampEpochMillis image timestamp
     * @return s3 version id
     */
    private String writeVersionedImage(final byte[] versionedImageData,
                                       final String imageKey,
                                       final long timestampEpochMillis) {
        final String versionedKey = getVersionedKey(imageKey);

        try {
            checkS3KeyFormat(imageKey);
            final ObjectMetadata metadata = createS3Metadata(timestampEpochMillis, versionedImageData.length);

            // Put versions image
            final PutObjectResult result = amazonS3Client.putObject(weathercamS3Properties.getS3WeathercamBucketName(), versionedKey,
                                                                    new ByteArrayInputStream(versionedImageData), metadata);
            if (log.isDebugEnabled()) {
                log.debug("method=writeVersionedImage s3Key={} lastModified: {} s3VersionId=\"{}\"",
                          versionedKey, metadata.getUserMetaDataOf(LAST_MODIFIED_USER_METADATA_HEADER), result.getVersionId());
            }
            return result.getVersionId();
        } catch (final Exception e) {
            throw new RuntimeException(String.format("method=writeVersionedImage Failed to write image to S3 s3Key=%s", versionedKey), e);
        }
    }

    private ObjectMetadata createS3Metadata(final long timestampEpochMillis, final long contentLength) {
        final ObjectMetadata metadata = new ObjectMetadata();
        final String lastModifiedInHeaderFormat = DateHelper.getInLastModifiedHeaderFormat(Instant.ofEpochMilli(timestampEpochMillis));
        metadata.addUserMetadata(LAST_MODIFIED_USER_METADATA_HEADER, lastModifiedInHeaderFormat);
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(contentLength);
        return metadata;
    }

    /**
     * @param imageKey file key (name) ins S3 to delete
     * @return Info if the file exists and delete success. For non existing images success is false.
     */
    public DeleteInfo deleteImage(final String imageKey) {
        final StopWatch start = StopWatch.createStarted();
        // Hide current image and last from history
        try  {
            checkS3KeyFormat(imageKey);
            if (amazonS3Client.doesObjectExist(weathercamS3Properties.getS3WeathercamBucketName(), imageKey)) {
                final String versionedKey = getVersionedKey(imageKey);
                log.info("method=deleteImage presetId={} s3Key={}", resolvePresetIdFromKey(imageKey), imageKey);
                amazonS3Client.deleteObject(weathercamS3Properties.getS3WeathercamBucketName(), imageKey);
                if (amazonS3Client.doesObjectExist(weathercamS3Properties.getS3WeathercamBucketName(), versionedKey)) {
                    amazonS3Client.deleteObject(weathercamS3Properties.getS3WeathercamBucketName(), versionedKey);
                    return DeleteInfo.success(start.getTime(), versionedKey);
                }
                return DeleteInfo.success(start.getTime(), imageKey);
            }
            return DeleteInfo.doesNotExist(start.getTime(), imageKey);
        } catch (final Exception e) {
            log.error(String.format("Failed to remove s3 file s3Key=%s", imageKey), e);
            return DeleteInfo.failed(start.getTime(), imageKey);
        }
    }

    private static String resolvePresetIdFromKey(final String key) {
        // Key ie. C0650802.jpg -> C0650802
        return StringUtils.substringBeforeLast(key, ".");
    }

    public static String getVersionedKey(final String key) {
        // Key ie. C0650802.jpg -> C0650802-versions.jpg
        return resolvePresetIdFromKey(key) + IMAGE_VERSION_KEY_SUFFIX;
    }

    public static class DeleteInfo {
        private final boolean fileExists;
        private final boolean deleteSuccess;
        private final long durationMs;
        private final String key;

        private DeleteInfo(final boolean fileExists, final boolean deleteSuccess, final long durationMs, final String key) {
            this.fileExists = fileExists;
            this.deleteSuccess = deleteSuccess;
            this.durationMs = durationMs;
            this.key = key;
        }

        static DeleteInfo failed(final long durationMs, final String key) {
            return new DeleteInfo(true, false, durationMs, key);
        }

        static DeleteInfo doesNotExist(final long durationMs, final String key) {
            return new DeleteInfo(false, false, durationMs, key);
        }

        static DeleteInfo success(final long durationMs, final String key) {
            return new DeleteInfo(true, true, durationMs, key);
        }

        public boolean isFileExists() {
            return fileExists;
        }

        public boolean isDeleteSuccess() {
            return deleteSuccess;
        }

        public boolean isSuccess() {
            return !isFileExists() || isDeleteSuccess();
        }

        public long getDurationMs() {
            return durationMs;
        }

        public String getKey() {
            return key;
        }
    }

    private void checkS3KeyFormat(final String key) {
        if (!key.matches(weathercamS3Properties.getS3WeathercamKeyRegexp())) {
            throw new IllegalArgumentException(String.format("S3 key should match regexp format \"%s\" ie. \"C1234567.jpg\" but was \"%s\"", weathercamS3Properties
                .getS3WeathercamKeyRegexp(), key));
        }
    }
}
