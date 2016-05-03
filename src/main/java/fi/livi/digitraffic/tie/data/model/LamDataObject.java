package fi.livi.digitraffic.tie.data.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Latest measurement data from LAM stations.")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "lamData"})
public class LamDataObject extends DataObject {

    @ApiModelProperty(value = "Latest measurement data from LAM stations", required = true)
    private final List<LamMeasurement> lamData;

    public LamDataObject(final List<LamMeasurement> lamData) {
        this.lamData = lamData;
    }

    public List<LamMeasurement> getLamData() {
        return lamData;
    }
}
