package fi.livi.digitraffic.tie.data.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;

import fi.livi.digitraffic.tie.service.aws.S3Service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageS3Writer;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class CameraImageS3WriterTest extends AbstractDaemonTest {

    @MockitoBean
    private S3Service s3Service;

    @Autowired
    private CameraImageS3Writer cameraImageS3Writer;

    @Test
    public void cameraImageS3WriterKeyNameTest() {
        final String key = "C1234567.jpg";
        final byte[] imgData = new byte[] { (byte) 3 };
        final long nowEpochMilli = Instant.now().toEpochMilli();

        final String regex = new WeathercamS3Properties("fakeBucket","fakeRegion",1,"fakeBaseUrl").getS3WeathercamKeyRegexp();
        final String errorMessage = String.format("S3 key should match regexp format \"%s\" ie. \"C1234567.jpg\" but was \"%s\"", regex, key);

        assertThrows(RuntimeException.class, () -> cameraImageS3Writer.writeCurrentImage(imgData, "BAD_KEY", nowEpochMilli), errorMessage);

        Assertions.assertDoesNotThrow(() -> cameraImageS3Writer.writeCurrentImage(imgData, key, nowEpochMilli));
    }

    @Test
    public void cameraS3WriterDeleteTest() {
        final String imgKey = "C0650802.jpg";
        final String versionedKey = CameraImageS3Writer.getVersionedKey(imgKey);

        when(s3Service.doesObjectExist(anyString(), eq(imgKey))).thenReturn(true);
        when(s3Service.doesObjectExist(anyString(), eq(versionedKey))).thenReturn(false);

        final CameraImageS3Writer.DeleteInfo deleteInfo = cameraImageS3Writer.deleteImage(imgKey);
        Assertions.assertTrue(deleteInfo.isDeleteSuccess());
    }
}
