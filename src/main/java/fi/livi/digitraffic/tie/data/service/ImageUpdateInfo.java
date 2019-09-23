package fi.livi.digitraffic.tie.data.service;

import java.time.Instant;
import java.time.ZonedDateTime;

public class ImageUpdateInfo {

    public enum Status { SUCCESS, FAILED, NONE;

        public boolean isSuccess() {
            return this.equals(SUCCESS);
        }


    }

    private final String presetId;
    private final String fullPath;
    private final ZonedDateTime lastUpdated;

    private String versionId;
    private long readTotalDurationMs = 0;
    private long writeTotalDurationMs = 0;
    private long readDurationMs = 0;
    private long writeDurationMs = 0;
    private String downloadUrl;
    private int sizeBytes = -1;
    private Status readStatus = Status.NONE;
    private Status writeStatus = Status.NONE;
    private Throwable readError;
    private Throwable writeError;
    private long imageTimestampEpochMillis;

    public ImageUpdateInfo(final String presetId, final String fullPath, final ZonedDateTime lastUpdated) {
        this.presetId = presetId;
        this.fullPath = fullPath;
        this.lastUpdated = lastUpdated;
    }

    public String getPresetId() {
        return presetId;
    }

    String getFullPath() {
        return fullPath;
    }

    public ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void updateReadTotalDurationMs(final long currentReadDurationMs) {
        readTotalDurationMs += currentReadDurationMs;
    }

    public long getReadTotalDurationMs() {
        return readTotalDurationMs;
    }

    public void updateWriteTotalDurationMs(final long currentWriteDurationMs) {
        writeTotalDurationMs += currentWriteDurationMs;
    }

    public long getWriteTotalDurationMs() {
        return writeTotalDurationMs;
    }

    void setReadDurationMs(final long readDurationMs) {
        this.readDurationMs = readDurationMs;
    }

    long getReadDurationMs() {
        return readDurationMs;
    }

    void setDownloadUrl(final String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    String getDownloadUrl() {
        return downloadUrl;
    }

    void setSizeBytes(final int sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    int getSizeBytes() {
        return sizeBytes;
    }

    void setWriteDurationMs(final long writeDurationMs) {
        this.writeDurationMs = writeDurationMs;
    }

    long getWriteDurationMs() {
        return writeDurationMs;
    }

    private void setReadStatus(final Status readStatus) {
        this.readStatus = readStatus;
    }

    Status getReadStatus() {
        return readStatus;
    }

    void updateReadStatusSuccess() {
        setReadStatus(Status.SUCCESS);
        setReadError(null);
    }

    private void setWriteStatus(final Status writeStatus) {
        this.writeStatus = writeStatus;
    }

    Status getWriteStatus() {
        return writeStatus;
    }

    void updateWriteStatusSuccess() {
        setWriteStatus(Status.SUCCESS);
        setWriteError(null);
    }

    boolean isSuccess() {
        return getReadStatus().isSuccess() && getWriteStatus().isSuccess();
    }

    long getDurationMs() {
        return getReadDurationMs() + getWriteDurationMs();
    }

    void updateReadStatusFailed(final Throwable readException) {
        setReadStatus(Status.FAILED);
        setReadError(readException);
        setSizeBytes(-1);
    }

    Throwable getReadError() {
        return readError;
    }

    private void setReadError(final Throwable readError) {
        this.readError = readError;
    }

    void updateWriteStatusFailed(final Throwable writeError) {
        setWriteStatus(Status.FAILED);
        setWriteError(writeError);
    }

    Throwable getWriteError() {
        return writeError;
    }

    private void setWriteError(Throwable writeError) {
        this.writeError = writeError;
    }
}
