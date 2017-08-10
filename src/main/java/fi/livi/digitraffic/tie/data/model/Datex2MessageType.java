package fi.livi.digitraffic.tie.data.model;

public enum Datex2MessageType {
    TRAFFIC_DISORDER("traffic_disorder"),
    ROADWORK("roadwork");

    private final String value;

    Datex2MessageType(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Datex2MessageType fromValue(final String value) {
        return valueOf(value);
    }
}
