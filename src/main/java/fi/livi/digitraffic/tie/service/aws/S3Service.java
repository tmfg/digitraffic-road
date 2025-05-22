package fi.livi.digitraffic.tie.service.aws;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.common.util.TimeUtil;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectAttributesRequest;
import software.amazon.awssdk.services.s3.model.GetObjectAttributesResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectAttributes;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static fi.livi.digitraffic.tie.service.weathercam.CameraImageS3Writer.LAST_MODIFIED_USER_METADATA_HEADER;

/**
 * Service-class for putting and getting images from S3.
 * Makes it easier to mock-tests.
 */
@Service
public class S3Service {
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    public record S3ImageObject(byte[] data, Date lastModified) {
    }

    private final S3Client s3Client;

    public S3Service(final S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @NotTransactionalServiceMethod
    public S3ImageObject readImage(final String bucketName, final String key, final String versionId)
            throws IOException {
        final var response = getObject(bucketName, key, versionId);

        return new S3ImageObject(response.readAllBytes(), Date.from(response.response().lastModified()));
    }

    @NotTransactionalServiceMethod
    public ResponseInputStream<GetObjectResponse> getObject(final String bucketName, final String key, final String versionId) throws IOException {
        log.info("method=getObject getting from bucket={} with key={} and versionId={}", bucketName, key, versionId);

        final GetObjectRequest.Builder builder = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key);

        if(StringUtils.isNotBlank(versionId)) {
            builder.versionId(versionId);
        }

        return s3Client.getObject(builder.build()));
    }

    @NotTransactionalServiceMethod
    public String putObject(final String bucketName, final String fileName, final String contentType, final byte[] byteArray) {
        final var response = s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(byteArray));

        return response.versionId();
    }

    @NotTransactionalServiceMethod
    public String putImage(final String bucketName, final String imageKey, final long timestampEpochMillis, final byte[] imageData) {
        final var metadata = Map.of(LAST_MODIFIED_USER_METADATA_HEADER, TimeUtil.getInLastModifiedHeaderFormat(
                Instant.ofEpochMilli(timestampEpochMillis)));

        if (log.isDebugEnabled()) {
            log.debug("method=putImage s3Key={} lastModified={}", imageKey,
                    metadata.get(LAST_MODIFIED_USER_METADATA_HEADER));
        }

        final var response = s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(imageKey)
                        .contentType("image/jpeg")
                        .metadata(metadata)
                        .contentLength((long) imageData.length)
                        .build(),
                RequestBody.fromBytes(imageData));

        return response.versionId();
    }

    @NotTransactionalServiceMethod
    public void deleteObject(final String bucketName, final String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    @NotTransactionalServiceMethod
    public boolean doesObjectExist(final String bucketName, final String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            return true;
        } catch (final S3Exception e) {
            if(e.statusCode() == 404) {
                return false;
            }

            throw e;
        }
    }
}
