package fi.livi.digitraffic.tie.data.dto.trafficsigns;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface TrafficSignHistory {
    String getDisplayValue();
    String getAdditionalInformation();
    ZonedDateTime getEffectDate();
    String getCause();
}
