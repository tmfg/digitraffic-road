package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.helper.DateHelper;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam station properties object with basic information")
@JsonPropertyOrder({ "id", "name", "cameraType" })
public class WeathercamStationPropertiesSimpleV1 extends RoadStationPropertiesSimpleV1<String> {

    @Schema(description = "Weathercam presets")
    private List<WeathercamPresetSimpleV1> presets = new ArrayList<>();

    public WeathercamStationPropertiesSimpleV1(final String cameraId) {
        super(cameraId);
    }

    public List<WeathercamPresetSimpleV1> getPresets() {
        return presets;
    }

    public void setPresets(final List<WeathercamPresetSimpleV1> presets) {
        this.presets = presets;
    }

    public void addPreset(final WeathercamPresetSimpleV1 preset) {
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

        final WeathercamStationPropertiesSimpleV1 that = (WeathercamStationPropertiesSimpleV1) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(presets, that.presets)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(presets)
                .toHashCode();
    }
}
