package fi.livi.digitraffic.tie.service.weathercam;

import java.util.Date;

public class ThumbnailGenerationError extends RuntimeException {
    private final String imageName;
    private final String versionId;
    private final Date lastModified;

    public ThumbnailGenerationError(final String message, final String imageName, final String versionId, final Date lastModified, final Throwable cause) {
        super(message, cause);
        this.imageName = imageName;
        this.versionId = versionId;
        this.lastModified = lastModified;
    }

    public String getImageName() {
        return imageName;
    }

    public String getVersionId() {
        return versionId;
    }

    public Date getLastModified() {
        return lastModified;
    }
}
