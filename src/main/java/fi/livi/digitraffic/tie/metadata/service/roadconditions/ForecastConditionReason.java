package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForecastConditionReason {

    public final Integer precipitationCondition;

    public final Integer roadCondition;

    public ForecastConditionReason(@JsonProperty("precipitationCondition") Integer precipitationCondition,
                                   @JsonProperty("roadCondition") Integer roadCondition) {
        this.precipitationCondition = precipitationCondition;
        this.roadCondition = roadCondition;
    }
}