package fi.livi.digitraffic.tie.data.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
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

    void writeImage(final byte[] data, final String filename, final int timestampEpochSecond) throws IOException, SftpException {
        final String imageFullPath = getImageFullPath(filename);
        try (final Session session = sftpSessionFactory.getSession()) {
            session.write(new ByteArrayInputStream(data), imageFullPath);
            ((ChannelSftp) session.getClientInstance()).setMtime(imageFullPath, timestampEpochSecond);
        } catch (Exception e) {
            log.warn("method=writeImage Failed to write image to sftpServerPath={} . mostSpecificCauseMessage={} . stackTrace={}",
                imageFullPath, NestedExceptionUtils.getMostSpecificCause(e).getMessage(), ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    /**
     * @param filename file name to delete
     * @return Info if the file exists and delete success. For non existing images success is false.
     */
    final DeleteInfo deleteImage(final String filename) {
        final StopWatch start = StopWatch.createStarted();
        final String imageFullPath = getImageFullPath(filename);
        try (final Session session = sftpSessionFactory.getSession()) {
            if (session.exists(imageFullPath) ) {
                log.info("method=deleteImage presetId={} imagePath={}", resolvePresetIdFromImageFullPath(imageFullPath), imageFullPath);
                session.remove(imageFullPath);
                return new DeleteInfo(true, true, start.getTime(), imageFullPath);
            }
            return new DeleteInfo(false, false, start.getTime(), imageFullPath);
        } catch (IOException e) {
            log.error(String.format("Failed to remove remote file deleteImageFileName=%s", imageFullPath), e);
            return new DeleteInfo(true, false, start.getTime(), imageFullPath);
        }
    }

    private static String resolvePresetIdFromImageFullPath(final String imageFullPath) {
        return StringUtils.substringBeforeLast(StringUtils.substringAfterLast(imageFullPath,"/"), ".");
    }

    String getImageFullPath(final String imageFileName) {
        return StringUtils.appendIfMissing(sftpUploadFolder, "/") + imageFileName;
    }

    static class DeleteInfo {
        private final boolean fileExists;
        private final boolean deleteSuccess;
        private final long durationMs;
        private final String fullPath;

        private DeleteInfo(final boolean fileExists, final boolean deleteSuccess, final long durationMs, final String fullPath) {
            this.fileExists = fileExists;
            this.deleteSuccess = deleteSuccess;
            this.durationMs = durationMs;
            this.fullPath = fullPath;
        }

        boolean isFileExists() {
            return fileExists;
        }

        boolean isDeleteSuccess() {
            return deleteSuccess;
        }

        boolean isFileExistsAndDeleteSuccess() {
            return isFileExists() && isDeleteSuccess();
        }

        boolean isSuccess() {
            return !isFileExists() || isDeleteSuccess();
        }

        long getDurationMs() {
            return durationMs;
        }

        String getFullPath() {
            return fullPath;
        }
    }
}
