package fi.livi.digitraffic.tie.metadata.model;

public enum MetadataType {

    CAMERA_STATION,
    LAM_STATION,
    WEATHER_STATION,
    // ROAD_STATION_SENSOR,
    WEATHER_STATION_SENSOR,
    LAM_ROAD_STATION_SENSOR,
    FORACAST_SECTION,
    LOCATIONS
    ;


    public static MetadataType getForRoadStationType(final RoadStationType roadStationType) {
        if (RoadStationType.LAM_STATION == roadStationType) {
            return LAM_ROAD_STATION_SENSOR;
        } else if (RoadStationType.WEATHER_STATION == roadStationType) {
            return WEATHER_STATION_SENSOR;
        }
        throw new IllegalArgumentException("No metadata type for " + roadStationType);
    }
}
