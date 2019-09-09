package fi.livi.digitraffic.tie.metadata.geojson.trafficsigns;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Properties", description = "Traffic Sign properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficSignProperties {
    // device properties
    public final String id;
    public final String type;
    public final String roadAddress;
    @ApiModelProperty("Direction of dataflow")
    public final String direction;
    @ApiModelProperty("Traffic sign placement")
    public final Integer carriageway;

    // data properties
    public final String displayInformation;
    public final String additionalInformation;
    @ApiModelProperty(value = "Information is effect after this date")
    public final ZonedDateTime effectDate;
    public final String cause;

    public TrafficSignProperties(final String id, final String type, final String roadAddress, final String direction, final Integer lane,
        final String displayInformation, final String additionalInformation, final ZonedDateTime effectDate, final String cause) {
        this.id = id;
        this.type = type;
        this.roadAddress = roadAddress;
        this.direction = direction;
        this.carriageway = lane;
        this.displayInformation = displayInformation;
        this.additionalInformation = additionalInformation;
        this.effectDate = effectDate;
        this.cause = cause;
    }
}
