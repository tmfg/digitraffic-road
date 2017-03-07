package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinkMetadataDto {

    public final Timestamp timestamp;

    public final ZonedDateTime lastUpdate;

    public final String service;

    public final List<Link> links;

    public final List<DirectionText> directions;

    public final List<Site> sites;

    public final String SUP;

    public final Version version;

    public LinkMetadataDto(@JsonProperty("timestamp") final Timestamp timestamp,
                           @JsonProperty("LastUpdate") final ZonedDateTime lastUpdate,
                           @JsonProperty("service") final String service,
                           @JsonProperty("linklist") final List<Link> links,
                           @JsonProperty("directionlist") final List<DirectionText> directions,
                           @JsonProperty("sitelist") final List<Site> sites,
                           @JsonProperty("SUP") final String SUP,
                           @JsonProperty("version") final Version version) {
        this.timestamp = timestamp;
        this.lastUpdate = lastUpdate;
        this.service = service;
        this.links = links;
        this.directions = directions;
        this.sites = sites;
        this.SUP = SUP;
        this.version = version;
    }
}
