package fi.livi.digitraffic.tie.service.v1.datex2;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2DetailedMessageType;

public class Datex2MessageDto {
    public final String message;
    public final String jsonMessage;
    public final ZonedDateTime importTime;
    public final D2LogicalModel model;
    public final Datex2DetailedMessageType messageType;
    public final String situationId;

    public Datex2MessageDto(final D2LogicalModel model,
                            final Datex2DetailedMessageType messageType,
                            final String message,
                            final String jsonMessage,
                            final ZonedDateTime importTime,
                            final String situationId) {
        this.message = message;
        this.jsonMessage = jsonMessage;
        this.importTime = importTime;
        this.model = model;
        this.messageType = messageType;
        this.situationId = situationId;
    }
}
