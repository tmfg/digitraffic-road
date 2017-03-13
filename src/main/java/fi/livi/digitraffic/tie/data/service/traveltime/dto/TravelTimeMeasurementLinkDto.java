package fi.livi.digitraffic.tie.data.service.traveltime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TravelTimeMeasurementLinkDto {

    public final long linkNaturalId;

    @JacksonXmlElementWrapper(useWrapping = false, localName = "measurements")
    @JacksonXmlProperty(localName = "ir")
    public final List<TravelTimeMeasurementDto> measurements;

    public TravelTimeMeasurementLinkDto(@JsonProperty("id") final long linkNaturalId,
                                        @JsonProperty("measurements") List<TravelTimeMeasurementDto> measurements) {
        this.linkNaturalId = linkNaturalId;
        this.measurements = measurements;
    }
}
