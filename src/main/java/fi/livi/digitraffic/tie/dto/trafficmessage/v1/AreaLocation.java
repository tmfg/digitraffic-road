
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Location consisting of one or more areas", name = "AreaLocationV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "areas"
})
public class AreaLocation extends JsonAdditionalProperties {

    @Schema(description = "List of areas", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public List<Area> areas = new ArrayList<>();

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
