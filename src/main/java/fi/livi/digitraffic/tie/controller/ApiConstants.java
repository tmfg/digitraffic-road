package fi.livi.digitraffic.tie.controller;

public final class ApiConstants {

    public static final String API = "/api";
    public static final String V1 = "/v1";
    public static final String V2 = "/v2"; // Just for example for now
    public static final String BETA = "/beta";

    /* Traffic messages */
    public static final String TRAFFIC_MESSAGES_TAG = "Traffic messages (BETA)";
    private static final String API_TRAFFIC_MESSAGES = API + "/traffic-messages";
    private static final String API_TRAFFIC_MESSAGES_BETA = API_TRAFFIC_MESSAGES + BETA;
    private static final String API_TRAFFIC_MESSAGES_V1 = API_TRAFFIC_MESSAGES + V1;
    private static final String API_TRAFFIC_MESSAGES_V2 = API_TRAFFIC_MESSAGES + V2; // Just for example for now

    private static final String TRAFFIC_MESSAGES_DATEX2 = "/datex2";
    private static final String TRAFFIC_MESSAGES_SIMPLE = "/simple";
    private static final String TRAFFIC_MESSAGES_AREA_GEOMETRIES = "/area-geometries";

    public static final String API_TRAFFIC_MESSAGES_V1_DATEX2 = API_TRAFFIC_MESSAGES_BETA + TRAFFIC_MESSAGES_DATEX2;
    public static final String API_TRAFFIC_MESSAGES_V2_DATEX2 = API_TRAFFIC_MESSAGES_V2 + TRAFFIC_MESSAGES_DATEX2; // Just for example for now
    public static final String API_TRAFFIC_MESSAGES_V1_SIMPLE = API_TRAFFIC_MESSAGES_BETA + TRAFFIC_MESSAGES_SIMPLE;
    public static final String API_TRAFFIC_MESSAGES_V2_SIMPLE = API_TRAFFIC_MESSAGES_V2 + TRAFFIC_MESSAGES_SIMPLE; // Just for example for now
    public static final String API_TRAFFIC_MESSAGES_V1_AREA_GEOMETRIES = API_TRAFFIC_MESSAGES_BETA + TRAFFIC_MESSAGES_AREA_GEOMETRIES;

    /* Maintenance trackings */
    public static final String MAINTENANCE_TAG = "Maintenance";
    public static final String MAINTENANCE_BETA_TAG = "Maintenance (BETA)";
    public static final String API_MAINTENANCE = API + "/maintenance";

    private ApiConstants() {}
}
