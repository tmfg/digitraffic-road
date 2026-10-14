package fi.livi.digitraffic.tie.service.weather.forecast;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSectionType;

/**
 * Forecast data sets of the S3 source. Each data set has its own files, forecast sections and update times.
 */
public enum ForecastDataSetV2 {
    DETAILED(ForecastSectionType.DETAILED, "roadweather_forecast_public_full_",
             DataType.FORECAST_SECTION_WEATHER_DATA_V2, DataType.FORECAST_SECTION_WEATHER_DATA_V2_CHECK),
    SIMPLE(ForecastSectionType.SIMPLE, "roadweather_forecast_public_simple_",
           DataType.FORECAST_SECTION_SIMPLE_WEATHER_DATA_V2, DataType.FORECAST_SECTION_SIMPLE_WEATHER_DATA_V2_CHECK);

    public final ForecastSectionType sectionType;
    public final String fileNamePrefix;
    public final DataType dataUpdatedType;
    public final DataType dataCheckedType;

    ForecastDataSetV2(final ForecastSectionType sectionType, final String fileNamePrefix,
                      final DataType dataUpdatedType, final DataType dataCheckedType) {
        this.sectionType = sectionType;
        this.fileNamePrefix = fileNamePrefix;
        this.dataUpdatedType = dataUpdatedType;
        this.dataCheckedType = dataCheckedType;
    }
}
