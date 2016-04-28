package fi.livi.digitraffic.tie.data.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Latest measurement data from LAM stations.")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "lamData"})
public class LamDataObject extends DataObject {

    private final List<LamMeasurement> lamData;

    public LamDataObject(final List<LamMeasurement> lamData) {
        this.lamData = lamData;
    }

    public List<LamMeasurement> getLamData() {
        return lamData;
    }
}
