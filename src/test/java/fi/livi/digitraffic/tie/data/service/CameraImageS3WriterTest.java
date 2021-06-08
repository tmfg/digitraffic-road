package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;

import fi.livi.digitraffic.tie.data.s3.AbstractCameraTestWithS3;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageS3Writer;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CameraImageS3WriterTest extends AbstractCameraTestWithS3 {
    private static final Logger log = LoggerFactory.getLogger(CameraImageS3WriterTest.class);

    @Autowired
    CameraImageS3Writer cameraImageS3Writer;

    private final long ts = Instant.now().toEpochMilli();

    /**
     * Tests that image and it versions are written in S3 and the data is correct.
     */
    @Test
    public void s3Write() throws IOException, ParseException {
        final String key = "C1234567.jpg";
        final int versionCount = 5;
        final List<Pair<String, byte[]>> versionIdImgDataPairs = writeImageVersions(key, versionCount);

        final String versionedKey = CameraImageS3Writer.getVersionedKey(key);

        // Check that writen versions image data and last modified metadata are correct for all versions
        for (int i = 0; i < versionCount; i++) {
            final Pair<String, byte[]> versionIdImgDataPair = versionIdImgDataPairs.get(i);
            final String versionId = versionIdImgDataPair.getKey();
            final byte[] dataFromS3 = readWeathercamS3DataVersion(versionedKey, versionId);
            assertArrayEquals(versionIdImgDataPair.getValue(), dataFromS3, "Image data read from S3 differs from expected image data for the version");
            final S3Object version = getS3ObjectVersionVersion(versionedKey, versionId);
            log.info("Version: {}", version.getObjectMetadata().getUserMetaDataOf(CameraImageS3Writer.LAST_MODIFIED_USER_METADATA_HEADER));
            assertEquals(getTimestampMillisForIndex(i)/1000, getLastModifiedSeconds(version), "Image version last modified timestamp differs from the expected");
        }

        // Test latest image data and last modified metadata
        final S3Object latest = amazonS3.getObject(weathercamBucketName, key);
        final byte[] dataFromS3 = latest.getObjectContent().readAllBytes();
        assertArrayEquals(versionIdImgDataPairs.get(versionIdImgDataPairs.size()-1).getValue(), dataFromS3,
            "Image data read from S3 differs from expected image data for the latest image");
        assertEquals(getTimestampMillisForIndex(4)/1000, getLastModifiedSeconds(latest), "Latest image last modified timestamp differs from the expected");
    }

    /**
     * Test that two images (key1, key2) are written to s3 and other (key1) is then deleted.
     * Versions won't get deleted in S3 as it only makes delete marker as latest version.
     */
    @Test
    public void s3Delete() {
        final String key1 = "C1234561.jpg";
        final String key2 = "C1234562.jpg";
        final int versionCount = 5;
        final List<Pair<String, byte[]>> versionIdImgPairs1 = writeImageVersions(key1, versionCount);
        final List<Pair<String, byte[]>> versionIdImgPairs2 = writeImageVersions(key2, versionCount);

        // Create 5 image versions for both images
        final String versionedKey1 = CameraImageS3Writer.getVersionedKey(key1);
        final String versionedKey2 = CameraImageS3Writer.getVersionedKey(key2);

        // Check all 5 versions exists for both keys
        versionIdImgPairs1.forEach(dw -> checkVersionObjectExistenceInS3(versionedKey1, dw.getKey(), true));
        versionIdImgPairs2.forEach(dw -> checkVersionObjectExistenceInS3(versionedKey2, dw.getKey(), true));
        // Check latest exists for both keys
        checkObjectExistenceInS3(key1, true);
        checkObjectExistenceInS3(key2, true);

        // Delete key1 -> only delete markes as they are versioned
        final CameraImageS3Writer.DeleteInfo di = cameraImageS3Writer.deleteImage(key1);
        assertTrue(di.isFileExists());
        assertTrue(di.isDeleteSuccess());
        assertTrue(di.isSuccess());

        // Check that deleted object delete marker is in place
        checkObjectExistenceInS3(key1, false); // latest version = delete marker
        checkObjectExistenceInS3(key2, true); // should exist
        checkObjectExistenceInS3(versionedKey1, false); // latest version = delete marker
        checkObjectExistenceInS3(versionedKey2, true); // should exist

        // S3 won't delete versions. It only makes delete marker as latest version. Check versions still exists
        versionIdImgPairs1.forEach(dw -> checkVersionObjectExistenceInS3(versionedKey1, dw.getKey(), true));
        versionIdImgPairs2.forEach(dw -> checkVersionObjectExistenceInS3(versionedKey2, dw.getKey(), true));
    }

    private List<Pair<String, byte[]>> writeImageVersions(final String key, final int versionCount) {
        final List<Pair<String, byte[]>> dataWritten = new ArrayList<>();
        IntStream.range(0, versionCount).forEach(i -> {
            final byte[] img = new byte[] { (byte) i };
            final String versionId = cameraImageS3Writer.writeImage(img, img, key, getTimestampMillisForIndex(i));
            dataWritten.add(Pair.of(versionId, img));
        });
        return dataWritten;
    }

    private void checkObjectExistenceInS3(final String key, final boolean shouldExist) {
        if (shouldExist) {
            assertNotNull(amazonS3.getObject(weathercamBucketName, key));
        } else {
            boolean exeption = false;
            try {
                assertNull(amazonS3.getObject(weathercamBucketName, key));
            } catch (AmazonS3Exception e) {
                assertTrue(e.getMessage().contains("The specified key does not exist"));
                exeption = true;
            }
            assertTrue(exeption, "Exception should have been thrown");
        }
    }

    private void checkVersionObjectExistenceInS3(String versionedKey, String versionId, boolean shouldExist) {
        if (shouldExist) {
            assertNotNull(getS3ObjectVersionVersion(versionedKey, versionId));
        } else {
            boolean exeption = false;
            try {
                assertNotNull(getS3ObjectVersionVersion(versionedKey, versionId));
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("The specified key does not exist"));
                exeption = true;
            }
            assertTrue(exeption, "Exception should have been thrown");
        }
    }

    private long getTimestampMillisForIndex(int index) {
        return ts + (index * 1000);
    }

    private long getLastModifiedSeconds(final S3Object s3Object) throws ParseException {
        final String lastModified = s3Object.getObjectMetadata().getUserMetaDataOf(CameraImageS3Writer.LAST_MODIFIED_USER_METADATA_HEADER);
        final Date lastModifiedS3Date = s3Object.getObjectMetadata().getLastModified();
        final Date time = CameraImageS3Writer.LAST_MODIFIED_FORMAT.parse(lastModified);
        log.info("User meta : {} S3 meta: {}", time.toInstant(), lastModifiedS3Date.toInstant());
        return time.toInstant().getEpochSecond();
    }
}
