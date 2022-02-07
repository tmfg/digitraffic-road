package fi.livi.digitraffic.tie.dto;

import java.util.List;

public class WazeFeedAnnouncementDto {
    public final List<WazeFeedIncidentsDto> incidents;

    public WazeFeedAnnouncementDto(final List<WazeFeedIncidentsDto> incidents) {
        this.incidents = incidents;
    }
}