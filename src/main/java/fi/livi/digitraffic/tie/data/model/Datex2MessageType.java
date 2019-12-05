package fi.livi.digitraffic.tie.data.model;

public enum Datex2MessageType {
    TRAFFIC_INCIDENT,
    ROADWORK,
    WEIGHT_RESTRICTION;

    public String toParameter() {
        return name().toLowerCase().replace("_", "-");
    }
}
