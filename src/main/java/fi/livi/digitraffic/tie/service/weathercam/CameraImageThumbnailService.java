package fi.livi.digitraffic.tie.service.weathercam;

import static fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties.getFullImageVersionS3Key;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

@Component
public class CameraImageThumbnailService {

    final double RESIZE_PERCENTAGE = 0.10;

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    private String weathercamImageBucket;

    public byte[] generateCameraImageThumbnail(final String imageName, final String versionId) throws IOException {
        final String imageKey = versionId != null ? getFullImageVersionS3Key(imageName, versionId) : imageName;

        final byte[] image = readImage(weathercamImageBucket, imageKey);

        final BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(image));
        final BufferedImage thumbnailImage = resizeImageByPercentage(originalImage, RESIZE_PERCENTAGE);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnailImage, "jpg", baos);
        final byte[] thumbnailData = baos.toByteArray();
        return thumbnailData;
    }

    private BufferedImage resizeImageByPercentage(final BufferedImage originalImage, final double percentage) {
        final int width = (int) (originalImage.getWidth() * percentage);
        final int height = (int) (originalImage.getHeight() * percentage);
        final BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    private byte[] readImage(final String bucketName, final String key) throws IOException {
        final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        final S3Object s3Object = s3Client.getObject(bucketName, key);
        final S3ObjectInputStream s3InputStream = s3Object.getObjectContent();

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = s3InputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        s3InputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

}
