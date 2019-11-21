package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;

import fi.livi.digitraffic.tie.data.s3.AbstractCameraTestWithS3;

public class CameraImageS3WriterTest extends AbstractCameraTestWithS3 {
    private static final Logger log = LoggerFactory.getLogger(CameraImageS3WriterTest.class);

    @Autowired
    CameraImageS3Writer cameraImageS3Writer;

    private final long ts = Instant.now().toEpochMilli();

    @Test
    public void s3Write() throws IOException, ParseException {

        final String key = "C1234567.jpg";
        final List<Pair<String, byte[]>> versionIdImgPairs = writeImageVersions(key);

        final String versionedKey = CameraImageS3Writer.getVersionedKey(key);

        for (int i = 0; i < 5; i++) {
            final Pair<String, byte[]> versionIdImgPair = versionIdImgPairs.get(i);
            final String versionId = versionIdImgPair.getKey();
            final byte[] dataRead = readWeathercamS3DataVersion(versionedKey, versionId);
            Assert.assertArrayEquals("Data written differs from data read for versions", versionIdImgPair.getValue(), dataRead);
            final S3Object version = getS3ObjectVersionVersion(versionedKey, versionId);
            log.info("Version: {}", version.getObjectMetadata().getUserMetaDataOf(CameraImageS3Writer.LAST_MODIFIED_USER_METADATA_HEADER));
            Assert.assertEquals("Image version ts vs S3 image ts differs", getTimestampMillisForIndex(i)/1000, getLastModifiedSeconds(version));
        }

        // Test latest
        S3Object latest = s3.getObject(weathercamBucketName, key);
        final byte[] dataRead = latest.getObjectContent().readAllBytes();
        Assert.assertArrayEquals("Data written differs from data read for latest image", versionIdImgPairs.get(versionIdImgPairs.size()-1).getValue(), dataRead);
        Assert.assertEquals("Image ts vs read image ts differs", getTimestampMillisForIndex(4)/1000, getLastModifiedSeconds(latest));
    }

    @Test
    public void s3Delete() {
        final String key1 = "C1234561.jpg";
        final String key2 = "C1234562.jpg";

        final List<Pair<String, byte[]>> versionIdImgPairs1 = writeImageVersions(key1);
        final List<Pair<String, byte[]>> versionIdImgPairs2 = writeImageVersions(key2);

        final String versionedKey1 = CameraImageS3Writer.getVersionedKey(key1);
        final String versionedKey2 = CameraImageS3Writer.getVersionedKey(key2);

        versionIdImgPairs1.forEach(dw -> checkVersionObjectExistenceInS3(versionedKey1, dw.getKey(), true));
        for (Pair<String, byte[]> stringPair : versionIdImgPairs2) {
            checkVersionObjectExistenceInS3(versionedKey2, stringPair.getKey(), true);
        }
        checkObjectExistenceInS3(key1, true);
        checkObjectExistenceInS3(key2, true);

        cameraImageS3Writer.deleteImage(key1);

        checkObjectExistenceInS3(key1, false);
        checkObjectExistenceInS3(key2, true);
        checkObjectExistenceInS3(versionedKey1, false);
        checkObjectExistenceInS3(versionedKey2, true);

        // S3 won't delete versions. It only makes delete marker as latest version
        versionIdImgPairs1.forEach(dw -> checkVersionObjectExistenceInS3(versionedKey1, dw.getKey(), true));
        for (Pair<String, byte[]> dw : versionIdImgPairs2) {
            checkVersionObjectExistenceInS3(versionedKey2, dw.getKey(), true);
        }
    }

    private List<Pair<String, byte[]>> writeImageVersions(String key) {
        final List<Pair<String, byte[]>> dataWritten = new ArrayList<>();
        IntStream.range(0, 5).forEach(i -> {
            final byte[] img = new byte[] { (byte) i };
            final String versionId = cameraImageS3Writer.writeImage(img, img, key, getTimestampMillisForIndex(i));
            dataWritten.add(Pair.of(versionId, img));
        });
        return dataWritten;
    }

    private void checkObjectExistenceInS3(final String key, final boolean shouldExist) {
        if (shouldExist) {
            Assert.assertNotNull(s3.getObject(weathercamBucketName, key));
        } else {
            boolean exeption = false;
            try {
                Assert.assertNull(s3.getObject(weathercamBucketName, key));
            } catch (AmazonS3Exception e) {
                Assert.assertTrue(e.getMessage().contains("The specified key does not exist"));
                exeption = true;
            }
            Assert.assertTrue("Exception should have been thrown", exeption);
        }
    }

    private void checkVersionObjectExistenceInS3(String versionedKey, String versionId, boolean shouldExist) {
        if (shouldExist) {
            Assert.assertNotNull(getS3ObjectVersionVersion(versionedKey, versionId));
        } else {
            boolean exeption = false;
            try {
                Assert.assertNotNull(getS3ObjectVersionVersion(versionedKey, versionId));
            } catch (Exception e) {
                Assert.assertTrue(e.getMessage().contains("The specified key does not exist"));
                exeption = true;
            }
            Assert.assertTrue("Exception should have been thrown", exeption);
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
