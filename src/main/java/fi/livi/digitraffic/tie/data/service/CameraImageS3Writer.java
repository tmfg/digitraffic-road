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

@Component
@ConditionalOnNotWebApplication
public class CameraImageS3Writer {

    private static final Logger log = LoggerFactory.getLogger(CameraImageS3Writer.class);

    private final AmazonS3 amazonS3Client;
    private final String bucketName;

    public final static String LAST_MODIFIED_METADATA_HEADER = "last-modified";


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

    public void writeImage(final byte[] data, final String key, final int timestampEpochSecond) {

        try {

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata(LAST_MODIFIED_METADATA_HEADER, getInLastModifiedHeaderFormat(Instant.ofEpochSecond(timestampEpochSecond)));
            log.info("writeImage {} LAST-MODIFIED: {} {}", key, getInLastModifiedHeaderFormat(Instant.ofEpochSecond(timestampEpochSecond)), Instant.ofEpochSecond(timestampEpochSecond));
            metadata.setContentType("image/jpeg");
            metadata.setContentLength(data.length);
            amazonS3Client.putObject(bucketName, key, new ByteArrayInputStream(data), metadata);
        } catch (Exception e) {
            log.warn("method=writeImage Failed to write image to S3 s3Key={} . mostSpecificCauseMessage={} . stackTrace={}",
                     key, NestedExceptionUtils.getMostSpecificCause(e).getMessage(), ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    private static String getInLastModifiedHeaderFormat(Instant instant) {
        return LAST_MODIFIED_FORMAT.format(Date.from(instant));
    }

    /**
     * @param key file key (name) ins S3 to delete
     * @return Info if the file exists and delete success. For non existing images success is false.
     */
    final DeleteInfo deleteImage(final String key) {
        final StopWatch start = StopWatch.createStarted();
        try  {
            if (amazonS3Client.doesObjectExist(bucketName, key)) {
                log.info("method=deleteImage presetId={} imagePath={}", resolvePresetIdFromImageFullPath(key), key);
                amazonS3Client.deleteObject(bucketName, key);
                return new DeleteInfo(true, true, start.getTime(), key);
            }
            return new DeleteInfo(false, false, start.getTime(), key);
        } catch (SdkClientException e) {
            log.error(String.format("Failed to remove remote file deleteImageFileName=%s", key), e);
            return new DeleteInfo(true, false, start.getTime(), key);
        }
    }

    private static String resolvePresetIdFromImageFullPath(final String imageFullPath) {
        return StringUtils.substringBeforeLast(StringUtils.substringAfterLast(imageFullPath,"/"), ".");
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
