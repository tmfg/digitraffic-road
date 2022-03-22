package fi.livi.digitraffic.tie.mqtt;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;

public class MqttDataMessage {
    private final String topic;
    private final Object data;

    public static MqttDataMessage createV2(final String topic, final SensorValueDto sv) {
        final MqttSensorValueMessageV2 msvm = new MqttSensorValueMessageV2(sv);

        return new MqttDataMessage(topic, msvm);
    }

    public MqttDataMessage(final String topic, final Object data) {
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
        return "DataMessage{topic: '" + topic + ", data: " + data + '}';
    }
}
