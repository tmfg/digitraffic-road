package fi.livi.digitraffic.tie.dto.v1.trafficsigns;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface TrafficSignHistory {
    @JsonInclude
    String getDisplayValue();
    String getAdditionalInformation();
    ZonedDateTime getEffectDate();
    String getCause();
    List<HistoryTextRow> getRows();
}
