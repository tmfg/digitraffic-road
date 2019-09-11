package fi.livi.digitraffic.tie.data.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.amazonaws.services.s3.model.S3Object;
import com.jcraft.jsch.SftpException;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.data.sftp.AbstractSftpTest;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetHistoryService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

public class CameraImageUpdateServiceTestWithS3 extends AbstractSftpTest {

    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateServiceTestWithS3.class);

    @MockBean
    private CameraImageReader cameraImageReader;

    @MockBean
    private CameraImageWriter cameraImageWriter;

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraImageUpdateService service;

    @Autowired
    private CameraPresetHistoryService cameraPresetHistoryService;

    @Test
    public void versionHistoryUpdated() throws IOException, SftpException {

        final ZonedDateTime initialLastModified = DateHelper.toZonedDateTimeAtUtc(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
        Set<String> presetIds = new HashSet<>();

        cameraPresetService.findAllPublishableCameraPresets().stream().limit(2).forEach(cp -> {

            try {
                when(cameraImageReader.readImage(eq(cp.getLotjuId()), any()))
                    .thenReturn(getAsByteArray(1),
                                getAsByteArray(2),
                                getAsByteArray(3),
                                getAsByteArray(4),
                                getAsByteArray(5));
            } catch (IOException e) {
                throw new RuntimeException();
            }

            presetIds.add(cp.getPresetId());
            IntStream.range(1, 6).forEach(loopIndex -> {
                // All even kuvaIds are public
                final boolean isPublic = isPublic(loopIndex);
                final KuvaProtos.Kuva kuva = createKuva(initialLastModified.plusMinutes(loopIndex), cp.getPresetId(), cp.getLotjuId(), isPublic);
                service.handleKuva(kuva);

                // Check that latest image data is correct after update
                final String key = cp.getPresetId() + ".jpg";
                // Image bytes equals with actual (public) or noise image (hidden)
                final byte[] image = readWeathercamS3Data(key);
                assertBytes(isPublic ? getAsByteArray(loopIndex) : service.getNoiseImage(), image);
                // S3 Object lastmodified is correct
                final S3Object imageS3Object = readWeathercamS3Object(key);
                assertLastModified(initialLastModified.plusMinutes(loopIndex), imageS3Object);
            });
        });

        presetIds.forEach(presetId -> {
            cameraPresetHistoryService.findAllByPresetId(presetId).forEach(h -> log.info(h.toString()));
            final AtomicInteger loopIndex = new AtomicInteger(1);

            cameraPresetHistoryService.findAllByPresetId(presetId).forEach(h -> {
                log.info(h.toString());

                // Check history data
                // All even versions are public
                final boolean shouldBePublic = isPublic(loopIndex.get());
                Assert.assertEquals(h.getPublishable(), shouldBePublic);
                // Last modified should equal
                final ZonedDateTime historyLastModified = h.getLastModified();
                Assert.assertEquals(initialLastModified.plusMinutes(loopIndex.get()), historyLastModified);

                // Check S3 image versioned data
                final byte[] image = readWeathercamS3DataVersion( CameraImageS3Writer.getVersionedKey(h.getPresetId()), h.getVersionId());
                Assert.assertEquals(image.length, h.getSize().intValue());
                // S3 image data should be equal with written dat. Hidden images also has real data, no noise image.
                assertBytes(getAsByteArray(loopIndex.get()), image);

                // S3 Object last modified should be equals with history
                final S3Object imageObject = readWeathercamS3ObjectVersion(h.getPresetId() + ".jpg", h.getVersionId());
                assertLastModified(historyLastModified, imageObject);

                // update loop index
                loopIndex.getAndSet(loopIndex.get() + 1);
            });
        });

        verify(cameraImageReader, times(10)).readImage(anyLong(), any());
        verify(cameraImageWriter, times(10)).writeImage(any(), any(), anyLong());
    }

    private void assertLastModified(final ZonedDateTime expected, final S3Object imageObject) {
        final String s3LastMofidiedHeader = imageObject.getObjectMetadata().getUserMetaDataOf(CameraImageS3Writer.LAST_MODIFIED_USER_METADATA_HEADER);
        final String historyLastModifiedHeader = CameraImageS3Writer.getInLastModifiedHeaderFormat(expected.toInstant());
        Assert.assertEquals(historyLastModifiedHeader, s3LastMofidiedHeader);
    }

    /**
     * @return true if parameter is even
     */
    private boolean isPublic(final int i) {
        return i % 2 == 0;
    }

    private void assertBytes(byte[] bytes1, byte[] bytes2) {
        Assert.assertArrayEquals(bytes1 , bytes2);
    }

    private byte[] getAsByteArray(int i) {
        byte[] arr = new byte[i];
        Arrays.fill(arr, (byte)i);
        return arr;
    }

    private KuvaProtos.Kuva createKuva(final ZonedDateTime lastModified, final String presetId, final long esiasentoId, final boolean isPublic) {
        return KuvaProtos.Kuva.newBuilder()
            .mergeFrom(KuvaProtos.Kuva.getDefaultInstance())
            .setAikaleima(lastModified.toInstant().toEpochMilli())
            .setEsiasentoId(esiasentoId)
            .setNimi(presetId + RandomStringUtils.randomAlphanumeric(10))
            .setJulkinen(isPublic)
            .setKuvaId(esiasentoId)
            .setKameraId(1)
            .setEsiasennonNimi("Esiasento " + presetId)
            .setAsemanNimi("Asema " + presetId)
            .build();
    }
}
