package fi.livi.digitraffic.tie.data.websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import jdk.nashorn.internal.ir.annotations.Immutable;

@Immutable
public class TmsMessage {

    public final SensorValueDto sensorValue;

    @JsonCreator
    public TmsMessage(@JsonProperty("data") final SensorValueDto sensorValue) {
        this.sensorValue = sensorValue;
    }

    @Override
    public String toString() {
        return ToStringHelpper.toStringFull(this);
    }
}
