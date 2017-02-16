package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.javafx.binding.StringFormatter;

import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
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

    private final SessionFactory sftpSessionFactory;
    private final RetryTemplate retryTemplate;

    private final HashMap<String, Integer> countMap = new HashMap<>();

    @Autowired
    CameraDataUpdateService(@Value("${camera-image-uploader.sftp.uploadFolder}")
                            final String sftpUploadFolder,
                            final CameraPresetService cameraPresetService,
                            SessionFactory sftpSessionFactory,
                            RetryTemplate retryTemplate) {
        this.sftpUploadFolder = sftpUploadFolder;
        this.cameraPresetService = cameraPresetService;
        this.sftpSessionFactory = sftpSessionFactory;
        this.retryTemplate = retryTemplate;
    }

    @Transactional
    public void updateCameraData(final List<Kuva> data) throws SQLException {
        // Collect newest data per station
        HashMap<Long, Kuva> kuvaMappedByPresetLotjuId = new HashMap<>();
        data.stream().forEach(k -> {
            if (k.getEsiasentoId() != null) {
                Kuva currentKamera = kuvaMappedByPresetLotjuId.get(k.getEsiasentoId());
                if (currentKamera == null || currentKamera.getAika().toGregorianCalendar().before(currentKamera.getAika().toGregorianCalendar())) {
                    if (currentKamera != null) {
                        log.info("Replace " + currentKamera.getAika() + " with " + currentKamera.getAika());
                    }
                    kuvaMappedByPresetLotjuId.put(k.getEsiasentoId(), k);
                }
            } else {
                log.warn("Kuva esiasentoId is null: {}", ToStringHelpper.toString(k));
            }
        });

        List<CameraPreset> cameraPresets = cameraPresetService.findPublishableCameraPresetByLotjuIdIn(kuvaMappedByPresetLotjuId.keySet());

        // Handle present presets
        for (CameraPreset cameraPreset : cameraPresets) {
            Kuva kuva = kuvaMappedByPresetLotjuId.remove(cameraPreset.getLotjuId());
            if (kuva == null) {
                log.error("No kuva for preset {}", cameraPreset.toString());
            } else {
                handleKuva(kuva, cameraPreset);
            }
        }

        // Handle missing presets to delete possible images from disk
        for (Kuva notFoundPresetForKuva : kuvaMappedByPresetLotjuId.values()) {
            handleKuva(notFoundPresetForKuva, null);
        }
    }

    private String resolvePresetId(final Kuva kuva) {
        return kuva.getNimi().substring(0, 8);
    }

    private void handleKuva(final Kuva kuva, final CameraPreset cameraPreset) {
        String presetId = cameraPreset != null ? cameraPreset.getPresetId() : resolvePresetId(kuva);
        String filename = presetId + ".jpg";
        log.info("Handling kuva: " + ToStringHelpper.toString(kuva));

        // Update CameraPreset
        if (cameraPreset != null) {
            ZonedDateTime pictureTaken = DateHelper.toZonedDateTime(kuva.getAika());
            cameraPreset.setPublicExternal(kuva.isJulkinen());
            cameraPreset.setPictureLastModified(pictureTaken);
        }
        // Load and save image or delete non public image
        if (cameraPreset != null && cameraPreset.isPublicExternal() && cameraPreset.isPublicInternal()) {
            retryTemplate.execute(context -> {
                try {
                    context.setAttribute(MetadataApplicationConfiguration.RETRY_OPERATION, StringFormatter.format("UploadImage from %s to %s", kuva.getUrl(), getImageFullPath(filename)));
                    uploadImage(kuva.getUrl(), filename);
                    return true;
                } catch (IOException e) {
                    log.error("Error reading or writing picture for presetId {} from {} to sftp server path {}",
                            presetId, kuva.getUrl(), getImageFullPath(filename));
                    log.error("Error", e);
                    return false;
                }
            });
        } else {
            if (cameraPreset == null) {
                log.info("Could not update non existing camera preset {}", presetId);
            }
            log.info("Delete hidden or missing preset's {} remote image {}", presetId, getImageFullPath(filename));
            deleteImageQuietly(filename);
        }
    }

    private String getImageFullPath(String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }

    private void deleteImageQuietly(String deleteImageFileName) {
        try (Session session = sftpSessionFactory.getSession()) {
            final String imageRemotePath = getImageFullPath(deleteImageFileName);
            if (session.exists(imageRemotePath) ) {
                session.remove(imageRemotePath);
            }
        } catch (IOException e) {
            log.error("Failed to remove remote file {}", getImageFullPath(deleteImageFileName));
        }
    }

    private void uploadImage(String downloadImageUrl, String uploadImageFileName) throws IOException {
        try (final Session session = sftpSessionFactory.getSession()) {
            final URL url = new URL(downloadImageUrl);
            final String uploadPath = getImageFullPath(uploadImageFileName);
            log.info("Download image {} and upload to sftp server path {}", downloadImageUrl, uploadPath);
            session.write(url.openStream(), uploadPath);
        } catch (Exception e) {
            log.error("Error while trying to upload image from {} to file {}", downloadImageUrl, getImageFullPath(uploadImageFileName));
            throw new RuntimeException(e);
        }
    }

}
