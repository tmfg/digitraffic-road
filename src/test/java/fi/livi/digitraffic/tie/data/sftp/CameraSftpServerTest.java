package fi.livi.digitraffic.tie.data.sftp;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.test.context.TestPropertySource;

import com.amazonaws.services.s3.model.S3Object;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.data.service.CameraImageS3Writer;
import fi.livi.digitraffic.tie.data.service.CameraImageUpdateService;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdateService;

@TestPropertySource( properties = { "camera-image-uploader.imageUpdateTimeout=500" })
public class CameraSftpServerTest extends AbstractCameraTestWithS3 {
    private static final Logger log = LoggerFactory.getLogger(CameraSftpServerTest.class);

    private static final String RESOURCE_IMAGE_SUFFIX = "image.jpg";
    private static final String IMAGE_DIR = "lotju/kuva/";
    private static final int TEST_UPLOADS = 10;

    @Autowired
    private SessionFactory  sftpSessionFactory;

    @Autowired
    private CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    private CameraImageUpdateService cameraImageUpdateService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private CameraStationUpdateService cameraStationUpdateService;

    @Autowired
    private CameraPresetService cameraPresetService;

    @Value("${camera-image-uploader.sftp.uploadFolder}")
    private String sftpUploadFolder;

    @Autowired
    CameraImageS3Writer cameraImageS3Writer;

    private ArrayList<KuvaProtos.Kuva> kuvas = new ArrayList<>();

    @Before
    public void setUpTestData() throws IOException, SftpException {

        log.info("Init test data");

        // Init minimum TEST_UPLOADS non obsolete presets
        List<CameraPreset> nonObsoleteCameraPresets = cameraPresetService.findAllPublishableCameraPresets();
        log.info("Non obsolete CameraPresets before " + nonObsoleteCameraPresets.size());
        Map<Long, CameraPreset> cameraPresets = cameraPresetService.findAllCameraPresetsMappedByLotjuId();

        int missingCount = TEST_UPLOADS - nonObsoleteCameraPresets.size();
        Iterator<CameraPreset> iter = cameraPresets.values().iterator();
        while (missingCount > 0 && iter.hasNext()) {
            CameraPreset cp = iter.next();
            RoadStation rs = cp.getRoadStation();
            if ( rs.isObsolete() || cp.isObsolete() || !rs.isPublic() || !cp.isPublic() || !cp.isPublicExternal()) {
                missingCount--;
            }
            rs.unobsolete();
            rs.setPublic(true);
            cp.unobsolete();
            cp.setPublicInternal(true);
            cp.setPublicExternal(true);
        }

        // Active presets
        List<CameraPreset> activePresets = cameraPresetService.findAllPublishableCameraPresets();
        nonObsoleteCameraPresets = activePresets.subList(0, Math.min(TEST_UPLOADS, activePresets.size()));
        log.info("Non obsolete CameraPresets for testing " + nonObsoleteCameraPresets.size());

        // Missing presets in db, images should get deleted
        int i = 0;
        final ArrayList<CameraPreset> missingCameraPresets = new ArrayList<>();
        while (i < 5) {
            i++;
            missingCameraPresets.add(generateDummyPreset());
        }

        Session session = this.sftpSessionFactory.getSession();
        if (!session.exists(sftpUploadFolder)) {
            session.mkdir(sftpUploadFolder);
        }

        List<CameraPreset> cps = Stream.concat(nonObsoleteCameraPresets.stream(), missingCameraPresets.stream()).collect(Collectors.toList());
        int count = 0;
        for (CameraPreset cp : cps) {
            count++;
            final int imageNumber = (count % 5)+1;

            log.info("Load image resource={}{} for presetId={}", imageNumber, RESOURCE_IMAGE_SUFFIX, cp.getPresetId());
            final Resource resource = resourceLoader.getResource("classpath:" + IMAGE_DIR + imageNumber + RESOURCE_IMAGE_SUFFIX);
            final File imageFile = resource.getFile();
            final byte[] bytes = FileUtils.readFileToByteArray(imageFile);
            final KuvaProtos.Kuva kuva = createKuvaDataAndHttpStub(cp, bytes, 0);
            kuvas.add(kuva);

            // Upload missing presets images to server
            if (cp.getPresetId().startsWith("X")) {
                final String imageFullPath = getSftpPath(kuva);
                log.info("Write image to sftp that should be deleted by update sftpPath={}", imageFullPath);
                session.write(new ByteArrayInputStream(bytes), imageFullPath);
                ((ChannelSftp) session.getClientInstance()).setMtime(imageFullPath, (int ) (kuva.getAikaleima() / 1000) );
                Session otherSession = this.sftpSessionFactory.getSession();
                assertTrue("Image not found on sftp server", otherSession.exists(imageFullPath));
                otherSession.close();
                cameraImageS3Writer.writeImage(bytes, bytes, getImageFilename(kuva.getNimi()), kuva.getAikaleima());
            }
        }
        session.close();
    }


