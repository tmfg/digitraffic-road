package fi.livi.digitraffic.tie.dto.wazefeed;

import java.util.List;

public class WazeFeedAnnouncementDto {
    public final List<WazeFeedIncidentDto> incidents;

    public WazeFeedAnnouncementDto(final List<WazeFeedIncidentDto> incidents) {
        this.incidents = incidents;
    }
}