package fi.livi.digitraffic.tie.mqtt;

import static fi.livi.digitraffic.common.util.TimeUtil.getEpochSeconds;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqttSensorValueMessageV2 {
    public final double value;
    public final long time;
    public final Long start;
    public final Long end;

    public MqttSensorValueMessageV2(final SensorValueDtoV1 sv) {
        this.value = sv.getValue();
        this.time = getEpochSeconds(sv.getModified());
        this.start = getEpochSeconds(sv.getTimeWindowStart());
        this.end = getEpochSeconds(sv.getTimeWindowEnd());
    }
}
