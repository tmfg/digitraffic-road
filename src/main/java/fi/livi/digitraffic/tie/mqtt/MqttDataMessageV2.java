package fi.livi.digitraffic.tie.mqtt;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;

public class MqttDataMessageV2 {
    private final String topic;
    private final Object data;

    public static MqttDataMessageV2 createV2(final String topic, final SensorValueDtoV1 sv) {
        final MqttSensorValueMessageV2 msvm = new MqttSensorValueMessageV2(sv);

        return new MqttDataMessageV2(topic, msvm);
    }

    public MqttDataMessageV2(final String topic, final Object data) {
        this.topic = topic;
        this.data = data;
    }

    public String getTopic() {
        return topic;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "MqttDataMessageV2{topic: '" + topic + ", data: " + data + '}';
    }
}
