package fi.livi.digitraffic.tie.service.v1.datex2;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;

public class Datex2MessageDto {
    public final String message;
    public String jsonMessage;
    public final ZonedDateTime importTime;
    public final D2LogicalModel model;

    public Datex2MessageDto(final String message, final String jsonMessage,
                            final ZonedDateTime importTime,
                            final D2LogicalModel model) {
        this.message = message;
        this.jsonMessage = jsonMessage;
        this.importTime = importTime;
        this.model = model;
    }
}
