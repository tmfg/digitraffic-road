package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class TravelTimeMeasurementLinkDto {

    @JsonProperty("id")
    public long linkNaturalId;

    @JsonProperty("ir")
    public List<TravelTimeMeasurementDto> measurements = new ArrayList<>();

    @JsonSetter("id")
    public void setLinkNaturalId(long linkNaturalId) {
        this.linkNaturalId = linkNaturalId;
    }

    @JsonSetter("ir")
    public void setMeasurement(TravelTimeMeasurementDto measurement) {
        this.measurements.add(measurement);
    }
}
