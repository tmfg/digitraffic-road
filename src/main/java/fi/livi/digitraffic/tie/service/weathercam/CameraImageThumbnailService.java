package fi.livi.digitraffic.tie.service.weathercam;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.conf.RoadCacheConfiguration;
import fi.livi.digitraffic.tie.service.aws.S3Service;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties.getPresetIdFromImageName;


@Service
public class CameraImageThumbnailService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageThumbnailService.class);

    final double RESIZE_FACTOR = 0.3;

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    private String weathercamImageBucket;

    private final S3Service s3Service;
    private final ThreadPoolTaskExecutor thumbnailExecutor;
    private final CaffeineCache thumbnailCache;

    public CameraImageThumbnailService(final S3Service s3Service, final ThreadPoolTaskExecutor thumbnailExecutor, @Qualifier(RoadCacheConfiguration.CACHE_THUMBNAILS) final CaffeineCache thumbnailCache) {
        this.s3Service = s3Service;
        this.thumbnailExecutor = thumbnailExecutor;
        this.thumbnailCache = thumbnailCache;
    }

    @NotTransactionalServiceMethod
    public CompletableFuture<byte[]> generateCameraImageThumbnailAsync(
            final String imageName, final String versionId) {

        final String imageKey = StringUtils.isNotBlank(versionId) ?
                getPresetIdFromImageName(imageName) + "-versions.jpg" :
                imageName;

        return s3Service.readImageAsync(weathercamImageBucket, imageKey, versionId)
                .thenCompose(image -> {
                    final String cacheKey = image.getCacheKey();

                    // Use Spring CaffeineCache to cache CompletableFuture
                    // Use Caffeine async cache to compute or retrieve existing CompletableFuture
                    // This ensures that concurrent requests for the same image while it's being processed
                    // will wait on the same CompletableFuture instead of triggering multiple resizes.
                    // Async version of the cache
                    return thumbnailCache.get(cacheKey, () ->
                            CompletableFuture.supplyAsync(() -> {
                                final StopWatch sw = StopWatch.createStarted();
                                int originalSizeBytes = -1;
                                int thumbnailSizeBytes = -1;
                                try {
                                    final BufferedImage originalImage = readImageWithFallback(
                                            new ByteArrayInputStream(image.data())
                                    );

                                    originalSizeBytes = image.data().length;
                                    // Resize thumbnail
                                    final BufferedImage thumbnailImage =
                                            resizeImageByPercentage(originalImage, RESIZE_FACTOR);

                                    // Convert to byte[]
                                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ImageIO.write(thumbnailImage, "jpg", baos);
                                    final byte [] thumbnailBytes = baos.toByteArray();
                                    thumbnailSizeBytes = thumbnailBytes.length;
                                    return thumbnailBytes;

                                } catch (final Exception e) {
                                    final String imageHash = DigestUtils.sha256Hex(image.data());
                                    throw new ThumbnailGenerationError(
                                            "Error generating thumbnail",
                                            imageName,
                                            versionId,
                                            image.lastModified(),
                                            imageHash,
                                            (double) image.data().length / 1024,
                                            e
                                    );
                                } finally {
                                    sw.stop();
                                    log.info("method=resizeCameraImageThumbnail imageName={} versionId={} fromBytes={} toBytes={} tookMs={}",
                                            imageName, versionId, originalSizeBytes, thumbnailSizeBytes, sw.getTime());
                                }
                            }, thumbnailExecutor)
                    );
                });
    }

    public static BufferedImage resizeImageByPercentage(final BufferedImage originalImage, final double resizeFactor)
            throws IOException {
        return Thumbnails.of(originalImage)
                .size((int) (originalImage.getWidth() * resizeFactor), (int) (originalImage.getHeight() * resizeFactor))
                .asBufferedImage();
    }

    public static BufferedImage readImageWithFallback(final ByteArrayInputStream image) throws IOException {
        try {
            return Imaging.getBufferedImage(image);
        } catch (final Exception e) {
            log.warn("method=readImageWithFallback Failed to read image with Imaging, falling back to ImageIO...", e);
            image.reset();
            return ImageIO.read(image);
        }
    }
}
