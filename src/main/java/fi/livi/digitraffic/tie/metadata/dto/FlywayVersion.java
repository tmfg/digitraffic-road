package fi.livi.digitraffic.tie.metadata.dto;

import java.time.LocalDateTime;

public interface FlywayVersion {
    Integer getInstalledRank();
    String getVersion();
    String getDescription();
    String getType();
    String getScript();
    Integer getChecksum();
    String getInstalledBy();
    LocalDateTime getInstalledOn();
    Integer getExecutionTime();
    Boolean getSuccess();
}
