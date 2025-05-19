package fi.livi.digitraffic.tie.service.jms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.activemq.artemis.jms.client.ActiveMQBytesMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.github.tomakehurst.wiremock.WireMockServer;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.common.util.ThreadUtil;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.jms.marshaller.WeathercamDataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageUpdateManager;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetService;
import jakarta.jms.JMSException;
import jakarta.persistence.EntityManager;

@TestPropertySource(properties = {
        "metadata.server.addresses=http://localhost:8898" // Overlaps with another test port
})
public class CameraJmsMessageHandlerTest extends AbstractJMSMessageHandlerTest {
    private static final Logger log = LoggerFactory.getLogger(CameraJmsMessageHandlerTest.class);

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
    private String lotjuImagePath;

    //private final WeathercamDataJMSMessageMarshaller messageMarshaller = new WeathercamDataJMSMessageMarshaller();

    private final Map<String, byte[]> imageFilesMap = new HashMap<>();

    private WireMockServer wm;

    private final int stationsCount = 50;
    private final int presetsPerStationCount = 5;

    @BeforeEach
    public void initData() throws IOException {
        TestUtils.truncateCameraData(entityManager);
        generateImageFilesMap();

        wm = new WireMockServer(options().port(8898));
        wm.start();
        log.info("WireMockServer options: {}", wm.getOptions());
        log.info("lotjuImagePath: {}", lotjuImagePath);
        log.info("healthPath: {}", healthPath);
        createHealthOKStubFor(healthPath);
        createHttpResponseStubFor(1);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(2);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(3);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(4);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(5);// + IMAGE_SUFFIX);

        // 250 presets
        final List<List<CameraPreset>> cs =
                TestUtils.generateDummyCameraStations(stationsCount, presetsPerStationCount);
        cs.forEach(camera -> camera.forEach(preset -> entityManager.persist(preset)));

        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    public void cleanUp() {
        if (wm.isRunning()) {
            wm.stop();
        }
    }

    private void generateImageFilesMap() throws IOException {
        int i = 5;
        while (i > 0) {
            final String imageName = i + IMAGE_SUFFIX;
            final Resource resource = resourceLoader.getResource("classpath:" + IMAGE_DIR + imageName);
            final File imageFile = resource.getFile();
            final byte[] bytes = FileUtils.readFileToByteArray(imageFile);

            imageFilesMap.put(imageName, bytes);
            i--;
        }
    }

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     */
    @Test
    public void testPerformanceForReceivedMessages() throws IOException, JMSException {

        final JMSMessageHandler.JMSDataUpdater<KuvaProtos.Kuva> dataUpdater = createJMSDataUpdater();

        final JMSMessageHandler<KuvaProtos.Kuva> cameraJmsMessageListener =
                new JMSMessageHandler<>(JMSMessageHandler.JMSMessageType.WEATHERCAM_DATA, dataUpdater, new WeathercamDataJMSMessageMarshaller(), lockingService.getInstanceId());

        Instant time = Instant.now().minusSeconds(60);

        // Generate update-data
        final List<CameraPreset> presets = cameraPresetService.findAllPublishableCameraPresets();
        final Iterator<CameraPreset> presetIterator = presets.iterator();

        long handleDataTotalTime = 0;
        long maxHandleTime = 0;
        final List<KuvaProtos.Kuva> jmsKuvaMessages = new ArrayList<>(presets.size());

        int iteration = 0;
        while (presetIterator.hasNext()) {
            iteration++;
            final int burstSize = 25;
            maxHandleTime += burstSize * 85; // allowed time per preset 85 ms
            final StopWatch sw = StopWatch.createStarted();

            while (presetIterator.hasNext()) {
                final CameraPreset preset = presetIterator.next();

                final KuvaProtos.Kuva kuva = createKuvaMessage(preset, time);
                jmsKuvaMessages.add(kuva);

                final String key = preset.getPresetId() + ".jpg";
                final String versionKey = preset.getPresetId() + "-versions.jpg";
                final String versionId = preset.getPresetId() + "-version-" + RandomStringUtils.secure();

                mockS3PutImageVersion(versionId, versionKey);
                mockS3GetObjectWithImageKey(kuva, key);

                time = time.plusMillis(1000);

                final ActiveMQBytesMessage bm = createBytesMessage(kuva);
                cameraJmsMessageListener.onMessage(bm);

                if (jmsKuvaMessages.size() >= burstSize * iteration) {
                    break;
                }
            }

            final long generation = sw.getDuration().toMillis();
            assertFalse(jmsKuvaMessages.isEmpty(), "Data was empty");
            final StopWatch drain = StopWatch.createStarted();
            cameraJmsMessageListener.drainQueueScheduled();
            handleDataTotalTime += drain.getDuration().toMillis();

            // send data with 1 s intervall
            final long sleep = 1000 - generation;
            if (sleep > 0) {
                ThreadUtil.delayMs(sleep);
            }
        }

        assertEquals(stationsCount * presetsPerStationCount, jmsKuvaMessages.size());

        log.info("Handle kuva data total took {} ms and max was {} ms success={}",
                handleDataTotalTime, maxHandleTime, (handleDataTotalTime <= maxHandleTime ? "OK" : "FAIL"));

        log.info("Check data validy");

        checkDataUpdated(jmsKuvaMessages);

        final long latestImageTimestampToExpect =
                jmsKuvaMessages.stream().mapToLong(KuvaProtos.Kuva::getAikaleima).max().orElseThrow();
        final Instant imageUpdatedInDb =
                dataStatusService.findDataUpdatedTime(DataType.CAMERA_STATION_IMAGE_UPDATED);
        assertEquals(Instant.ofEpochMilli(roundToZeroMillis(latestImageTimestampToExpect)),
                imageUpdatedInDb, "Latest image update time not correct");

        log.info("Data is valid");
        assertTrue(handleDataTotalTime <= maxHandleTime,
                "Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms");
    }

    private void checkDataUpdated(final List<KuvaProtos.Kuva> jmsKuvaMessages) throws IOException {
        final Map<Long, CameraPreset> updatedPresets = cameraPresetService.findAllCameraPresetsMappedByLotjuId();

        for (final KuvaProtos.Kuva kuva : jmsKuvaMessages) {
            final String presetId = CameraHelper.resolvePresetId(kuva);
            // Check written image against source image
            final byte[] dst = readCameraImageFromS3(presetId);
            final byte[] src = imageFilesMap.get(kuva.getKuvaId() + IMAGE_SUFFIX);
            assertArrayEquals(src, dst, "Written image is invalid for " + presetId);

            // Check preset updated to db against kuva
            final CameraPreset preset = updatedPresets.get(kuva.getEsiasentoId());

            final Instant kuvaTaken = Instant.ofEpochMilli(kuva.getAikaleima());
            final Instant presetPictureLastModified = preset.getPictureLastModified();

            assertEquals(kuvaTaken, presetPictureLastModified,
                    "Preset not updated with kuva's timestamp " + preset.getPresetId());
        }
    }

    private void mockS3GetObjectWithImageKey(final KuvaProtos.Kuva kuva, final String key) {
        final byte[] imageData = imageFilesMap.get(kuva.getKuvaId() + IMAGE_SUFFIX);
        final S3Object s3Object = new S3Object();
        final S3ObjectInputStream objectContent = new S3ObjectInputStream(new ByteArrayInputStream(imageData), null);
        s3Object.setObjectContent(objectContent);
        Mockito.when(amazonS3.getObject(Mockito.eq(bucketName), Mockito.eq(key))).thenReturn(s3Object);
    }

    private void mockS3PutImageVersion(final String versionId, final String versionKey) {
        final PutObjectResult result = new PutObjectResult();
        result.setVersionId(versionId);
        Mockito
                .when(amazonS3.putObject(Mockito.anyString(), Mockito.eq(versionKey), Mockito.any(), Mockito.any()))
                .thenReturn(result);
    }

    private static KuvaProtos.Kuva createKuvaMessage(final CameraPreset preset, final Instant time) {
        // Kuva: {"asemanNimi":"Vaalimaa_testi","nimi":"C0364302201610110000.jpg","esiasennonNimi":"esiasento2","esiasentoId":3324,"kameraId":1703,"aika":2016-10-10T21:00:40Z,"tienumero":7,"tieosa":42,"tieosa":false,"url":"https://testioag.liikennevirasto.fi/LOTJU/KameraKuvavarasto/6845284"}
        final int kuvaIndex = TestUtils.getRandomId(1, 5);

        final KuvaProtos.Kuva.Builder kuvaBuilder = KuvaProtos.Kuva.newBuilder();
        kuvaBuilder.setEsiasentoId(preset.getLotjuId());
        kuvaBuilder.setKameraId(preset.getCameraLotjuId());
        kuvaBuilder.setNimi(preset.getPresetId() + "1234.jpg");
        kuvaBuilder.setAikaleima(time.toEpochMilli());
        kuvaBuilder.setAsemanNimi("Suomenmaa " + TestUtils.getRandomString(4));
        kuvaBuilder.setEsiasennonNimi("Esiasento" + TestUtils.getRandomString(4));
        kuvaBuilder.setEtaisyysTieosanAlusta(TestUtils.getRandomId(0, 99999));
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

        return kuvaBuilder.build();
    }

    private JMSMessageHandler.JMSDataUpdater<KuvaProtos.Kuva> createJMSDataUpdater() {
        return (data) -> {
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
            log.info("handleData tookMs={}", start.getDuration().toMillis());
            return updated;
        };
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
        wm.stubFor(get(urlEqualTo(healthPath))
                .willReturn(aResponse().withBody("ok!")
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .withStatus(200)));
    }

    private void createHttpResponseStubFor(final int kuvaId) {
        final String path = StringUtils.appendIfMissing(lotjuImagePath, "/") + kuvaId;
        log.info("Create image mock with url: {}", path);
        wm.stubFor(get(urlEqualTo(path))
                .willReturn(aResponse().withBody(imageFilesMap.get(kuvaId + IMAGE_SUFFIX))
                        .withHeader("Content-Type", "image/jpeg")
                        .withStatus(200)));
    }

    public static long roundToZeroMillis(final long epochMilli) {
        final long secs = Math.floorDiv(epochMilli, 1000);
        final int mos = Math.floorMod(epochMilli, 1000);
        if (mos >= 500) {
            return (secs + 1) * 1000;
        }
        return secs * 1000;
    }
}
