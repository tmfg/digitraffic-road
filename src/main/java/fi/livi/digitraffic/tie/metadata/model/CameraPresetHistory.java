package fi.livi.digitraffic.tie.metadata.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@DynamicUpdate
public class CameraPresetHistory {

    @EmbeddedId
    private CameraPresetHistoryPK id;
    @Column(nullable = false)
    private Long cameraPresetId;
    @Column(nullable = false)
    private ZonedDateTime lastModified;
    @Column(nullable = false)
    private Boolean publishable;
    @Column(nullable = false)
    private Integer size;
    @Column(nullable = false)
    private ZonedDateTime created;

    // For Hibernate
    public CameraPresetHistory() {
    }

    public CameraPresetHistory(final @NotNull String presetId, final @NotNull String versionId, final long cameraPresetId,
                               final @NotNull ZonedDateTime lastModified, final boolean publishable, final int size,
                               final @NotNull ZonedDateTime created) {
        this.id = new CameraPresetHistoryPK(presetId, versionId);
        this.cameraPresetId = cameraPresetId;
        this.lastModified = lastModified;
        this.publishable = publishable;
        this.size = size;
        this.created = created;
    }

    public CameraPresetHistoryPK getId() {
        return id;
    }

    public void setId(CameraPresetHistoryPK id) {
        this.id = id;
    }

    public String getPresetId() {
        return id.getPresetId();
    }

    public String getVersionId() {
        return id.getVersionId();
    }

    public Long getCameraPresetId() {
        return cameraPresetId;
    }

    public void setCameraPresetId(Long cameraPresetId) {
        this.cameraPresetId = cameraPresetId;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public Boolean getPublishable() {
        return publishable;
    }

    public void setPublishable(Boolean publishable) {
        this.publishable = publishable;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
