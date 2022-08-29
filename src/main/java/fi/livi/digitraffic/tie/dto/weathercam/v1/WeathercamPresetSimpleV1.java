package fi.livi.digitraffic.tie.dto.weathercam.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam preset object with basic information")
@JsonPropertyOrder({ "id", "cameraId", "presentationName" })
public class WeathercamPresetSimpleV1 implements Comparable<WeathercamPresetSimpleV1>{

    @JsonIgnore
    @Schema(description = "Id of the camera that the preset belongs to")
    public final String cameraId;

    @Schema(description = "Id of preset")
    public final String id;
    @Schema(description = "Is preset in collection")
    public final boolean inCollection;

    public WeathercamPresetSimpleV1(final String presetId, final String cameraId, final boolean inCollection) {
        this.id = presetId;
        this.cameraId = cameraId;
        this.inCollection = inCollection;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final WeathercamPresetSimpleV1 that = (WeathercamPresetSimpleV1) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(cameraId, that.cameraId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(cameraId)
                .toHashCode();
    }

    @Override
    public int compareTo(final WeathercamPresetSimpleV1 other) {
        if (this.id != null && other.id == null) {
            return -1;
        } else if (this.id == null && other.id != null) {
            return 1;
        } else if (this.id == null) {
            return 0;
        }
        return this.id.compareTo(other.id);
    }
}
