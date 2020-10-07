package fi.livi.digitraffic.tie.model.v1.datex2;

import org.apache.commons.lang3.StringUtils;

public enum Datex2DetailedMessageType {
    // Order is important as it is used to find first match and text of roadwork
    // can contain ie. "Erikoiskuljetusta varten" or "Erikoiskuljetusreitti".
    ROADWORK(Datex2MessageType.ROADWORK, "Tietyö"),
    WEIGHT_RESTRICTION(Datex2MessageType.WEIGHT_RESTRICTION, "Painorajoitus"),
    TRAFFIC_ANNOUNCEMENT(Datex2MessageType.TRAFFIC_INCIDENT, "Liikennetiedote"),
    PRELIMINARY_ANNOUNCEMENT(Datex2MessageType.TRAFFIC_INCIDENT, "Ensitiedote"),
    EXEMPTED_TRANSPORT(Datex2MessageType.TRAFFIC_INCIDENT, "Erikoiskuljetus"),
    UNCONFIRMED_OBSERVATION(Datex2MessageType.TRAFFIC_INCIDENT, "Vahvistamaton havainto"),
    UNKNOWN(Datex2MessageType.TRAFFIC_INCIDENT, null);

    private final String token;
    private final Datex2MessageType datex2MessageType;

    Datex2DetailedMessageType(final Datex2MessageType datex2MessageType, final String textToFind) {
        this.token = textToFind;
        this.datex2MessageType = datex2MessageType;
    }

    public Datex2MessageType getDatex2MessageType() {
        return datex2MessageType;
    }

    /**
     * Finds for token in given text and if match found returns type of matched token.
     * @param findFrom Text to search from
     * @return found type of null
     */
    public static Datex2DetailedMessageType findTypeForText(final String findFrom) {
        for(Datex2DetailedMessageType type : Datex2DetailedMessageType.values()) {
            if ( StringUtils.contains(findFrom, type.token) ) {
                return type;
            }
        }
        // Not found
        return UNKNOWN;
    }

    public String getToken() {
        return token;
    }
}
