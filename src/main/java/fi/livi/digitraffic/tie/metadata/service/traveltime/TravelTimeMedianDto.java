package fi.livi.digitraffic.tie.metadata.service.traveltime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TravelTimeMedianDto {

    public final long linkNaturalId;

    public final long median;

    public final int numberOfObservations;

    public TravelTimeMedianDto(@JsonProperty("id") long linkNaturalId,
                               @JsonProperty("tt") long median,
                               @JsonProperty("n") int numberOfObservations) {
        this.linkNaturalId = linkNaturalId;
        this.median = median;
        this.numberOfObservations = numberOfObservations;
    }
}