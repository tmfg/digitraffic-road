package fi.livi.digitraffic.tie.data.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.quartz.CameraMetadataUpdateJob;

@Component
@ConditionalOnNotWebApplication
public class CameraImageWriter {

    private static final Logger log = LoggerFactory.getLogger(CameraImageWriter.class);

    private final SessionFactory sftpSessionFactory;
    private final String sftpUploadFolder;

    CameraImageWriter(
        @Qualifier("sftpSessionFactory")
        final SessionFactory sftpSessionFactory,
        @Value("${camera-image-uploader.sftp.uploadFolder}")
        final String sftpUploadFolder
    ) {
        this.sftpSessionFactory = sftpSessionFactory;
        this.sftpUploadFolder = sftpUploadFolder;
    }

    void writeImage(byte[] data, String filename, int timestampEpochSecond) throws IOException, SftpException {
        final String uploadPath = getImageFullPath(filename);
        try (final Session session = sftpSessionFactory.getSession()) {
            log.info("method=writeImage Writing image to sftpServerPath={} started", uploadPath);
            session.write(new ByteArrayInputStream(data), uploadPath);
            ((ChannelSftp) session.getClientInstance()).setMtime(uploadPath, timestampEpochSecond);
            log.info("method=writeImage Writing image to sftpServerPath={} fileTimestamp={} ended successfully",
                uploadPath, Instant.ofEpochSecond(timestampEpochSecond));
        } catch (Exception e) {
            log.warn("method=writeImage Failed to write image to sftpServerPath={} . mostSpecificCauseMessage={} . stackTrace={}", uploadPath,
                NestedExceptionUtils
                    .getMostSpecificCause(e).getMessage(), ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    /**
     * @param deleteImageFileName file name to delete
     * @return Info if the file exists and delete success. For non existing images success is false.
     */
    final DeleteInfo deleteImage(final String deleteImageFileName) {
        try (final Session session = sftpSessionFactory.getSession()) {
            final String imageRemotePath = getImageFullPath(deleteImageFileName);
            if (session.exists(imageRemotePath)) {
                log.info("Delete imagePath={}", imageRemotePath);
                session.remove(imageRemotePath);
                return new DeleteInfo(true, true);
            }
            return new DeleteInfo(false, false);
        } catch (IOException e) {
            log.error(String.format("Failed to remove remote file deleteImageFileName=%s", getImageFullPath(deleteImageFileName)), e);
            return new DeleteInfo(true, false);
        }
    }

    /**
     * @return success (true) if file doesn't exist or delete success for existing file. Otherwise failure (false);
     */
    final boolean deleteKuva(KuvaProtos.Kuva kuva, String presetId, String filename) {
        log.info(
            "method=deleteKuva Deleting presetId={} remote imagePath={}. The image is not publishable or preset was not included in previous run of" +
                "clazz={}. Kuva from incoming JMS: {}", presetId, getImageFullPath(filename),
            CameraMetadataUpdateJob.class.getName(), ToStringHelper.toString(kuva));

        final DeleteInfo result = deleteImage(filename);
        return !result.isFileExists() || result.isDeleteSuccess();
    }

    static class DeleteInfo {
        private final boolean fileExists;
        private final boolean deleteSuccess;

        private DeleteInfo(boolean fileExists, boolean deleteSuccess) {
            this.fileExists = fileExists;
            this.deleteSuccess = deleteSuccess;
        }

        boolean isFileExists() {
            return fileExists;
        }

        boolean isDeleteSuccess() {
            return deleteSuccess;
        }

        boolean isFileExistsAndDeleteSuccess() {
            return fileExists && deleteSuccess;
        }
    }

    private String getImageFullPath(final String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }
}
