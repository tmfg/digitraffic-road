package fi.livi.digitraffic.tie.model.data;

import java.time.Instant;

public interface SituationMessage {
    String getSituationId();
    String getSituationType();
    String getMessageType();
    String getMessageVersion();
    String getMessage();
    Instant getModifiedAt();
}
