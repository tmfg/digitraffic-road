package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.helper.DateHelper;

public class ImageUpdateInfo {

    private final ZonedDateTime updateTime;

    public enum Status {
        SUCCESS, FAILED, NONE;

        public boolean isSuccess() {
            return this.equals(SUCCESS);
        }
    }

    private final String presetId;
    private final String fullPath;
    private final ZonedDateTime lastUpdated;

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

    private String s3VersionId;
    private long s3WriteDurationMs;

    public ImageUpdateInfo(final String presetId, final String fullPath, final ZonedDateTime lastUpdated) {
        this.presetId = presetId;
        this.fullPath = fullPath;
        this.lastUpdated = lastUpdated;
        this.updateTime = DateHelper.zonedDateTimeNowAtUtc();
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

    public void setS3VersionId(final String versionId) {
        this.s3VersionId = versionId;
    }

    public String getS3VersionId() {
        return s3VersionId;
    }

    public void setS3WriteDurationMs(final long s3WriteDurationMs) {
        this.s3WriteDurationMs = s3WriteDurationMs;
    }

    public long getS3WriteDurationMs() {
        return s3WriteDurationMs;
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

    public int getSizeBytes() {
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

    public boolean isSuccess() {
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

    public ZonedDateTime getUpdateTime() {
        return updateTime;
    }

    public long getDiffBetweenModifiedAndUpdated() {
        return updateTime.toEpochSecond() - lastUpdated.toEpochSecond();
    }
}
