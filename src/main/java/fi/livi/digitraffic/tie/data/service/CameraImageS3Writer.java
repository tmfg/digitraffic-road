package fi.livi.digitraffic.tie.data.service;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

@Component
@ConditionalOnNotWebApplication
public class CameraImageS3Writer {

    private static final Logger log = LoggerFactory.getLogger(CameraImageS3Writer.class);
    public static final String IMAGE_VERSION_KEY_SUFFIX = "-versions.jpg";

    private final AmazonS3 amazonS3Client;
    private final String bucketName;
    private String s3WeathercamKeyRegexp;

    final static String LAST_MODIFIED_USER_METADATA_HEADER = "last-modified";


    // Tue, 03 Sep 2019 13:56:36 GMT
    final static SimpleDateFormat LAST_MODIFIED_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    static {
        LAST_MODIFIED_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    CameraImageS3Writer(
        final AmazonS3 amazonS3Client,
        @Value("${dt.amazon.s3.weathercam.bucketName}") final String bucketName,
        @Value("${dt.amazon.s3.weathercam.key.regexp}") final String s3WeathercamKeyRegexp) {
        this.amazonS3Client = amazonS3Client;
        this.bucketName = bucketName;
        this.s3WeathercamKeyRegexp = s3WeathercamKeyRegexp;
    }

    /**
     * @return S3 versionId
     */
    public String writeImage(final byte[] currentImageData, final byte[] versionedImageData,
                             final String imageKey, final long timestampEpochMillis) {
        try {
            checkS3KeyFormat(imageKey);
            final String versionedKey = getVersionedKey(imageKey);
            final ObjectMetadata metadata = new ObjectMetadata();
            final String lastModifiedInHeaderFormat = getInLastModifiedHeaderFormat(Instant.ofEpochMilli(timestampEpochMillis));
            metadata.addUserMetadata(LAST_MODIFIED_USER_METADATA_HEADER, lastModifiedInHeaderFormat);
            if (log.isDebugEnabled()) {
                log.debug("method=writeImage s3Key={} lastModified: {}", imageKey, lastModifiedInHeaderFormat);
            }
            metadata.setContentType("image/jpeg");

            // Put current image
            metadata.setContentLength(currentImageData.length);
            amazonS3Client.putObject(bucketName, imageKey, new ByteArrayInputStream(currentImageData), metadata);

            // Put versions image
            metadata.setContentLength(versionedImageData.length);
            final PutObjectResult result = amazonS3Client.putObject(bucketName, versionedKey, new ByteArrayInputStream(versionedImageData), metadata);
            if (log.isDebugEnabled()) {
                log.debug("method=writeImage versioned s3Key={} lastModified: {} versionId={}",
                          versionedKey, lastModifiedInHeaderFormat, result.getVersionId());
            }
            return result.getVersionId();
        } catch (Exception e) {
            log.warn("method=writeImage Failed to write image to S3 s3Key={} . mostSpecificCauseMessage={} . stackTrace={}",
                     imageKey, NestedExceptionUtils.getMostSpecificCause(e).getMessage(), ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    /**
     * @param imageKey file key (name) ins S3 to delete
     * @return Info if the file exists and delete success. For non existing images success is false.
     */
    DeleteInfo deleteImage(final String imageKey) {
        final StopWatch start = StopWatch.createStarted();
        // Hide current image and last from history
        try  {
            checkS3KeyFormat(imageKey);
            if (amazonS3Client.doesObjectExist(bucketName, imageKey)) {
                final String versionedKey = getVersionedKey(imageKey);
                log.info("method=deleteImage presetId={} s3Key={}", resolvePresetIdFromKey(imageKey), imageKey);
                amazonS3Client.deleteObject(bucketName, imageKey);
                if (amazonS3Client.doesObjectExist(bucketName, versionedKey)) {
                    amazonS3Client.deleteObject(bucketName, versionedKey);
                    return DeleteInfo.success(start.getTime(), versionedKey);
                }
                return DeleteInfo.success(start.getTime(), imageKey);
            }
            return DeleteInfo.doesNotExist(start.getTime(), imageKey);
        } catch (Exception e) {
            log.error(String.format("Failed to remove s3 file s3Key=%s", imageKey), e);
            return DeleteInfo.failed(start.getTime(), imageKey);
        }
    }

    static String getInLastModifiedHeaderFormat(final Instant instant) {
        return LAST_MODIFIED_FORMAT.format(Date.from(instant));
    }

    private static String resolvePresetIdFromKey(final String key) {
        // Key ie. C0650802.jpg -> C0650802
        return StringUtils.substringBeforeLast(key, ".");
    }

    static String getVersionedKey(String key) {
        // Key ie. C0650802.jpg -> C0650802-versions.jpg
        return resolvePresetIdFromKey(key) + IMAGE_VERSION_KEY_SUFFIX;
    }

    static class DeleteInfo {

        private final boolean fileExists;
        private final boolean deleteSuccess;
        private final long durationMs;
        private final String fullPath;

        private DeleteInfo(final boolean fileExists, final boolean deleteSuccess, final long durationMs, final String fullPath) {
            this.fileExists = fileExists;
            this.deleteSuccess = deleteSuccess;
            this.durationMs = durationMs;
            this.fullPath = fullPath;
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

        boolean isFileExists() {
            return fileExists;
        }

        boolean isDeleteSuccess() {
            return deleteSuccess;
        }

        boolean isFileExistsAndDeleteSuccess() {
            return isFileExists() && isDeleteSuccess();
        }

        boolean isSuccess() {
            return !isFileExists() || isDeleteSuccess();
        }

        long getDurationMs() {
            return durationMs;
        }

        String getFullPath() {
            return fullPath;
        }
    }

    private void checkS3KeyFormat(final String key) {
        if (!key.matches(s3WeathercamKeyRegexp)) {
            throw new IllegalArgumentException(String.format("S3 key should match regexp format \"%s\" ie. \"C1234567.jpg\" but was \"%s\"", s3WeathercamKeyRegexp, key));
        }
    }
}
