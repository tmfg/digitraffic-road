package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesV1;
import fi.livi.digitraffic.tie.model.v1.camera.CameraType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam properties")
@JsonPropertyOrder({ "id", "name", "cameraType" })
public class WeathercamPropertiesV1 extends RoadStationPropertiesV1<String> {

    @Schema(description = "Weathercam id")
    private String id;

    @Schema(description = "Type of camera")
    private CameraType cameraType;

    /** Nearest weather station natural id */
    @Schema(description = "Nearest weather station id")
    private Long nearestWeatherStationId;

    @Schema(description = "Camera presets")
    private List<WeathercamPresetV1> presets = new ArrayList<>();


    public void setCameraType(final CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setNearestWeatherStationId(final Long nearestWeatherStationId) {
        this.nearestWeatherStationId = nearestWeatherStationId;
    }

    public Long getNearestWeatherStationId() {
        return nearestWeatherStationId;
    }

    public List<WeathercamPresetV1> getPresets() {
        return presets;
    }

    public void setPresets(final List<WeathercamPresetV1> presets) {
        this.presets = presets;
    }

    public void addPreset(final WeathercamPresetV1 preset) {
        this.presets.add(preset);
        Collections.sort(this.presets);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final WeathercamPropertiesV1 that = (WeathercamPropertiesV1) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(cameraType, that.cameraType)
                .append(nearestWeatherStationId, that.nearestWeatherStationId)
                .append(presets, that.presets)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(cameraType)
                .append(nearestWeatherStationId)
                .append(presets)
                .toHashCode();
    }
}
