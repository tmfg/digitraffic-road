package fi.livi.digitraffic.tie.mqtt;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqttSensorValueMessageV2 {
    public final double value;
    public final long time;
    public final Long windowStart;
    public final Long windowEnd;

    public MqttSensorValueMessageV2(final SensorValueDto sv) {
        this.value = sv.getSensorValue();
        this.time = sv.getUpdatedTime().toEpochSecond();
        this.windowStart = sv.getTimeWindowStart() != null ? sv.getTimeWindowStart().toEpochSecond() : null;
        this.windowEnd = sv.getTimeWindowEnd() != null ? sv.getTimeWindowEnd().toEpochSecond() : null;
    }
}
