package fi.livi.digitraffic.tie.controller;

public final class ApiConstants {

    /** API base */
    public static final String API = "/api";
    /** API versions */
    public static final String V1 = "/v1";
    public static final String BETA = "/beta";
    public static final String INTEGRATION = API + "/integration";

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

    /* Waze feed */
    public static final String API_WAZEFEED = INTEGRATION + "/waze";

    /** All deprecations */
    public static final String DEPRECATED_2022_11_01 = "2022-11-01";

    private ApiConstants() {}
}