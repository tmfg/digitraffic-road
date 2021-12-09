package fi.livi.digitraffic.tie.controller;

public final class ApiConstants {

    public static final String API_BASE = "/api";
    private static final String V1 = "/v1";
    private static final String V2 = "/v2";
    private static final String V3 = "/v3";
    public static final String API_BETA_BASE = API_BASE + "/beta";

    /* Traffic messages */
    public static final String TRAFFIC_MESSAGES_TAG = "Traffic messages";
    public static final String TRAFFIC_MESSAGES_TAG_DESCRIPTION = "Traffic messages";
    public static final String TRAFFIC_MESSAGES_BASE = API_BASE + "/traffic-messages";
    public static final String TRAFFIC_MESSAGES_V1 = TRAFFIC_MESSAGES_BASE + V1;
    public static final String TRAFFIC_MESSAGES_V2 = TRAFFIC_MESSAGES_BASE + V2;
    public static final String TRAFFIC_MESSAGES_V3 = TRAFFIC_MESSAGES_BASE + V3;

    public static final String TRAFFIC_MESSAGES_DATEX2 = "/datex2";
    public static final String TRAFFIC_MESSAGES_SIMPLE = "/simple";
    public static final String TRAFFIC_MESSAGES_AREA_GEOMETRIES = "/area-geometries";

    private ApiConstants() {}
}
