package fi.livi.digitraffic.tie.data.jms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.sftp.AbstractSftpTest;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;

@Transactional
public class CameraJmsMessageListenerTest extends AbstractSftpTest {
    
    private static final Logger log = LoggerFactory.getLogger(CameraJmsMessageListenerTest.class);

    private static final String IMAGE_SUFFIX = "image.jpg";
    private static final String IMAGE_DIR = "lotju/kuva/";

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    LockingService lockingService;

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Autowired
    ResourceLoader resourceLoader;

    private Map<String, byte[]> imageFilesMap = new HashMap<>();

    private Marshaller jaxbMarshaller;
    private Unmarshaller jaxbUnmarshaller;

    @After
    public void restoreData() throws IOException, JAXBException {
        restoreGeneratedLotjuIdsWithJdbc();
    }

    @Before
    public void initData() throws IOException, JAXBException {

        // Creates also new road stations so run before generating lotjuIds
        cameraStationUpdater.fixCameraPresetsWithMissingRoadStations();
        entityManager.flush();
        entityManager.clear();
        generateMissingLotjuIdsWithJdbc();
        fixDataWithJdbc();

        jaxbMarshaller = JAXBContext.newInstance(Kuva.class).createMarshaller();
        jaxbUnmarshaller = JAXBContext.newInstance(Kuva.class).createUnmarshaller();

        int i = 5;
        while (i > 0) {
            String imageName = i + IMAGE_SUFFIX;
            Resource resource = resourceLoader.getResource("classpath:" + IMAGE_DIR + imageName);
            final File imageFile = resource.getFile();
            byte[] bytes = FileUtils.readFileToByteArray(imageFile);
            imageFilesMap.put(imageName, bytes);
            i--;
        }

        List<CameraPreset> nonObsoleteCameraPresets = cameraPresetService.findAllPublishableCameraPresets();
        log.info("Non obsolete CameraPresets before " + nonObsoleteCameraPresets.size());
        Map<Long, CameraPreset> cameraPresets = cameraPresetService.findAllCameraPresetsMappedByLotjuId();
        log.info("All camera presets size {}", cameraPresets.size());
        int missingMin = 1000 - nonObsoleteCameraPresets.size();
        Iterator<CameraPreset> iter = cameraPresets.values().iterator();
        while (missingMin > 0 && iter.hasNext()) {
            CameraPreset cp = iter.next();
            RoadStation rs = cp.getRoadStation();
            if (!rs.isPublishable() || !cp.isPublishable()) {
                missingMin--;
            }
            rs.setCollectionStatus(CollectionStatus.GATHERING);
            rs.setObsolete(false);
            rs.setPublic(true);
            cp.setObsolete(false);
            cp.setPublicExternal(true);
            cp.setPublicInternal(true);
        }
        entityManager.flush();
        entityManager.clear();
        nonObsoleteCameraPresets = cameraPresetService.findAllPublishableCameraPresets();
        log.info("Non obsolete CameraPresets for testing " + nonObsoleteCameraPresets.size());
    }

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     * @throws IOException
     * @throws JAXBException
     * @throws DatatypeConfigurationException
     */
    @Test
    public void testPerformanceForReceivedMessages() throws IOException, JAXBException, DatatypeConfigurationException {

        log.info("Using weathercam.importDir: " + testFolder.getRoot().getPath());

        log.info("Init mock http-server for images");
        log.info("Mock server port: " + port);
        createHttpResponseStubFor(1 + IMAGE_SUFFIX);
        createHttpResponseStubFor(2 + IMAGE_SUFFIX);
        createHttpResponseStubFor(3 + IMAGE_SUFFIX);
        createHttpResponseStubFor(4 + IMAGE_SUFFIX);
        createHttpResponseStubFor(5 + IMAGE_SUFFIX);

        JMSMessageListener.JMSDataUpdater<Kuva> dataUpdater = (data) -> {
            StopWatch start = StopWatch.createStarted();
            if (TestTransaction.isActive()) {
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
            TestTransaction.start();
            try {
                cameraDataUpdateService.updateCameraData(data.stream().map(p -> p.getLeft()).collect(Collectors.toList()));
            } catch (SQLException e) {
                Assert.fail("Data updating failed");
            }
            TestTransaction.flagForCommit();
            TestTransaction.end();
            log.info("handleData took {} ms", start.getTime());
        };

        JMSMessageListener<Kuva> cameraJmsMessageListener =
                new JMSMessageListener<Kuva>(Kuva.class, dataUpdater, true, log);

        DatatypeFactory df = DatatypeFactory.newInstance();
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        XMLGregorianCalendar xgcal = df.newXMLGregorianCalendar(gcal);

        // Generate update-data
        List<CameraPreset> presets = cameraPresetService.findAllPublishableCameraPresets();
        Iterator<CameraPreset> presetIterator = presets.iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        long maxHandleTime = testBurstsLeft * 2000;
        final List<Pair<Kuva, String>> data = new ArrayList<>(presets.size());

        StopWatch sw = new StopWatch();
        while (testBurstsLeft > 0) {
            testBurstsLeft--;
            sw.reset();
            sw.start();

            data.clear();
            while (true && presetIterator.hasNext()) {
                CameraPreset preset = presetIterator.next();

                // Kuva: {"asemanNimi":"Vaalimaa_testi","nimi":"C0364302201610110000.jpg","esiasennonNimi":"esiasento2","esiasentoId":3324,"kameraId":1703,"aika":2016-10-10T21:00:40Z,"tienumero":7,"tieosa":42,"tieosa":false,"url":"https://testioag.liikennevirasto.fi/LOTJU/KameraKuvavarasto/6845284"}
                int kuvaIndex = RandomUtils.nextInt(1, 6);
                Kuva kuva = new Kuva();
                kuva.setEsiasentoId(preset.getLotjuId());
                kuva.setKameraId(preset.getCameraLotjuId());
                kuva.setNimi(preset.getPresetId() + "1234.jpg");
                kuva.setAika((XMLGregorianCalendar) xgcal.clone());
                kuva.setAsemanNimi("Suomenmaa " + RandomUtils.nextLong(1000, 9999));
                kuva.setEsiasennonNimi("Esiasento" + RandomUtils.nextLong(1000, 9999));
                kuva.setEtaisyysTieosanAlusta(BigInteger.valueOf(RandomUtils.nextLong(0, 99999)));
                kuva.setJulkinen(true);
                kuva.setLiviId("" + kuvaIndex);
                if (preset.getRoadStation().getRoadAddress() != null) {
                    kuva.setTienumero(BigInteger.valueOf(preset.getRoadStation().getRoadAddress().getRoadNumber()));
                    kuva.setTieosa(BigInteger.valueOf(preset.getRoadStation().getRoadAddress().getRoadSection()));
                }
                kuva.setUrl("http://localhost:" + httpPort + REQUEST_PATH + kuvaIndex + IMAGE_SUFFIX);
                kuva.setXKoordinaatti("12345.67");
                kuva.setYKoordinaatti("23456.78");

                data.add(Pair.of(kuva, null));
                xgcal.add(df.newDuration(1000));

                StringWriter xmlSW = new StringWriter();
                jaxbMarshaller.marshal(kuva, xmlSW);
                StringReader sr = new StringReader(xmlSW.toString());
                Kuva object = (Kuva)jaxbUnmarshaller.unmarshal(sr);

                cameraJmsMessageListener.onMessage(AbstractJmsMessageListenerTest.createTextMessage(xmlSW.toString(), "Kuva " + preset.getPresetId()));

                if (data.size() >= 25) {
                    break;
                }
            }

            sw.stop();
            long generation = sw.getTime();
            log.info("Data generation took " + generation + " ms");

            sw.reset();
            sw.start();
            Assert.assertTrue("Data size too small: " + data.size(), data.size() >= 25);
            cameraJmsMessageListener.drainQueueScheduled();
            sw.stop();
            log.info("Data handle took " + sw.getTime() + " ms");
            handleDataTotalTime += sw.getTime();

            try {
                // send data with 1 s intervall
                long sleep = 1000 - generation;
                if (sleep < 0) {
                    log.error("Data generation took too long");
                } else {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Handle kuva data total took " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms " +
                (handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)"));

        log.info("Check data validy");

        Map<Long, CameraPreset> updatedPresets = cameraPresetService.findAllCameraPresetsMappedByLotjuId();

        for (Pair<Kuva, String> pair : data) {
            Kuva kuva = pair.getLeft();
            String presetId = CameraHelper.resolvePresetId(kuva);
            // Check written image against source image
            byte[] dst = readCameraDataFromSftp(presetId);
            byte[] src = imageFilesMap.get(kuva.getLiviId() + IMAGE_SUFFIX);
            Assert.assertArrayEquals("Written image is invalid for " + presetId, src, dst);

            // Check preset updated to db against kuva
            CameraPreset preset = updatedPresets.get(kuva.getEsiasentoId());
            LocalDateTime kuvaTaken = DateHelper.toLocalDateTime(kuva.getAika());
            LocalDateTime presetPictureLastModified = DateHelper.toLocalDateTime(preset.getPictureLastModified());
            Assert.assertEquals("Preset not updated with kuva's timestamp " + preset.getPresetId(), kuvaTaken, presetPictureLastModified);
        }
        log.info("Data is valid");
        Assert.assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms",
                handleDataTotalTime <= maxHandleTime);
    }

    private byte[] readCameraDataFromSftp(String presetId) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Session s = sftpSessionFactory.getSession();
        s.read(getSftpPath(presetId), out);
        s.close();
        return out.toByteArray();
    }

    String getImportDir() {
        return testFolder.getRoot().getPath();
    }

    private byte[] readCameraDataFromDisk(String presetId) throws IOException {
        final File imageFile = new File(getImportDir() + "/" + presetId + ".jpg");
        return FileUtils.readFileToByteArray(imageFile);
    }

    private void createHttpResponseStubFor(String kuva) throws IOException {
        byte[] data = imageFilesMap.get(kuva);
        log.info("Create mock with url: " + REQUEST_PATH + kuva);
        stubFor(get(urlEqualTo(REQUEST_PATH + kuva))
                .willReturn(aResponse().withBody(imageFilesMap.get(kuva))
                        .withHeader("Content-Type", "image/jpeg")
                        .withStatus(200)));
    }
}
