package fi.livi.digitraffic.tie.data.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@Service
public class CameraDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraDataUpdateService.class);

    private final String weathercamImportDir;
    private CameraPresetService cameraPresetService;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    @Autowired
    CameraDataUpdateService(@Value("${weathercam.import-dir}")
                            final String weathercamImportDir,
                            final CameraPresetService cameraPresetService) {
        this.weathercamImportDir = weathercamImportDir;
        this.cameraPresetService = cameraPresetService;
        File dir = new File(weathercamImportDir);
        if (!dir.exists()) {
            log.info("Create weathercam import dir: " + weathercamImportDir);
            boolean success = dir.mkdirs();
            Assert.isTrue(success, "Failed to create weathercam import dir: " + weathercamImportDir);
        } else {
            log.info("Weathercam import dir " + weathercamImportDir + " exists");
        }
    }

    @Transactional
    public void updateCameraData(final List<Kuva> data) throws SQLException {
        for (Kuva kuva : data) {
            try {
                handleKuva(kuva);
            } catch (IOException e) {
                log.error("Error while handling kuva", e);
            }
        }
    }

    private File handleKuva(Kuva kuva) throws IOException {

        String presetId = kuva.getNimi().substring(0, 8);
        String filename = presetId + ".jpg";
        LocalDateTime pictureTaken = DateHelper.toLocalDateTimeAtZone(kuva.getAika(), ZoneId.systemDefault());
        log.info("Handling kuva: " +ToStringHelpper.toString(kuva));
        CameraPreset cameraPreset = cameraPresetService.findCameraPresetByPresetId(presetId);

        // Update CameraPreset
        if (cameraPreset != null) {
            cameraPreset.setPublicExternal(kuva.isJulkinen());
            cameraPreset.setPictureLastModified(pictureTaken);
        }
        // Load and save image or delete non public image
        if (cameraPreset != null && cameraPreset.isPublicExternal() && cameraPreset.isPublicInternal()) {
            try {
                File targetFile = new File(weathercamImportDir, filename);
                URL srcUrl = new URL(kuva.getUrl());
                copyURLToFile(srcUrl, targetFile);
                return targetFile;
            } catch (Exception ex) {
                log.error("Error reading/writing picture for presetId: " + presetId, ex);
            }
        } else {
            if (cameraPreset == null) {
                log.error("Could not update camera preset for " + presetId + ": doesn't exist");
            }
            // Delete possible image that should be hidden
            File targetFile = new File(weathercamImportDir, filename);
            FileUtils.deleteQuietly(targetFile);
        }
        return null;
    }

    /**
     * Loads data from given url and writes it to given file.
     * Implementation done based org.apache.commons.io.FileUtils implementation
     *
     * @param source url where to read data
     * @param destination file to write data
     * @throws IOException
     */
    private static void copyURLToFile(URL source, File destination) throws IOException {

        long start = System.currentTimeMillis();

        String tempFileName = destination.getName() + ".tmp";
        File tempTargetFile = new File(destination.getParentFile().getPath(), tempFileName);

        if (tempTargetFile.exists()) {
            log.debug("Delete old tmp file " + tempTargetFile);
            FileUtils.deleteQuietly(tempTargetFile);
            tempTargetFile = new File(destination.getPath(), tempFileName);
        }
        log.info("Load picture from " + source +  " and write to " + tempTargetFile.getAbsolutePath());

        long timeReadMs = 0L;
        long timeWriteMs = 0L;
        long bytesTotal = 0;

        InputStream input = null;
        FileOutputStream output = null;
        long startOpenStreams = 0;
        long endOpenStreams = 0;
        try {
            startOpenStreams = System.currentTimeMillis();
            input = source.openStream();
            output = openOutputStream(tempTargetFile);
            endOpenStreams = System.currentTimeMillis();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            while (true) {
                long startRead = System.currentTimeMillis();
                int bytesRead = input.read(buffer);
                long endInStartWrite = System.currentTimeMillis();
                timeReadMs += endInStartWrite-startRead;
                if (bytesRead != -1) {
                    output.write(buffer, 0, bytesRead);
                    bytesTotal += bytesRead;
                    long endWrite = System.currentTimeMillis();
                    timeWriteMs += endWrite-endInStartWrite;
                } else {
                    break;
                }
            }
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
        }

        long startMove = System.currentTimeMillis();
        FileUtils.copyFile(tempTargetFile, destination);
        FileUtils.deleteQuietly(tempTargetFile);
        long endMove = System.currentTimeMillis();
        final long timeMove = endMove - startMove;
        log.info(String.format("File handling took %1$d ms (%2$d bytes, read %3$d ms, write to disk %4$d ms, and move to dst %5$d ms, open streams %6$d ms",
                (endMove-start), bytesTotal, timeReadMs, timeWriteMs, timeMove, endOpenStreams-startOpenStreams));
    }

    private static FileOutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && parent.exists() == false) {
                if (parent.mkdirs() == false) {
                    throw new IOException("File '" + file + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file);
    }

}
