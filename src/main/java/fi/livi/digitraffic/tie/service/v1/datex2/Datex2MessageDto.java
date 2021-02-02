package fi.livi.digitraffic.tie.service.v1.datex2;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;

public class Datex2MessageDto {
    public final String message;
    public final String jsonMessage;
    public final ZonedDateTime importTime;
    public final D2LogicalModel model;
    public final SituationType situationType;
    public final TrafficAnnouncementType trafficAnnouncementType;
    public final String situationId;

    public Datex2MessageDto(final D2LogicalModel model,
                            final SituationType situationType,
                            final TrafficAnnouncementType trafficAnnouncementType,
                            final String message,
                            final String jsonMessage,
                            final ZonedDateTime importTime,
                            final String situationId) {
        this.situationType = situationType;
        this.trafficAnnouncementType = trafficAnnouncementType;
        this.message = message;
        this.jsonMessage = jsonMessage;
        this.importTime = importTime;
        this.model = model;
        this.situationId = situationId;
    }
}
