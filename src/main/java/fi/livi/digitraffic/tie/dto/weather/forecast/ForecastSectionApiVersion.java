package fi.livi.digitraffic.tie.dto.weather.forecast;

import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSectionType;

public enum ForecastSectionApiVersion {
    V1(1, ForecastSectionType.SIMPLE),
    V2(2, ForecastSectionType.DETAILED);

    private final int version;
    private final ForecastSectionType type;

    ForecastSectionApiVersion(final int version, final ForecastSectionType type) {
        this.version = version;
        this.type = type;
    }

    public ForecastSectionType getType() {
        return type;
    }

    public int getVersion() {
        return version;
    }
}
