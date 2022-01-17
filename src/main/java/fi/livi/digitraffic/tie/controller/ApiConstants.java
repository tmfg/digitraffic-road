package fi.livi.digitraffic.tie.controller;

public final class ApiConstants {

    public static final String API = "/api";
    private static final String V1 = "/v1";
    private static final String V2 = "/v2"; // Just for example for now
    public static final String BETA = "/beta";

    /* Traffic messages */
    public static final String TRAFFIC_MESSAGE_TAG = "Traffic messages (BETA)";
    private static final String API_TRAFFIC_MESSAGE = API + "/traffic-message";
    private static final String API_TRAFFIC_MESSAGE_BETA = API_TRAFFIC_MESSAGE + BETA;
    private static final String API_TRAFFIC_MESSAGE_V1 = API_TRAFFIC_MESSAGE + V1;

    private static final String MESSAGES = "/messages";
    public static final String API_TRAFFIC_MESSAGE_V1_MESSAGES = API_TRAFFIC_MESSAGE_BETA + MESSAGES;
    private static final String DATEX2 = "/datex2";
    private static final String SIMPLE = "/simple";
    private static final String AREA_GEOMETRIES = "/area-geometries";
    public static final String API_TRAFFIC_MESSAGES_V1_DATEX2 = API_TRAFFIC_MESSAGE_V1_MESSAGES + DATEX2;
    public static final String API_TRAFFIC_MESSAGES_V1_SIMPLE = API_TRAFFIC_MESSAGE_V1_MESSAGES + SIMPLE;
    public static final String API_TRAFFIC_MESSAGES_V1_AREA_GEOMETRIES = API_TRAFFIC_MESSAGE_BETA + AREA_GEOMETRIES;

    private ApiConstants() {}
}
