package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class Link {

    public final Distance distance;

    public final String dirindex;

    public final List<IntermediateSite> intermediates;

    @JacksonXmlElementWrapper(useWrapping = false)
    public final List<Name> name;

    public final String startsite;

    public final Freeflowspeed freeflowspeed;

    public final String endsite;

    public final String linkno;

    public Link(@JsonProperty("distance") final Distance distance,
                @JsonProperty("dirindex") final String dirindex,
                @JsonProperty("intermediates") final List<IntermediateSite> intermediates,
                @JsonProperty("name") final List<Name> name,
                @JsonProperty("startsite") final String startsite,
                @JsonProperty("freeflowspeed") final Freeflowspeed freeflowspeed,
                @JsonProperty("endsite") final String endsite,
                @JsonProperty("linkno") final String linkno) {
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
