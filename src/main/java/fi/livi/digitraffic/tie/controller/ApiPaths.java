package fi.livi.digitraffic.tie.controller;

/**
 * These will be removed eventually.
 * @see ApiConstants
 */
@Deprecated()
public final class ApiPaths {
    public static final String API_V1_BASE_PATH = "/api/v1";
    public static final String API_V2_BASE_PATH = "/api/v2";
    public static final String API_V3_BASE_PATH = "/api/v3";
    public static final String API_BETA_BASE_PATH = "/api/beta";

    public static final String API_METADATA_PART_PATH = "/metadata";
    public static final String API_DATA_PART_PATH = "/data";

    public static final String WEATHERCAM_PATH = "/weathercam";

    public static final String TMS_STATIONS_PATH = "/tms-stations";
    public static final String TMS_STATIONS_TMS_NUMBER_PATH = TMS_STATIONS_PATH + "/tms-number";
    public static final String TMS_STATIONS_ROAD_NUMBER_PATH = TMS_STATIONS_PATH + "/road-number";
    public static final String TMS_STATIONS_ROAD_STATION_ID_PATH = TMS_STATIONS_PATH + "/road-station-id";

    public static final String TMS_STATIONS_AVAILABLE_SENSORS_PATH = "/tms-sensors";
    public static final String CAMERA_STATIONS_PATH = "/camera-stations";
    public static final String WEATHER_STATIONS_PATH = "/weather-stations";
    public static final String WEATHER_STATIONS_AVAILABLE_SENSORS_PATH = "/weather-sensors";

    public static final String FORECAST_SECTIONS_PATH = "/forecast-sections";
    public static final String LOCATIONS_PATH = "/locations";
    public static final String LOCATION_VERSIONS_PATH = "/location-versions";
    public static final String LOCATION_TYPES_PATH = "/location-types";

    public static final String CAMERA_DATA_PATH = "/camera-data";
    public static final String TMS_DATA_PATH = "/tms-data";
    public static final String WEATHER_DATA_PATH = "/weather-data";
    public static final String WEATHER_HISTORY_DATA_PATH = "/weather-history-data";
    public static final String TMS_SENSOR_CONSTANTS = "/tms-sensor-constants";
    public static final String FORECAST_SECTION_WEATHER_DATA_PATH = "/road-conditions";
    public static final String CAMERA_HISTORY_PATH = "/camera-history";

    // traffic-messages / datex2
    public static final String TRAFFIC_MESSAGES_PATH = "/traffic-messages";
    public static final String TRAFFIC_MESSAGES_DATEX2_PATH = TRAFFIC_MESSAGES_PATH + "/datex2";
    @Deprecated
    public static final String TRAFFIC_DATEX2_PATH = "/traffic-datex2";
    @Deprecated
    public static final String TRAFFIC_DISORDERS_DATEX2_PATH = "/traffic-disorders-datex2";
    @Deprecated
    public static final String ROADWORKS_DATEX2_PATH = "/roadworks-datex2";
    @Deprecated
    public static final String WEIGHT_RESTRICTIONS_DATEX2_PATH = "/weight-restrictions-datex2";

    private ApiPaths() {}
}