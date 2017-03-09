package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class LinkDto {

    public final int linkNumber;

    public final int startSite;

    public final int endSite;

    @JacksonXmlElementWrapper(useWrapping = false, localName = "names")
    @JacksonXmlProperty(localName = "name")
    public final List<NameDto> names;

    public final DistanceDto distance;

    public final int directionIndex;

    public final List<IntermediateSiteDto> intermediates;

    public final FreeFlowSpeedDto freeFlowSpeed;

    public LinkDto(@JsonProperty("linkno") final int linkNumber,
                   @JsonProperty("startsite") final int startSite,
                   @JsonProperty("endsite") final int endSite,
                   @JsonProperty("names") final List<NameDto> names,
                   @JsonProperty("distance") final DistanceDto distance,
                   @JsonProperty("dirindex") final int directionIndex,
                   @JsonProperty("intermediates") final List<IntermediateSiteDto> intermediates,
                   @JsonProperty("freeflowspeed") final FreeFlowSpeedDto freeFlowSpeed) {
        this.distance = distance;
        this.directionIndex = directionIndex;
        this.intermediates = intermediates;
        this.names = names;
        this.startSite = startSite;
        this.freeFlowSpeed = freeFlowSpeed;
        this.endSite = endSite;
        this.linkNumber = linkNumber;
    }
}
