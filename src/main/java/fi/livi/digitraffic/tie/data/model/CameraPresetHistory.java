package fi.livi.digitraffic.tie.data.model;

import java.time.ZonedDateTime;

public class CameraPresetHistory {

    private Long cameraPresetId;
    private String presetId;
    private String versionId;
    private ZonedDateTime lastModified;
    private Boolean publishable;
    private Integer size;
    private ZonedDateTime created;

    public CameraPresetHistory(final long cameraPresetId, final String presetId, final String versionId, final ZonedDateTime lastModified,
                               final boolean publishable, final int size, final ZonedDateTime created) {
        this.cameraPresetId = cameraPresetId;
        this.presetId = presetId;
        this.versionId = versionId;
        this.lastModified = lastModified;
        this.publishable = publishable;
        this.size = size;
        this.created = created;
    }
}
