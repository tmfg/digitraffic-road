package fi.livi.digitraffic.tie.mqtt;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqttStatusMessageV2 {
    public final Long updated;
    public final Long lastError;

    public MqttStatusMessageV2(final Long updated, final Long lastError) {
        this.updated = updated;
        this.lastError = lastError;
    }
}
