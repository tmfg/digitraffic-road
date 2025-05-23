package fi.livi.digitraffic.tie.model.weathercam;

import java.time.Instant;

import org.hibernate.annotations.DynamicUpdate;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.ReadOnlyCreatedAndModifiedFields;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

@Entity
@DynamicUpdate
public class CameraPresetHistory extends ReadOnlyCreatedAndModifiedFields {

    @EmbeddedId
    private CameraPresetHistoryPK id;
    @Column(nullable = false)
    private Long cameraPresetId;
    @Column(nullable = false)
    private Instant lastModified; // Image last modified
    @Column(nullable = false)
    private Boolean publishable;
    @Column(nullable = false)
    private Integer size;
    @Column(nullable = false)
    private String cameraId;
    @Column(nullable = false)
    private Boolean presetPublic;

    // For Hibernate
    public CameraPresetHistory() {
    }

    public CameraPresetHistory(final @NotNull String presetId, final @NotNull String versionId, final long cameraPresetId,
                               final Instant lastModified, final boolean publishable, final int size, final boolean presetPublic) {
        this.id = new CameraPresetHistoryPK(presetId, versionId);
        this.cameraPresetId = cameraPresetId;
        this.lastModified = lastModified;
        this.publishable = publishable;
        this.size = size;
        this.cameraId = presetId.substring(0,6);
        this.presetPublic = presetPublic;
    }

    public CameraPresetHistoryPK getId() {
        return id;
    }

    public String getPresetId() {
        return id.getPresetId();
    }

    public String getCameraId() {
        return cameraId;
    }

    public String getVersionId() {
        return id.getVersionId();
    }

    public Long getCameraPresetId() {
        return cameraPresetId;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public Boolean getPublishable() {
        return publishable;
    }

    public void setPublishable(final Boolean publishable) {
        this.publishable = publishable;
    }

    public Integer getSize() {
        return size;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
