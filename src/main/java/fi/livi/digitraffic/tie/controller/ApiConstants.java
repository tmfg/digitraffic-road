package fi.livi.digitraffic.tie.controller;

public final class ApiConstants {

    /** API base */
    public static final String API = "/api";
    /** API versions */
    public static final String V1 = "/v1";
    public static final String BETA = "/beta";
    public static final String INTEGRATION = API + "/integration";

    public static final String LAST_UPDATED_PARAM = "lastUpdated";

    /**
     * API data types
     */

    /** Traffic messages */
    public static final String TRAFFIC_MESSAGE_TAG = "Traffic message";
    public static final String TRAFFIC_MESSAGE_BETA_TAG = "Traffic message (BETA)";
    public static final String API_TRAFFIC_MESSAGE = API + "/traffic-message";

    /** Maintenance trackings */
    public static final String MAINTENANCE_TAG = "Maintenance";
    public static final String MAINTENANCE_BETA_TAG = "Maintenance (BETA)";
    public static final String API_MAINTENANCE = API + "/maintenance";

    /** Weathercam */
    public static final String WEATHERCAM_TAG = "Weathercam";
    public static final String WEATHERCAM_BETA_TAG = "Weathercam (BETA)";
    public static final String API_WEATHERCAM = API + "/weathercam";

    /* Waze feed */
    public static final String API_WAZEFEED = INTEGRATION + "/waze";

    private ApiConstants() {}
}