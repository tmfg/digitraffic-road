package fi.livi.digitraffic.tie.model.v1.datex2;

import io.swagger.v3.oas.annotations.media.Schema;

@Deprecated
@Schema(description = "Datex2 message type", name = "Datex2MessageType", enumAsRef = true)
public enum Datex2MessageType {
    TRAFFIC_INCIDENT,
    ROADWORK,
    WEIGHT_RESTRICTION;

    public String toParameter() {
        return name().toLowerCase().replace("_", "-");
    }

}
