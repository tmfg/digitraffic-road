package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LinkMetadataDto {

    public final TimestampDto timestamp;

    public final ZonedDateTime lastUpdate;

    public final String service;

    public final List<LinkDto> links;

    public final List<DirectionTextDto> directions;

    public final List<SiteDto> sites;

    public final String SUP;

    public final VersionDto version;

    public LinkMetadataDto(@JsonProperty("timestamp") final TimestampDto timestamp,
                           @JsonProperty("LastUpdate") final ZonedDateTime lastUpdate,
                           @JsonProperty("service") final String service,
                           @JsonProperty("linklist") final List<LinkDto> links,
                           @JsonProperty("directionlist") final List<DirectionTextDto> directions,
                           @JsonProperty("sitelist") final List<SiteDto> sites,
                           @JsonProperty("SUP") final String SUP,
                           @JsonProperty("version") final VersionDto version) {
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
