package fi.livi.digitraffic.tie.controller;

public final class ApiConstants {

    /** API base */
    public static final String API = "/api";
    /** API versions */
    public static final String V1 = "/v1";
    public static final String BETA = "/beta";
    public static final String INTEGRATION = API + "/integration";

    public static final String LAST_UPDATED_PARAM = "lastUpdated";

    /*
     * API data types
     */

    public static final String XML = ".xml";

    /** Info api */

    public static final String INFO_TAG_V1 = "Info V1";
    public static final String API_INFO = API + "/info";

    /** Traffic messages */
    public static final String TRAFFIC_MESSAGE_TAG_V1 = "Traffic message V1";
    public static final String TRAFFIC_MESSAGE_BETA_TAG = "Traffic message (BETA)";
    public static final String API_TRAFFIC_MESSAGE = API + "/traffic-message";

    /** Maintenance trackings */
    public static final String MAINTENANCE_TAG_V1 = "Maintenance V1";
    public static final String MAINTENANCE_BETA_TAG = "Maintenance (BETA)";
    public static final String API_MAINTENANCE = API + "/maintenance";

    /** Weathercam */
    public static final String WEATHERCAM_TAG_V1 = "Weathercam V1";
    public static final String WEATHERCAM_BETA_TAG = "Weathercam (BETA)";
    public static final String API_WEATHERCAM = API + "/weathercam";

    /* Waze feed */
    public static final String API_WAZEFEED = INTEGRATION + "/waze";

    /** TMS */
    public static final String TMS_TAG_V1 = "TMS V1";
    public static final String TMS_BETA_TAG = "TMS (BETA)";
    public static final String API_TMS = API + "/tms";

    public static final String WEATHER_TAG_V1 = "Weather V1";
    public static final String API_WEATHER = API + "/weather";
    /* Variable Sign */
    public static final String VARIABLE_SIGN_TAG_V1 = "Variable Sign V1";
    public static final String API_VS_V1 = API + "/variable-sign/v1";
    public static final String API_SIGNS = "/signs";
    public static final String API_SIGNS_DATEX2 = "/signs.datex2";
    public static final String API_SIGNS_HISTORY = API_SIGNS + "/history";
    public static final String API_SIGNS_CODE_DESCRIPTIONS = API_SIGNS + "/code-descriptions";

    /* Counting site */
    public static final String API_COUNTING_SITE_V1 = API + "/counting-site/v1";
    public static final String API_COUNTING_SITE_V1_COUNTERS = API_COUNTING_SITE_V1 + "/counters";
    public static final String API_COUNTING_SITE_V1_DIRECTIONS = API_COUNTING_SITE_V1 + "/directions";
    public static final String API_COUNTING_SITE_V1_DOMAIN = API_COUNTING_SITE_V1 + "/domain";
    public static final String API_COUNTING_SITE_V1_USER_TYPES = API_COUNTING_SITE_V1 + "/user-types";
    public static final String API_COUNTING_SITE_V1_VALUES = API_COUNTING_SITE_V1 + "/values";


    private ApiConstants() {}
}
