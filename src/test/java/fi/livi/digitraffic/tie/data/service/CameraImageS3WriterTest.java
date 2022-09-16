package fi.livi.digitraffic.tie.data.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.AmazonS3;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageS3Writer;

public class CameraImageS3WriterTest extends AbstractDaemonTest {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private CameraImageS3Writer cameraImageS3Writer;

    @Test
    public void cameraImageS3WriterKeyNameTest() {
        final String key = "C1234567.jpg";
        final byte[] imgData = new byte[] { (byte) 3 };
        final long nowEpochMilli = Instant.now().toEpochMilli();

        final String regex = new WeathercamS3Properties("fakeBucket","fakeRegion",1,"fakeBaseUrl").getS3WeathercamKeyRegexp();
        final String errorMessage = String.format("S3 key should match regexp format \"%s\" ie. \"C1234567.jpg\" but was \"%s\"", regex, key);

        assertThrows(RuntimeException.class, () -> {
            cameraImageS3Writer.writeCurrentImage(imgData, "BAD_KEY", nowEpochMilli);
        }, errorMessage);

        Assertions.assertDoesNotThrow(() -> cameraImageS3Writer.writeCurrentImage(imgData, key, nowEpochMilli));
    }

    @Test
    public void cameraS3WriterDeleteTest() {
        final String imgKey = "C0650802.jpg";
        final String versionedKey = CameraImageS3Writer.getVersionedKey(imgKey);
        Mockito.when(amazonS3.doesObjectExist(Mockito.anyString(), Mockito.eq(imgKey))).thenReturn(true);
        Mockito.when(amazonS3.doesObjectExist(Mockito.anyString(), Mockito.eq(versionedKey))).thenReturn(false);

        final CameraImageS3Writer.DeleteInfo deleteInfo = cameraImageS3Writer.deleteImage(imgKey);
        Assertions.assertTrue(deleteInfo.isDeleteSuccess());
    }
}
