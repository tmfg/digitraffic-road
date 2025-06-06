package fi.livi.digitraffic.tie.model.weathercam;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class CameraPresetHistoryPK implements Serializable {

    @Column(nullable = false)
    private String presetId;
    @Column(nullable = false)
    private String versionId;

    public CameraPresetHistoryPK() {
    }

    CameraPresetHistoryPK(final @NotNull String presetId, final @NotNull String versionId) {
        this.presetId = presetId;
        this.versionId = versionId;
    }

    public String getPresetId() {
        return presetId;
    }

    public String getVersionId() {
        return versionId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final CameraPresetHistoryPK that = (CameraPresetHistoryPK) o;

        return new EqualsBuilder()
                .append(presetId, that.presetId)
                .append(versionId, that.versionId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(presetId)
                .append(versionId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
