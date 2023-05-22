package fi.livi.digitraffic.tie.dto.variablesigns.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface TrafficSignHistoryV1 {
    @JsonInclude
    String getDisplayValue();
    String getAdditionalInformation();
    Instant getEffectDate();
    String getCause();
    List<HistoryTextRowV1> getRows();

    @JsonIgnore
    Instant getCreated();
}
