
package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location consisting of one or more areas", name = "AreaLocation_OldV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "areas"
})
public class AreaLocation extends JsonAdditionalProperties {

    @Schema(description = "List of areas", required = true)
    @NotNull
    public List<Area> areas = new ArrayList<>();

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public AreaLocation() {
    }

    public AreaLocation(List<Area> areas) {
        super();
        this.areas = areas;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
