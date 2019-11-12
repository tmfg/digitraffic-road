package fi.livi.digitraffic.tie.metadata.dto;

import java.time.LocalDateTime;

public interface FlywayVersion {
    String getVersion();
    LocalDateTime getInstalledOn();
    Boolean getSuccess();
}
