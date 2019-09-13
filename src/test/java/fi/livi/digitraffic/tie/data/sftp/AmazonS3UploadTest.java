package fi.livi.digitraffic.tie.data.sftp;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;

import com.amazonaws.services.s3.AmazonS3;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.data.service.CameraImageS3Writer;

@Ignore("Manual testing purposes only")
@TestPropertySource( properties = { "camera-image-uploader.imageUpdateTimeout=500" })
public class AmazonS3UploadTest extends AbstractDaemonTestWithoutS3 {
    private static final Logger log = LoggerFactory.getLogger(AmazonS3UploadTest.class);

    private static final String RESOURCE_IMAGE_SUFFIX = "image.jpg";
    private static final String IMAGE_DIR = "lotju/kuva/";

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private AmazonS3 amazonS3Client;

    @Autowired
    CameraImageS3Writer cameraImageS3Writer;

    @Value("${dt.amazon.s3.weathercamBucketName}")
    private String weathercamBucketName;

    @Ignore("Test is ignored as a demonstration")
    @Test
    public void testSendCameraImageVersionsToAwsS3() throws IOException, InterruptedException {

        log.info("Send images to S3 bucket {}", weathercamBucketName);

        int i = 0;
        while (i < 5) {
            i++;
            final Resource resource = resourceLoader.getResource("classpath:" + IMAGE_DIR + i + RESOURCE_IMAGE_SUFFIX);
            final File imageFile = resource.getFile();
            final byte[] bytes = FileUtils.readFileToByteArray(imageFile);

            cameraImageS3Writer.writeImage(bytes, bytes,"C0650802.jpg", (int) (Instant.now().minusSeconds(60).toEpochMilli() / 1000));
            Thread.sleep(2000);
        }

    }


}
