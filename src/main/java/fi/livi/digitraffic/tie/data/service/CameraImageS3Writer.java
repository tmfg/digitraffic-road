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

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

@Component
@ConditionalOnNotWebApplication
public class CameraImageS3Writer {

    private static final Logger log = LoggerFactory.getLogger(CameraImageS3Writer.class);
    public static final String VERSIONS_SUFFIX = "-versions";

    private final AmazonS3 amazonS3Client;
    private final String bucketName;

    public final static String LAST_MODIFIED_USER_METADATA_HEADER = "last-modified";


    // Tue, 03 Sep 2019 13:56:36 GMT
    public final static SimpleDateFormat LAST_MODIFIED_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    static {
        LAST_MODIFIED_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    CameraImageS3Writer(
        final AmazonS3 amazonS3Client,
        final @Value("${dt.amazon.s3.weathercamBucketName}") String bucketName
    ) {
        this.amazonS3Client = amazonS3Client;
        this.bucketName = bucketName;
    }

    /**
     * @return S3 versionId
     */
    public String writeImage(final byte[] currentImageData, final byte[] versionedImageData,
                             final String imageKey, final long timestampEpochMillis) {
        try {
            final String versionedKey = getVersionedKey(imageKey);
            final ObjectMetadata metadata = new ObjectMetadata();
            final String lastModifiedInHeaderFormat = getInLastModifiedHeaderFormat(Instant.ofEpochMilli(timestampEpochMillis));
            metadata.addUserMetadata(LAST_MODIFIED_USER_METADATA_HEADER, lastModifiedInHeaderFormat);
            log.debug("method=writeImage s3Key={} lastModified: {}", imageKey, lastModifiedInHeaderFormat);
            metadata.setContentType("image/jpeg");

            // Put current image
            metadata.setContentLength(currentImageData.length);
            amazonS3Client.putObject(bucketName, imageKey, new ByteArrayInputStream(currentImageData), metadata);

            // Put versions image
            metadata.setContentLength(versionedImageData.length);
            PutObjectResult result = amazonS3Client.putObject(bucketName, versionedKey, new ByteArrayInputStream(versionedImageData), metadata);

            return result.getVersionId();
        } catch (Exception e) {
            log.warn("method=writeImage Failed to write image to S3 s3Key={} . mostSpecificCauseMessage={} . stackTrace={}",
                     imageKey, NestedExceptionUtils.getMostSpecificCause(e).getMessage(), ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    /**
     * @param key file key (name) ins S3 to delete
     * @return Info if the file exists and delete success. For non existing images success is false.
     */
    final DeleteInfo deleteImage(final String key) {
        final StopWatch start = StopWatch.createStarted();
        try  {
            if (amazonS3Client.doesObjectExist(bucketName, key)) {
                log.info("method=deleteImage presetId={} imagePath={}", resolvePresetIdFromKey(key), key);
                amazonS3Client.deleteObject(bucketName, key);
                return new DeleteInfo(true, true, start.getTime(), key);
            }
            return new DeleteInfo(false, false, start.getTime(), key);
        } catch (SdkClientException e) {
            log.error(String.format("Failed to remove remote file deleteImageFileName=%s", key), e);
            return new DeleteInfo(true, false, start.getTime(), key);
        }
    }

    private static String getInLastModifiedHeaderFormat(final Instant instant) {
        return LAST_MODIFIED_FORMAT.format(Date.from(instant));
    }

    public static String resolvePresetIdFromKey(final String key) {
        // Key ie. C0650802.jpg -> C0650802
        return StringUtils.substringBeforeLast(key, ".");
    }

    public static String getVersionedKey(String key) {
        // Key ie. C0650802.jpg -> C0650802-versions.jpg
        return resolvePresetIdFromKey(key) + VERSIONS_SUFFIX + ".jpg";
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
}
