package fi.livi.digitraffic.tie.data.service.datex2;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

public class Datex2MessageDto {
    public final String message;
    public final ZonedDateTime importTime;
    public final D2LogicalModel model;

    public Datex2MessageDto(final String message, final ZonedDateTime importTime, final D2LogicalModel model) {
        this.message = message;
        this.importTime = importTime;
        this.model = model;
    }
}
