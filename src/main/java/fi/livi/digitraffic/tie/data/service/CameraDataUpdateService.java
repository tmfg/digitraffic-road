package fi.livi.digitraffic.tie.data.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.FileHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@Service
public class CameraDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraDataUpdateService.class);

    private String weathercamImportDir;
    private CameraPresetService cameraPresetService;

    private final EntityManager entityManager;

    @Autowired
    CameraDataUpdateService(@Value("${weathercam.importDir}")
                            final String weathercamImportDir,
                            final CameraPresetService cameraPresetService,
                            final EntityManager entityManager) {
        setWeathercamImportDir(weathercamImportDir);
        this.cameraPresetService = cameraPresetService;
        this.entityManager = entityManager;
    }

    public void setWeathercamImportDir(String weathercamImportDir) {
        this.weathercamImportDir = weathercamImportDir;
        File dir = new File(weathercamImportDir);
        if (!dir.exists()) {
            log.info("Create weathercam import dir: " + weathercamImportDir);
            boolean success = dir.mkdirs();
            if (!success) {
                throw new IllegalStateException("Failed to create weathercam import dir: " + weathercamImportDir);
            }
        } else {
            log.info("Weathercam import dir " + weathercamImportDir + " exists");
        }
        if (!dir.canWrite()) {
            throw new IllegalStateException("Weathercam import dir: " + weathercamImportDir + " is not writeable!");
        }
    }

    @Transactional
    public void updateCameraData(final List<Kuva> data) throws SQLException {
        HashMap<String, Kuva> presetIdToKuvaMap = new HashMap<>();
        for (Kuva kuva : data) {
            presetIdToKuvaMap.put(CameraHelper.resolvePresetId(kuva), kuva);
        }

        List<CameraPreset> cameraPresets = cameraPresetService.findCameraPresetByPresetIdIn(presetIdToKuvaMap.keySet());

        // Handle present presets
        for (CameraPreset cameraPreset : cameraPresets) {
            Kuva kuva = presetIdToKuvaMap.remove(cameraPreset.getPresetId());
            handleKuva(kuva, cameraPreset);
        }

        // Handle missing presets to delete possible images from disk
        for (Kuva notFoundPresetForKuva : presetIdToKuvaMap.values()) {
            handleKuva(notFoundPresetForKuva, null);
        }
    }

    private String resolvePresetId(final Kuva kuva) {
        return kuva.getNimi().substring(0, 8);
    }

    private void handleKuva(final Kuva kuva, final CameraPreset cameraPreset) {

        String presetId = resolvePresetId(kuva);
        String filename = presetId + ".jpg";
        ZonedDateTime pictureTaken = DateHelper.toZonedDateTime(kuva.getAika());
        log.info("Handling kuva: " +ToStringHelpper.toString(kuva));

        // Update CameraPreset
        if (cameraPreset != null) {
            cameraPreset.setPublicExternal(kuva.isJulkinen());
            cameraPreset.setPictureLastModified(pictureTaken);
            entityManager.flush();
            entityManager.clear();
        }
        // Load and save image or delete non public image
        if (cameraPreset != null && cameraPreset.isPublicExternal() && cameraPreset.isPublicInternal()) {
            try {
                File targetFile = new File(weathercamImportDir, filename);
                URL srcUrl = new URL(kuva.getUrl());
                FileHelper.copyURLToFile(srcUrl, targetFile);
            } catch (IOException ex) {
                log.error("Error reading/writing picture for presetId: " + presetId, ex);
            }
        } else {
            if (cameraPreset == null) {
                log.error("Could not update camera preset for " + presetId + ": doesn't exist");
            }
            // Delete possible image that should be hidden
            File targetFile = new File(weathercamImportDir, filename);
            if (targetFile.exists()) {
                log.info("Delete hidden or missing preset's image " + targetFile.getPath() + " for presetId " + presetId);
                FileUtils.deleteQuietly(targetFile);
            }
        }
    }
}
