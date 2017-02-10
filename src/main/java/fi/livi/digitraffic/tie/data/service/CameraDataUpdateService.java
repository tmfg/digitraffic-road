package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@Service
public class CameraDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraDataUpdateService.class);

    private String sftpUploadFolder;
    private CameraPresetService cameraPresetService;

    private final EntityManager entityManager;

    @Autowired
    private CachingSessionFactory cachingSessionFactory;

    @Autowired
    CameraDataUpdateService(@Value("${camera-image-uploader.sftp.uploadFolder}")
                            final String sftpUploadFolder,
                            final CameraPresetService cameraPresetService,
                            final EntityManager entityManager) {
        this.sftpUploadFolder = sftpUploadFolder;
        this.cameraPresetService = cameraPresetService;
        this.entityManager = entityManager;
    }

    @Transactional
    public void updateCameraData(final List<Kuva> data) throws SQLException {
        final Map<String, Kuva> presetIdToKuvaMap =
                data.stream().collect(Collectors.toMap(kuva -> CameraHelper.resolvePresetId(kuva), Function.identity()));

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
                uploadImage(kuva.getUrl(), filename);
            } catch (IOException e) {
                log.error("Error reading or writing picture for presetId {} from {} to {}",
                          presetId, kuva.getUrl(), getImageFullPath(filename));
                log.error("Error", e);
            }
        } else {
            if (cameraPreset == null) {
                log.error("Could not update non existing camera preset {}", presetId);
            }
            log.info("Delete hidden or missing preset's {} remote image {}", presetId, getImageFullPath(filename));
            deleteImageQuietly(filename);
        }
    }

    private String getImageFullPath(String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }

    private void deleteImageQuietly(String deleteImageFileName) {
        try {
            final Session session = cachingSessionFactory.getSession();
            session.remove(getImageFullPath(deleteImageFileName));
        } catch (IOException e) {
            log.debug("Failed to remove remote file {}", getImageFullPath(deleteImageFileName));
        }
    }

    private void uploadImage(String downloadImageUrl, String uploadImageFileName) throws IOException {
        final Session session = cachingSessionFactory.getSession();
        final URL url = new URL(downloadImageUrl);
        final String uploadPath = StringUtils.appendIfMissing(sftpUploadFolder, "/") + uploadImageFileName;
        log.info("Download image {} and upload with sftp to {}", downloadImageUrl, uploadPath);
        session.write(url.openStream(), uploadPath);
    }

}
