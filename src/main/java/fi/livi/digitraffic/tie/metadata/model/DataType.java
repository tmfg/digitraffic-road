package fi.livi.digitraffic.tie.metadata.model;

public enum DataType {

    CAMERA_STATION_METADATA,
    TMS_STATION_METADATA,
    WEATHER_STATION_METADATA,
    WEATHER_STATION_SENSOR_METADATA,
    TMS_STATION_SENSOR_METADATA,
    FORECAST_SECTION_METADATA,
    FORECAST_SECTION_WEATHER_DATA,
    LOCATIONS_METADATA,
    LOCATION_TYPES_METADATA,
    TRAVEL_TIME_MEDIANS_DATA,
    TRAVEL_TIME_MEASUREMENTS_DATA,
    TRAVEL_TIME_LINKS_METADATA,
    LINK_FREE_FLOW_SPEEDS_DATA,
    TMS_FREE_FLOW_SPEEDS_DATA
    ;


    public static DataType getSensorMetadataTypeForRoadStationType(final RoadStationType roadStationType) {
        if (RoadStationType.TMS_STATION == roadStationType) {
            return TMS_STATION_SENSOR_METADATA;
        } else if (RoadStationType.WEATHER_STATION == roadStationType) {
            return WEATHER_STATION_SENSOR_METADATA;
        }
        throw new IllegalArgumentException("No metadata type for " + roadStationType);
    }
}
