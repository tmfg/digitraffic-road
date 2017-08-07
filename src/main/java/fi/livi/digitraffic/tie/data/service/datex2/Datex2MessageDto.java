package fi.livi.digitraffic.tie.data.service.datex2;

import java.sql.Timestamp;

import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

public class Datex2MessageDto {

    public final String message;
    public final Timestamp timestamp;
    public final D2LogicalModel model;

    public Datex2MessageDto(final String message, final Timestamp timestamp, final D2LogicalModel model) {
        this.message = message;
        this.timestamp = timestamp;
        this.model = model;
    }
}
