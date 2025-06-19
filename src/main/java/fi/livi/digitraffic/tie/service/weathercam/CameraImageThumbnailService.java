package fi.livi.digitraffic.tie.service.weathercam;

import static fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties.getPresetIdFromImageName;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.service.aws.S3Service;
import net.coobird.thumbnailator.Thumbnails;


@Service
public class CameraImageThumbnailService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageThumbnailService.class);

    final double RESIZE_FACTOR = 0.3;

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    private String weathercamImageBucket;

    @Autowired
    private S3Service s3Service;

    @NotTransactionalServiceMethod
    public byte[] generateCameraImageThumbnail(final String imageName, final String versionId) throws IOException {
        final String imageKey =
                (StringUtils.isNotBlank(versionId)) ? getPresetIdFromImageName(imageName) + "-versions.jpg" :

                imageName;

        final S3Service.S3ImageObject image = s3Service.readImage(weathercamImageBucket, imageKey, versionId);

        try {
            final BufferedImage originalImage = readImageWithFallback(new ByteArrayInputStream(image.data()));
            final BufferedImage thumbnailImage = resizeImageByPercentage(originalImage, RESIZE_FACTOR);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnailImage, "jpg", baos);

            return baos.toByteArray();

        } catch (final Exception e) {
            final String imageHash = DigestUtils.sha256Hex(image.data());
            throw new ThumbnailGenerationError("Error generating thumbnail", imageName, versionId, image.lastModified(), imageHash, image.data().length / 1024, e);
        }
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
            log.warn("Failed to read image with Imaging, falling back to ImageIO...", e);
            image.reset();
            return ImageIO.read(image);
        }
    }

}
