package fi.livi.digitraffic.tie.service.weathercam;

import static fi.livi.digitraffic.tie.helper.DateHelper.getZonedDateTimeNowAtUtc;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.tie.service.lotju.AbstractMultiDestinationProviderTest;

public class CameraImageReaderTest extends AbstractMultiDestinationProviderTest {

    final byte[] img1 = new byte[] { (byte) 1 };
    final byte[] img2 = new byte[] { (byte) 2 };

    private CameraImageReader cameraImageReader;

    @BeforeEach
    public void initCameraImageReader() {
        cameraImageReader = new CameraImageReader(1000, 1000, createLotjuMetadataProperties());
    }

    @Test
    public void firstHealthOk() throws IOException {
        // Health response from server OK
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());
        // Data request goes to server 1
        final int id = randomId();
        final String presetId = randomPresetId();
        serverWhenRequestUrlThenReturn(wireMockServer1, dataPath + "/" +  id, OK, img1);
        final ImageUpdateInfo info = new ImageUpdateInfo(presetId, getZonedDateTimeNowAtUtc());
        final byte[] img = cameraImageReader.readImage(id, info);

        assertArrayEquals(img1, img);
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(0);
    }

    @Test
    public void firstHealthNotOk() throws IOException {
        // Health response from server OK
        server1WhenRequestHealthThenReturn(INTERNAL_SERVER_ERROR, NOT_OK_RESPONSE_CONTENT);
        server2WhenRequestHealthThenReturn(OK, getOkResponseString());
        // Data request goes to server 1
        final int id = randomId();
        final String presetId = randomPresetId();
        serverWhenRequestUrlThenReturn(wireMockServer1, dataPath + "/" +  id, OK, img1);
        serverWhenRequestUrlThenReturn(wireMockServer2, dataPath + "/" +  id, OK, img2);
        final ImageUpdateInfo info = new ImageUpdateInfo(presetId, getZonedDateTimeNowAtUtc());
        final byte[] img = cameraImageReader.readImage(id, info);

        assertArrayEquals(img2, img);
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
        verifyServer1DataCount(0);
        verifyServer2DataCount(1);
    }

    @Test
    public void firstHealthOkDataNotOk() throws IOException {
        // Health response from server OK
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());
        server2WhenRequestHealthThenReturn(OK, getOkResponseString());
        // Data request goes to server 1
        final int id = randomId();
        final String presetId = randomPresetId();
        serverWhenRequestUrlThenReturn(wireMockServer1, dataPath + "/" +  id, INTERNAL_SERVER_ERROR, (String) null);
        serverWhenRequestUrlThenReturn(wireMockServer2, dataPath + "/" +  id, OK, img2);
        final ImageUpdateInfo info = new ImageUpdateInfo(presetId, getZonedDateTimeNowAtUtc());
        try {
            cameraImageReader.readImage(id, info);
            fail("First request to server1 should fail and throw exception");
        } catch (final Exception e) {
            // empty
        }
        final byte[] img = cameraImageReader.readImage(id, info);

        assertArrayEquals(img2, img);
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
        verifyServer1DataCount(1);
        verifyServer2DataCount(1);
    }

    private String randomPresetId() {
        return "C" + RandomUtils.nextInt(1000000, 10000000);

    }

    private int randomId() {
        return RandomUtils.nextInt();
    }
}
