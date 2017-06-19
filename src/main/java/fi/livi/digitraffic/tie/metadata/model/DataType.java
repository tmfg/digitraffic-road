package fi.livi.digitraffic.tie.metadata.model;

public enum DataType {

    CAMERA_STATION_METADATA,
    LAM_STATION_METADATA,
    WEATHER_STATION_METADATA,
    WEATHER_STATION_SENSOR,
    LAM_ROAD_STATION_SENSOR,
    FORECAST_SECTION,
    FORECAST_SECTION_WEATHER,
    LOCATIONS,
    LOCATION_TYPES,
    TRAVEL_TIME_MEDIANS,
    TRAVEL_TIME_MEASUREMENTS,
    TRAVEL_TIME_LINKS
    ;


    public static DataType getForRoadStationType(final RoadStationType roadStationType) {
        if (RoadStationType.TMS_STATION == roadStationType) {
            return LAM_ROAD_STATION_SENSOR;
        } else if (RoadStationType.WEATHER_STATION == roadStationType) {
            return WEATHER_STATION_SENSOR;
        }
        throw new IllegalArgumentException("No metadata type for " + roadStationType);
    }
}
