
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Location to display in ETRS-TM35FIN coordinate format.", value = "LocationToDisplayV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "e",
    "n"
})
public class LocationToDisplay {

    @ApiModelProperty(value = "ETRS-TM35FIN east coordinate", required = true, position = 1)
    public Double e;

    @ApiModelProperty(value = "ETRS-TM35FIN north coordinate", required = true, position = 1)
    public Double n;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public LocationToDisplay(Double e, Double n) {
        super();
        this.e = e;
        this.n = n;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
