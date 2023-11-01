package fi.livi.digitraffic.tie.dto.weather.forecast.client;

public class ForecastSectionV2FeatureDto {

    private String type;

    private ForecastSectionV2Geometry geometry;

    private ForecastSectionV2PropertiesDto properties;

    public ForecastSectionV2FeatureDto() {
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public ForecastSectionV2Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(final ForecastSectionV2Geometry geometry) {
        this.geometry = geometry;
    }

    public ForecastSectionV2PropertiesDto getProperties() {
        return properties;
    }

    public void setProperties(final ForecastSectionV2PropertiesDto properties) {
        this.properties = properties;
    }
}
