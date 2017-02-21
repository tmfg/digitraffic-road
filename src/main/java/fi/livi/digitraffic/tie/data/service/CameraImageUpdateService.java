package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@Service
public class CameraImageUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateService.class);

    private String sftpUploadFolder;

    private CameraPresetService cameraPresetService;

    private final SessionFactory sftpSessionFactory;
    private final RetryTemplate retryTemplate;

    @Autowired
    CameraImageUpdateService(@Value("${camera-image-uploader.sftp.uploadFolder}")
                             final String sftpUploadFolder,
                             final CameraPresetService cameraPresetService,
                             final SessionFactory sftpSessionFactory,
                             final RetryTemplate retryTemplate) {
        this.sftpUploadFolder = sftpUploadFolder;
        this.cameraPresetService = cameraPresetService;
        this.sftpSessionFactory = sftpSessionFactory;
        this.retryTemplate = retryTemplate;
    }

    private String resolvePresetId(final Kuva kuva) {
        return kuva.getNimi().substring(0, 8);
    }

    private String getPresetImageName(final String presetId) {
        return  presetId + ".jpg";
    }

    private String getImageFullPath(String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }

    @Transactional(readOnly = true)
    public void deleteAllImagesForNonPublishablePresets() {
        List<String> presetIdsToDelete = cameraPresetService.findAllNotPublishableCameraPresetsPresetIds();
        presetIdsToDelete.forEach(presetId -> deleteImageQuietly(getPresetImageName(presetId)));
    }

    @Transactional
    @Async
    public Future<Boolean> handleKuva(final Kuva kuva, final CameraPreset cameraPreset) {
        String presetId = cameraPreset != null ? cameraPreset.getPresetId() : resolvePresetId(kuva);
        String filename = getPresetImageName(presetId);
        log.info("Handling {}", ToStringHelpper.toString(kuva));
        // Update CameraPreset
        if (cameraPreset != null) {
            ZonedDateTime pictureTaken = DateHelper.toZonedDateTime(kuva.getAika());
            cameraPreset.setPublicExternal(kuva.isJulkinen());
            cameraPreset.setPictureLastModified(pictureTaken);
        }

        return retryTemplate.execute(context -> {
            // Load and save image or delete non public image
            if (cameraPreset != null && cameraPreset.isPublicExternal() && cameraPreset.isPublicInternal()) {
                try {
                    uploadImage(kuva.getUrl(), filename);
                    return new AsyncResult<>(true);
                } catch (IOException e) {
                    log.error("Error reading or writing picture for presetId {} from {} to sftp server path {}",
                            presetId, kuva.getUrl(), getImageFullPath(filename));
                    log.error("Error", e);
                    return new AsyncResult<>(false);
                }
            } else {
                if (cameraPreset == null) {
                    log.info("Could not update non existing camera preset {}", presetId);
                }
                log.info("Delete hidden or missing preset's {} remote image {}", presetId, getImageFullPath(filename));
                return new AsyncResult<>(deleteImageQuietly(filename));
            }
        });
    }

    public boolean deleteImageQuietly(String deleteImageFileName) {
        try (Session session = sftpSessionFactory.getSession()) {
            final String imageRemotePath = getImageFullPath(deleteImageFileName);
            if (session.exists(imageRemotePath) ) {
                log.info("Delete image {}", imageRemotePath);
                session.remove(imageRemotePath);
            }
            return true;
        } catch (IOException e) {
            log.error("Failed to remove remote file {}", getImageFullPath(deleteImageFileName));
            return false;
        }
    }

    private void uploadImage(final String downloadImageUrl, final String uploadImageFileName) throws IOException {
        try (final Session session = sftpSessionFactory.getSession()) {
            final URL url = new URL(downloadImageUrl);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            final String uploadPath = getImageFullPath(uploadImageFileName);
            log.info("Download image {} and upload to sftp server path {}", downloadImageUrl, uploadPath);
            session.write(con.getInputStream(), uploadPath);
        } catch (Exception e) {
            log.error("Error while trying to upload image from {} to file {}", downloadImageUrl, getImageFullPath(uploadImageFileName));
            throw new RuntimeException(e);
        }
    }
}
