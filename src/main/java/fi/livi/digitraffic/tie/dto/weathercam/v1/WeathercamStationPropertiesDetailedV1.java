package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesDetailedV1;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.weathercam.CameraType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam station properties object with detailed information")
@JsonPropertyOrder({ "id", "name", "cameraType", "nearestWeatherStationId" })
public class WeathercamStationPropertiesDetailedV1 extends RoadStationPropertiesDetailedV1<String> {

    @Schema(description = "Weathercam presets")
    private List<WeathercamPresetDetailedV1> presets = new ArrayList<>();

    public List<WeathercamPresetDetailedV1> getPresets() {
        return presets;
    }

    @Schema(description = "Type of camera")
    public final CameraType cameraType;

    /** Nearest weather station natural id */
    @Schema(description = "Nearest weather station id")
    public final Long nearestWeatherStationId;

    public WeathercamStationPropertiesDetailedV1(final String cameraId, final CameraType cameraType,
                                                 final Long nearestWeatherStationId) {
        super(cameraId);
        this.cameraType = cameraType;
        this.nearestWeatherStationId = nearestWeatherStationId;
    }

    public void setPresets(final List<WeathercamPresetDetailedV1> presets) {
        this.presets = presets;
    }

    public void addPreset(final WeathercamPresetDetailedV1 preset) {
        this.presets.add(preset);
        setDataUpdatedTime(DateHelper.getGreatest(getDataUpdatedTime(), preset.getModified()));
        Collections.sort(this.presets);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final WeathercamStationPropertiesDetailedV1 that = (WeathercamStationPropertiesDetailedV1) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(cameraType, that.cameraType)
                .append(nearestWeatherStationId, that.nearestWeatherStationId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(cameraType)
                .append(nearestWeatherStationId)
                .toHashCode();
    }
}
