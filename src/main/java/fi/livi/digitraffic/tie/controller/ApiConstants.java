package fi.livi.digitraffic.tie.controller;

public final class ApiConstants {

    public static final String API = "/api";
    private static final String V1 = "/v1";
    private static final String V2 = "/v2"; // Just for example for now
    private static final String LATEST = "/latest";
    public static final String BETA = "/beta";

    /* Traffic messages */
    private static final String API_TRAFFIC_MESSAGES = API + "/traffic-messages";
    private static final String API_TRAFFIC_MESSAGES_V1 = API_TRAFFIC_MESSAGES + V1;
    private static final String API_TRAFFIC_MESSAGES_V2 = API_TRAFFIC_MESSAGES + V2; // Just for example for now
    private static final String API_TRAFFIC_MESSAGES_LATEST = API_TRAFFIC_MESSAGES + LATEST;
    private static final String API_TRAFFIC_MESSAGES_BETA = API_TRAFFIC_MESSAGES + BETA;

    private static final String TRAFFIC_MESSAGES_DATEX2 = "/datex2";
    private static final String TRAFFIC_MESSAGES_SIMPLE = "/simple";
    private static final String TRAFFIC_MESSAGES_AREA_GEOMETRIES = "/area-geometries";

    public static final String TRAFFIC_MESSAGES_TAG = "Traffic messages";
    public static final String API_TRAFFIC_MESSAGES_DATEX2_V1 = API_TRAFFIC_MESSAGES_V1 + TRAFFIC_MESSAGES_DATEX2;
    public static final String API_TRAFFIC_MESSAGES_DATEX2_LATEST = API_TRAFFIC_MESSAGES_LATEST + TRAFFIC_MESSAGES_DATEX2;
    public static final String API_TRAFFIC_MESSAGES_DATEX2_BETA = API_TRAFFIC_MESSAGES_BETA + TRAFFIC_MESSAGES_DATEX2;

    public static final String API_TRAFFIC_MESSAGES_SIMPLE_V1 = API_TRAFFIC_MESSAGES_V1 + TRAFFIC_MESSAGES_SIMPLE;
    public static final String API_TRAFFIC_MESSAGES_SIMPLE_LATEST = API_TRAFFIC_MESSAGES_LATEST + TRAFFIC_MESSAGES_SIMPLE;
    public static final String API_TRAFFIC_MESSAGES_SIMPLE_BETA = API_TRAFFIC_MESSAGES_BETA + TRAFFIC_MESSAGES_SIMPLE;

    public static final String API_TRAFFIC_MESSAGES_AREA_GEOMETRIES_V1 = API_TRAFFIC_MESSAGES_V1 + TRAFFIC_MESSAGES_AREA_GEOMETRIES;
    public static final String API_TRAFFIC_MESSAGES_AREA_GEOMETRIES_LATEST = API_TRAFFIC_MESSAGES_LATEST + TRAFFIC_MESSAGES_AREA_GEOMETRIES;
    public static final String API_TRAFFIC_MESSAGES_AREA_GEOMETRIES_BETA = API_TRAFFIC_MESSAGES_BETA + TRAFFIC_MESSAGES_AREA_GEOMETRIES;

    private ApiConstants() {}
}
