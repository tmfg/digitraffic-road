package fi.livi.digitraffic.tie.service.weathercam;

import static fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties.getPresetIdFromImageName;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import net.coobird.thumbnailator.Thumbnails;

@Service
public class CameraImageThumbnailService {

    private static final Logger log = LoggerFactory.getLogger(CameraImageThumbnailService.class);

    final double RESIZE_FACTOR = 0.3;

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    private String weathercamImageBucket;

    @NotTransactionalServiceMethod
    public byte[] generateCameraImageThumbnail(final String imageName, final String versionId) throws IOException {
        final String imageKey =
                (StringUtils.isNotBlank(versionId)) ? getPresetIdFromImageName(imageName) + "-versions.jpg" :

                imageName;

        final byte[] image = readImage(weathercamImageBucket, imageKey, versionId);

        final BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(image));
        final BufferedImage thumbnailImage = resizeImageByPercentage(originalImage, RESIZE_FACTOR);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnailImage, "jpg", baos);
        final byte[] thumbnailData = baos.toByteArray();
        return thumbnailData;
    }

    private BufferedImage resizeImageByPercentage(final BufferedImage originalImage, final double percentage)
            throws IOException {
        return Thumbnails.of(originalImage)
                .size((int) (originalImage.getWidth() * percentage), (int) (originalImage.getHeight() * percentage))
                .asBufferedImage();
    }

    private byte[] readImage(final String bucketName, final String key, final String versionId) throws IOException {
        final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        log.info("method=readImage getting from bucket={} with key={} and versionId={}", bucketName, key, versionId);
        final GetObjectRequest request =
                (StringUtils.isNotBlank(versionId)) ? new GetObjectRequest(bucketName, key, versionId) :
                new GetObjectRequest(bucketName, key);
        final S3Object s3Object = s3Client.getObject(request);
        final S3ObjectInputStream s3InputStream = s3Object.getObjectContent();

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = s3InputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        s3InputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

}
