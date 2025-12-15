package fi.livi.digitraffic.tie.model.data;

import java.time.Instant;

public interface SituationMqttMessage {
    String getMessage();
    Instant getModifiedAt();
    String getSituationType();
    String getMessageType();
    String getMessageVersion();
}