    @Test
    public void testDeleteNotPublishableCameraImages() throws Exception {

        cameraDataUpdateService.updateCameraData(kuvas);

        KuvaProtos.Kuva kuvaToDelete = kuvas.stream().filter(k -> k.getNimi().startsWith("C")).findFirst().get();
        CameraPreset presetToDelete =
                cameraPresetService.findAllPublishableCameraPresets().stream().filter(cp -> cp.getPresetId().equals(kuvaToDelete.getNimi()))
                        .findFirst().get();

        try (final Session session = this.sftpSessionFactory.getSession()) {
            assertTrue("Publishable preset image should exist: " + presetToDelete, session.exists(getSftpPath(presetToDelete.getPresetId())));
            final SftpATTRS stat = ((ChannelSftp) session.getClientInstance()).stat(getSftpPath(presetToDelete.getPresetId()));
            log.info("Kuva timestamp {} vs image timestamp {}", Instant.ofEpochMilli(kuvaToDelete.getAikaleima()), Instant.ofEpochSecond(stat.getMTime()));
            Assert.assertEquals(kuvaToDelete.getAikaleima()/1000, stat.getMTime());
        }

        assertTrue("Publishable preset image should exist: " + presetToDelete, s3.doesObjectExist(weathercamBucketName, getImageFilename(presetToDelete.getPresetId())));
        S3Object s3Object = s3.getObject(weathercamBucketName, getImageFilename(presetToDelete.getPresetId()));
        long lastModifiedSecondsFromEpoch = getLastModifiedSeconds(s3Object);

        log.info("Kuva timestamp {} vs S3 image timestamp {}", Instant.ofEpochMilli(kuvaToDelete.getAikaleima()), Instant.ofEpochSecond(lastModifiedSecondsFromEpoch));
        Assert.assertEquals(kuvaToDelete.getAikaleima()/1000, lastModifiedSecondsFromEpoch);


        presetToDelete.setPublicExternal(false);
        entityManager.flush();

        cameraImageUpdateService.deleteAllImagesForNonPublishablePresets();

        try (final Session session = this.sftpSessionFactory.getSession()) {
            assertFalse("Not publishable preset image should not exist", session.exists(getSftpPath(presetToDelete.getPresetId())));
        }
        assertFalse("Not publishable preset image should not exist in S3", s3.doesObjectExist(weathercamBucketName, getImageFilename(presetToDelete.getPresetId())));
    }

    @Test
    public void testS3Versioning() throws IOException {

        final String key = "C1234567.jpg";
        long ts = Instant.now().getEpochSecond();
        final List<Pair<String, byte[]>> dataWritten = new ArrayList<>();
        IntStream.range(0, 5).forEach(i -> {
            final byte[] img = new byte[] { (byte) i };
            final String versionId = cameraImageS3Writer.writeImage(img, img, key, ts + i);
            dataWritten.add(Pair.of(versionId, img));
        });

        dataWritten.forEach(p -> {
            final byte[] dataRead = readWeathercamS3DataVersion(CameraImageS3Writer.getVersionedKey(key), p.getKey());
            Assert.assertArrayEquals("Data written differs from data read for versions", p.getValue(), dataRead);
        });
        // Test latest
        S3Object latest = s3.getObject(weathercamBucketName, key);
        final byte[] dataRead = latest.getObjectContent().readAllBytes();
        Assert.assertArrayEquals("Data written differs from data read for latest image", dataWritten.get(dataWritten.size()-1).getValue(), dataRead);
    }

    private KuvaProtos.Kuva createKuvaDataAndHttpStub(final CameraPreset cp, final byte[] data, final int httpResponseDelay) {
        KuvaProtos.Kuva.Builder kuva = KuvaProtos.Kuva.newBuilder();
        kuva.setNimi(cp.getPresetId());
        kuva.setAikaleima(Instant.now().toEpochMilli());
        kuva.setAsemanNimi("Suomenmaa " + RandomUtils.nextLong(1000, 10000));
        kuva.setEsiasennonNimi("Esiasento" + RandomUtils.nextLong(1000, 10000));
        kuva.setEsiasentoId(cp.getLotjuId() != null ? cp.getLotjuId() : RandomUtils.nextLong(10000, 100000));
        kuva.setEtaisyysTieosanAlusta(RandomUtils.nextInt(0, 99999));
        kuva.setJulkinen(true);
        kuva.setKameraId(Long.parseLong(cp.getCameraId().substring(1)));
        kuva.setLiviId("" + RandomUtils.nextLong(0, 99999));
        kuva.setKuvaId(Long.parseLong(cp.getPresetId().substring(1)));

        if (cp.getRoadStation().getRoadAddress() != null) {
            kuva.setTienumero(cp.getRoadStation().getRoadAddress().getRoadNumber());
            kuva.setTieosa(cp.getRoadStation().getRoadAddress().getRoadSection());
        }
        //kuva.setUrl(getImageUrl(cp.getPresetId()));
        kuva.setXKoordinaatti("12345.67");
        kuva.setYKoordinaatti("23456.78");

        if (data != null) {
            createHttpResponseStubFor(Long.parseLong(cp.getPresetId().substring(1)), data, httpResponseDelay);
        }

        return kuva.build();
    }

    private void createHttpResponseStubFor(final Long imageId, final byte[] data, final int httpResponseDelay) {
        //imageFilesMap.put(presetId, data);
        final String url = getImageUrlPath(imageId);
        log.info("Create mock with url={}", url);
        stubFor(get(urlEqualTo(getImageUrlPath(imageId)))
                .willReturn(aResponse().withBody(data)
                        .withHeader("Content-Type", "image/jpeg")
                        .withStatus(200)
                        .withFixedDelay(httpResponseDelay)));
    }

    private String getImageFilename(final String presetId) {
        return presetId + ".jpg";
    }

    private String getImageFilename(final KuvaProtos.Kuva kuva) {
        return getImageFilename(kuva.getNimi());
    }

    private long getLastModifiedSeconds(final S3Object s3Object) throws ParseException {
        final String lastModified = s3Object.getObjectMetadata().getUserMetaDataOf(CameraImageS3Writer.LAST_MODIFIED_USER_METADATA_HEADER);
        final Date lastModifiedS3Date = s3Object.getObjectMetadata().getLastModified();
        Date time = CameraImageS3Writer.LAST_MODIFIED_FORMAT.parse(lastModified);
        log.info("User meta : {} S3 meta: {}", time.toInstant(), lastModifiedS3Date.toInstant());
        return time.toInstant().getEpochSecond();
    }
}
