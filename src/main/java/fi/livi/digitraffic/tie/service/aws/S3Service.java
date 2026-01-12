package fi.livi.digitraffic.tie.service.aws;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.common.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static fi.livi.digitraffic.tie.service.weathercam.CameraImageS3Writer.LAST_MODIFIED_USER_METADATA_HEADER;

/**
 * Service-class for putting and getting images from S3.
 * Makes it easier to mock-tests.
 */
@Service
public class S3Service {
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    public record S3ImageObject(byte[] data, Date lastModified, String key, String versionId) {
        public String getCacheKey() {
            return key + ":" + (versionId != null ? versionId : "") + ":" + lastModified.toInstant().toString();
        }
    }

    private final S3Client s3Client;
    private final S3AsyncClient s3AsyncClient;
    private final Semaphore s3Limiter = new Semaphore(100);

    public S3Service(final S3Client s3Client, final S3AsyncClient s3AsyncClient) {
        this.s3AsyncClient = s3AsyncClient;
        this.s3Client = s3Client;
    }

    @NotTransactionalServiceMethod
    public CompletableFuture<S3ImageObject> readImageAsync(
            final String bucket, final String key, final String versionId) {

        final GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key);
        if (StringUtils.isNotBlank(versionId)) {
            requestBuilder.versionId(versionId);
        }

        // Acquire permit immediately and safely
        try {
            if (!s3Limiter.tryAcquire(300, TimeUnit.MILLISECONDS)) {
                return CompletableFuture.failedFuture(
                        new S3RateLimitExceededException("Exceeded S3 concurrency limit")
                );
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt(); // restore the interrupt status
            return CompletableFuture.failedFuture(
                    new CompletionException(e)
            );
        }

        try {
            return s3AsyncClient.getObject(
                            requestBuilder.build(),
                            AsyncResponseTransformer.toBytes()
                    )
                    .orTimeout(35, TimeUnit.SECONDS) // orTimeout should be ≤ apiCallAttemptTimeout
                    .thenApply(resp -> new S3ImageObject(
                            resp.asByteArray(),
                            Date.from(resp.response().lastModified()), key, versionId))
                    // Release permit exactly once
                    .whenComplete((r, e) -> s3Limiter.release());
        } catch (final Exception e) {
            s3Limiter.release(); // Ensure release on exception
            throw new RuntimeException(e);
        }
    }

    @NotTransactionalServiceMethod
    public S3ImageObject readImage(final String bucketName, final String key, final String versionId)
            throws IOException {
        final var response = getObject(bucketName, key, versionId);

        return new S3ImageObject(response.readAllBytes(), Date.from(response.response().lastModified()), key, versionId);
    }

    @NotTransactionalServiceMethod
    public ResponseInputStream<GetObjectResponse> getObject(final String bucketName, final String key, final String versionId) {
        log.info("method=getObject getting from bucket={} with key={} and versionId={}", bucketName, key, versionId);

        final GetObjectRequest.Builder builder = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key);

        if (StringUtils.isNotBlank(versionId)) {
            builder.versionId(versionId);
        }

        return s3Client.getObject(builder.build());
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
            if (e.statusCode() == 404) {
                return false;
            }

            throw e;
        }
    }

    @NotTransactionalServiceMethod
    @Scheduled(fixedRate = 60 * 1000)
    public void logS3LimiterStats() {
        final int active = s3Limiter.getQueueLength(); // threads waiting
        final int acquired = 100 - s3Limiter.availablePermits(); // in-use permits
        final int available = s3Limiter.availablePermits();
        log.info("method=logS3LimiterStats inUse={} available={} waiting={}", acquired, available, active);
    }

    public static final class S3RateLimitExceededException extends RuntimeException {
        public S3RateLimitExceededException(final String message) {
            super(message);
        }
    }
}
