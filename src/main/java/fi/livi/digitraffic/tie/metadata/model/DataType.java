package fi.livi.digitraffic.tie.metadata.model;

public enum DataType {

    CAMERA_STATION_METADATA,
    CAMERA_STATION_METADATA_CHECK,
    TMS_STATION_METADATA,
    TMS_STATION_METADATA_CHECK,
    TMS_STATION_SENSOR_CONSTANT_METADATA,
    TMS_STATION_SENSOR_CONSTANT_METADATA_CHECK,
    WEATHER_STATION_METADATA,
    WEATHER_STATION_METADATA_CHECK,
    WEATHER_STATION_SENSOR_METADATA,
    WEATHER_STATION_SENSOR_METADATA_CHECK,
    TMS_STATION_SENSOR_METADATA,
    TMS_STATION_SENSOR_METADATA_CHECK,
    FORECAST_SECTION_METADATA,
    FORECAST_SECTION_METADATA_CHECK,
    FORECAST_SECTION_WEATHER_DATA,
    LOCATIONS_METADATA,
    LOCATIONS_METADATA_CHECK,
    LOCATION_TYPES_METADATA,
    LOCATION_TYPES_METADATA_CHECK,
    TRAVEL_TIME_MEDIANS_DATA,
    TRAVEL_TIME_MEASUREMENTS_DATA,
    TRAVEL_TIME_LINKS_METADATA,
    TRAVEL_TIME_LINKS_METADATA_CHECK,
    TMS_FREE_FLOW_SPEEDS_DATA,
    TMS_SENSOR_CONSTANT_METADATA,
    TMS_SENSOR_CONSTANT_METADATA_CHECK,
    TMS_SENSOR_CONSTANT_VALUE_DATA,
    TMS_SENSOR_CONSTANT_VALUE_DATA_CHECK,
    ;


    public static DataType getSensorMetadataTypeForRoadStationType(final RoadStationType roadStationType) {
        if (RoadStationType.TMS_STATION == roadStationType) {
            return TMS_STATION_SENSOR_METADATA;
        } else if (RoadStationType.WEATHER_STATION == roadStationType) {
            return WEATHER_STATION_SENSOR_METADATA;
        }
        throw new IllegalArgumentException("No metadata type for " + roadStationType);
    }

    public static DataType getSensorMetadataCheckTypeForRoadStationType(RoadStationType roadStationType) {
        if (RoadStationType.TMS_STATION == roadStationType) {
            return TMS_STATION_SENSOR_METADATA_CHECK;
        } else if (RoadStationType.WEATHER_STATION == roadStationType) {
            return WEATHER_STATION_SENSOR_METADATA_CHECK;
        }
        throw new IllegalArgumentException("No metadata type for " + roadStationType);
    }
}
