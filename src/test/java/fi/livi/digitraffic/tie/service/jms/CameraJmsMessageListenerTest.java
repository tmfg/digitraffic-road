package fi.livi.digitraffic.tie.service.jms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import jakarta.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.transaction.TestTransaction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.jms.marshaller.KuvaMessageMarshaller;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateManager;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;

@Disabled("Does not execute properly in CI server")
public class CameraJmsMessageListenerTest extends AbstractDaemonTest {
    private static final Logger log = LoggerFactory.getLogger(CameraJmsMessageListenerTest.class);

    private static final String IMAGE_SUFFIX = "image.jpg";
    private static final String IMAGE_DIR = "lotju/kuva/";

    @Value("${metadata.server.path.health:#{null}}")
    private String healthPath;

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraImageUpdateManager cameraImageUpdateManager;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    private String bucketName;

    @Value("${metadata.server.path.image}")
    private String LOTJU_IMAGE_PATH;

    private Map<String, byte[]> imageFilesMap = new HashMap<>();
    @BeforeEach
    public void initData() throws IOException {
        log.info("LOTJU_IMAGE_PATH={}", LOTJU_IMAGE_PATH);
        log.info("healthPath={}", healthPath);
        createHealthOKStubFor(healthPath);

        int i = 5;
        while (i > 0) {
            final String imageName = i + IMAGE_SUFFIX;
            final Resource resource = resourceLoader.getResource("classpath:" + IMAGE_DIR + imageName);
            final File imageFile = resource.getFile();
            final byte[] bytes = FileUtils.readFileToByteArray(imageFile);

            imageFilesMap.put(imageName, bytes);
            i--;
        }

        final List<CameraPreset> nonObsoleteCameraPresets = cameraPresetService.findAllPublishableCameraPresets();
        log.info("Non obsolete CameraPresets before:{}", nonObsoleteCameraPresets.size());

        final Map<Long, CameraPreset> cameraPresets = cameraPresetService.findAllCameraPresetsMappedByLotjuId();
        log.info("All camera presets size cameraPresetsCount={}", cameraPresets.size());

        int missingMin = 1000 - nonObsoleteCameraPresets.size();
        final Iterator<CameraPreset> iter = cameraPresets.values().iterator();

        while (missingMin > 0 && iter.hasNext()) {
            final CameraPreset cp = iter.next();
            final RoadStation rs = cp.getRoadStation();

            if (!rs.isPublishable() || !cp.isPublishable()) {
                missingMin--;
            }
            rs.setCollectionStatus(CollectionStatus.GATHERING);
            rs.unobsolete();
            rs.updatePublicity(true);
            cp.unobsolete();
            cp.setPublic(true);
        }
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     */
    @Test
    public void testPerformanceForReceivedMessages() throws IOException, JMSException {
        createHttpResponseStubFor(1);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(2);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(3);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(4);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(5);// + IMAGE_SUFFIX);

        final JMSMessageListener.JMSDataUpdater<KuvaProtos.Kuva> dataUpdater = (data) -> {
            final StopWatch start = StopWatch.createStarted();
            if (TestTransaction.isActive()) {
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
            TestTransaction.start();
            int updated = 0;
            try {
                updated = cameraImageUpdateManager.updateCameraData(data);
            } catch (final Exception e) {
                fail("Data updating failed");
            }
            TestTransaction.flagForCommit();
            TestTransaction.end();
            log.info("handleData tookMs={}", start.getTime());
            return updated;
        };

        final JMSMessageListener<KuvaProtos.Kuva> cameraJmsMessageListener =
            new JMSMessageListener<>(new KuvaMessageMarshaller(), dataUpdater, true, log);

        Instant time = Instant.now();

        // Generate update-data
        final List<CameraPreset> presets = cameraPresetService.findAllPublishableCameraPresets();
        final Iterator<CameraPreset> presetIterator = presets.iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        long maxHandleTime = testBurstsLeft * 2200;
        final List<KuvaProtos.Kuva> data = new ArrayList<>(presets.size());

        final StopWatch sw = new StopWatch();
        while (testBurstsLeft > 0) {
            testBurstsLeft--;
            sw.reset();
            sw.start();

            data.clear();
            while (presetIterator.hasNext()) {
                CameraPreset preset = presetIterator.next();

                // Kuva: {"asemanNimi":"Vaalimaa_testi","nimi":"C0364302201610110000.jpg","esiasennonNimi":"esiasento2","esiasentoId":3324,"kameraId":1703,"aika":2016-10-10T21:00:40Z,"tienumero":7,"tieosa":42,"tieosa":false,"url":"https://testioag.liikennevirasto.fi/LOTJU/KameraKuvavarasto/6845284"}
                int kuvaIndex = RandomUtils.nextInt(1, 6);

                KuvaProtos.Kuva.Builder kuvaBuilder = KuvaProtos.Kuva.newBuilder();
                kuvaBuilder.setEsiasentoId(preset.getLotjuId());
                kuvaBuilder.setKameraId(preset.getCameraLotjuId());
                kuvaBuilder.setNimi(preset.getPresetId() + "1234.jpg");
                kuvaBuilder.setAikaleima(time.toEpochMilli());
                kuvaBuilder.setAsemanNimi("Suomenmaa " + RandomUtils.nextLong(1000, 9999));
                kuvaBuilder.setEsiasennonNimi("Esiasento" + RandomUtils.nextLong(1000, 9999));
                kuvaBuilder.setEtaisyysTieosanAlusta(RandomUtils.nextInt(0, 99999));
                kuvaBuilder.setJulkinen(true);
                kuvaBuilder.setLiviId("" + kuvaIndex);
                kuvaBuilder.setKuvaId(kuvaIndex);

                if (preset.getRoadStation().getRoadAddress() != null) {
                    kuvaBuilder.setTienumero(preset.getRoadStation().getRoadAddress().getRoadNumber());
                    kuvaBuilder.setTieosa(preset.getRoadStation().getRoadAddress().getRoadSection());
                }
                //kuvaBuilder.setUrl("http://localhost:" + httpPort + REQUEST_PATH + kuvaIndex + IMAGE_SUFFIX);
                kuvaBuilder.setXKoordinaatti("12345.67");
                kuvaBuilder.setYKoordinaatti("23456.78");

                KuvaProtos.Kuva kuva = kuvaBuilder.build();
                data.add(kuva);

                time = time.plusMillis(1000);

                cameraJmsMessageListener.onMessage(createBytesMessage(kuva));

                if (data.size() >= 25) {
                    break;
                }
            }

            final long generation = sw.getTime();

            sw.reset();
            sw.start();
            assertTrue(data.size() >= 25, "Data size too small: " + data.size());
            cameraJmsMessageListener.drainQueueScheduled();
            log.info("Data handle took " + sw.getTime() + " ms");
            handleDataTotalTime += sw.getTime();

            try {
                // send data with 1 s intervall
                long sleep = 1000 - generation;
                if (sleep > 0) {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Handle kuva data total took {} ms and max was {} ms success={}",
            handleDataTotalTime, maxHandleTime, (handleDataTotalTime <= maxHandleTime ? "OK" : "FAIL"));

        log.info("Check data validy");

        final Map<Long, CameraPreset> updatedPresets = cameraPresetService.findAllCameraPresetsMappedByLotjuId();

        for (KuvaProtos.Kuva kuva : data) {
            String presetId = CameraHelper.resolvePresetId(kuva);
            // Check written image against source image
            byte[] dst = readCameraImageFromS3(presetId);
            byte[] src = imageFilesMap.get(kuva.getKuvaId() + IMAGE_SUFFIX);
            assertArrayEquals(src, dst, "Written image is invalid for " + presetId);

            // Check preset updated to db against kuva
            CameraPreset preset = updatedPresets.get(kuva.getEsiasentoId());

            Instant kuvaTaken = Instant.ofEpochMilli(kuva.getAikaleima());
            Instant presetPictureLastModified = preset.getPictureLastModified().toInstant();

            assertEquals(kuvaTaken, presetPictureLastModified, "Preset not updated with kuva's timestamp " + preset.getPresetId());
        }

        final long latestImageTimestampToExpect = data.stream().mapToLong(KuvaProtos.Kuva::getAikaleima).max().orElseThrow();
        final ZonedDateTime imageUpdatedInDb = dataStatusService.findDataUpdatedTime(DataType.CAMERA_STATION_IMAGE_UPDATED);
        assertEquals(Instant.ofEpochMilli(roundToZeroMillis(latestImageTimestampToExpect)), imageUpdatedInDb.toInstant(), "Latest image update time not correct");

        log.info("Data is valid");
        assertTrue(handleDataTotalTime <= maxHandleTime,
            "Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms");
    }

    private byte[] readCameraImageFromS3(final String presetId) throws IOException {
        final String key = presetId + ".jpg";
        final S3Object o = amazonS3.getObject(bucketName, key);
        final byte[] imageData = o.getObjectContent().readAllBytes();
        o.getObjectContent().close();
        return imageData;
    }

    private void createHealthOKStubFor(final String healthPath) {
        log.info("Create health mock with url: " + healthPath);
        stubFor(get(urlEqualTo(healthPath))
            .willReturn(aResponse().withBody("ok!")
                .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .withStatus(200)));
    }

    private void createHttpResponseStubFor(final int kuvaId) {
        final String path = StringUtils.appendIfMissing(LOTJU_IMAGE_PATH, "/") + kuvaId;
        log.info("Create image mock with url: {}", path);
        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse().withBody(imageFilesMap.get(kuvaId + IMAGE_SUFFIX))
                        .withHeader("Content-Type", "image/jpeg")
                        .withStatus(200)));
    }

    public static BytesMessage createBytesMessage(final KuvaProtos.Kuva kuva) throws JMSException, IOException {
        final org.apache.commons.io.output.ByteArrayOutputStream bous = new org.apache.commons.io.output.ByteArrayOutputStream(0);
        kuva.writeDelimitedTo(bous);
        final byte[] kuvaBytes = bous.toByteArray();

        final BytesMessage bytesMessage = mock(BytesMessage.class);

        when(bytesMessage.getBodyLength()).thenReturn((long)kuvaBytes.length);
        when(bytesMessage.readBytes(any(byte[].class))).then(invocation -> {
            final byte[] bytes = (byte[]) invocation.getArguments()[0];
            System.arraycopy(kuvaBytes, 0, bytes, 0, kuvaBytes.length);

            return kuvaBytes.length;
        });

        return bytesMessage;
    }

    public static long roundToZeroMillis(final long epochMilli) {
        long secs = Math.floorDiv(epochMilli, 1000);
        int mos = Math.floorMod(epochMilli, 1000);
        if (mos >= 500) {
            return (secs+1)*1000;
        }
        return secs*1000;
    }
}
