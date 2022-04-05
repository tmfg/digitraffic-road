package fi.livi.digitraffic.tie.mqtt;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;

import static fi.livi.digitraffic.tie.helper.MqttUtil.getEpochSeconds;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqttSensorValueMessageV2 {
    public final double value;
    public final long time;
    public final Long start;
    public final Long end;

    public MqttSensorValueMessageV2(final SensorValueDto sv) {
        this.value = sv.getSensorValue();
        this.time = getEpochSeconds(sv.getUpdatedTime());
        this.start = getEpochSeconds(sv.getTimeWindowStart());
        this.end = getEpochSeconds(sv.getTimeWindowEnd());
    }
}
