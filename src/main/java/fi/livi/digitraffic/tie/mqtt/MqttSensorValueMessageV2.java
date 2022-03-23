package fi.livi.digitraffic.tie.mqtt;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;

import static fi.livi.digitraffic.tie.helper.MqttUtil.getEpochSeconds;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqttSensorValueMessageV2 {
    public final double value;
    public final long time;
    public final Long windowStart;
    public final Long windowEnd;

    public MqttSensorValueMessageV2(final SensorValueDto sv) {
        this.value = sv.getSensorValue();
        this.time = getEpochSeconds(sv.getUpdatedTime());
        this.windowStart = getEpochSeconds(sv.getTimeWindowStart());
        this.windowEnd = getEpochSeconds(sv.getTimeWindowEnd());
    }
}
