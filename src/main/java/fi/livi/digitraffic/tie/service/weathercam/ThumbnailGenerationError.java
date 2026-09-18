package fi.livi.digitraffic.tie.service.weathercam;

import java.time.Instant;

public class ThumbnailGenerationError extends RuntimeException {
    private final String imageName;
    private final String versionId;
    private final Instant lastModified;
    private final String originalImageHash;
    private final double originalImageSize;

    public ThumbnailGenerationError(final String message, final String imageName, final String versionId, final Instant lastModified, final String originalImageHash, final double originalImageSize, final Throwable cause) {
        super(message, cause);
        this.imageName = imageName;
        this.versionId = versionId;
        this.lastModified = lastModified;
        this.originalImageHash = originalImageHash;
        this.originalImageSize = originalImageSize;
    }

    public String getImageName() {
        return imageName;
    }

    public String getVersionId() {
        return versionId;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public String getOriginalImageHash() {
        return originalImageHash;
    }

    public double getOriginalImageSize() {
        return originalImageSize;
    }
}
