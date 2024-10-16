package fi.livi.digitraffic.tie.service.trafficmessage;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.TrafficAnnouncementType;

public class Datex2UpdateValues {
    public final String message;
    public final String jsonMessage;
    public final ZonedDateTime importTime;
    public final D2LogicalModel model;
    public final SituationType situationType;
    public final TrafficAnnouncementType trafficAnnouncementType;
    public final String situationId;
    public final String originalJsonMessage;

    public Datex2UpdateValues(final D2LogicalModel model,
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
        this.originalJsonMessage = null;
    }

    public Datex2UpdateValues(final D2LogicalModel model,
                              final SituationType situationType,
                              final TrafficAnnouncementType trafficAnnouncementType,
                              final String message,
                              final String jsonMessage,
                              final ZonedDateTime importTime,
                              final String situationId,
                              final String originalJsonMessage) {
        this.situationType = situationType;
        this.trafficAnnouncementType = trafficAnnouncementType;
        this.message = message;
        this.jsonMessage = jsonMessage;
        this.importTime = importTime;
        this.model = model;
        this.situationId = situationId;
        this.originalJsonMessage = originalJsonMessage;
    }
}
