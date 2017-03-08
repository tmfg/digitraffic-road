package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Link {

    public final int linkno;

    public final int startsite;

    public final int endsite;

    @JacksonXmlElementWrapper(useWrapping = false, localName = "names")
    @JacksonXmlProperty(localName = "name")
    public final List<Name> names;

    public final Distance distance;

    public final int dirindex;

    public final List<IntermediateSite> intermediates;

    public final FreeFlowSpeed freeFlowSpeed;

    public Link(@JsonProperty("linkno") final int linkno,
                @JsonProperty("startsite") final int startsite,
                @JsonProperty("endsite") final int endsite,
                @JsonProperty("names") final List<Name> names,
                @JsonProperty("distance") final Distance distance,
                @JsonProperty("dirindex") final int dirindex,
                @JsonProperty("intermediates") final List<IntermediateSite> intermediates,
                @JsonProperty("freeflowspeed") final FreeFlowSpeed freeFlowSpeed) {
        this.distance = distance;
        this.dirindex = dirindex;
        this.intermediates = intermediates;
        this.names = names;
        this.startsite = startsite;
        this.freeFlowSpeed = freeFlowSpeed;
        this.endsite = endsite;
        this.linkno = linkno;
    }
}
