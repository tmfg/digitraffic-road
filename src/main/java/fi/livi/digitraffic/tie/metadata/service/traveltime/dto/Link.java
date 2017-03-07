package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class Link {

    public final int linkno;

    public final int startsite;

    public final int endsite;

    @JacksonXmlElementWrapper(useWrapping = false)
    public final List<Name> name;

    public final Distance distance;

    public final int dirindex;

    public final List<IntermediateSite> intermediates;

    public final Freeflowspeed freeflowspeed;

    public Link(@JsonProperty("linkno") final int linkno,
                @JsonProperty("startsite") final int startsite,
                @JsonProperty("endsite") final int endsite,
                @JsonProperty("name") final List<Name> name,
                @JsonProperty("distance") final Distance distance,
                @JsonProperty("dirindex") final int dirindex,
                @JsonProperty("intermediates") final List<IntermediateSite> intermediates,
                @JsonProperty("freeflowspeed") final Freeflowspeed freeflowspeed) {
        this.distance = distance;
        this.dirindex = dirindex;
        this.intermediates = intermediates;
        this.name = name;
        this.startsite = startsite;
        this.freeflowspeed = freeflowspeed;
        this.endsite = endsite;
        this.linkno = linkno;
    }
}
