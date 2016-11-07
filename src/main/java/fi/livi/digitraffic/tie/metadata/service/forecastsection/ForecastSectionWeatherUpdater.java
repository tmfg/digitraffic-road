package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ForecastSectionWeatherUpdater {

    private final ForecastSectionClient forecastSectionClient;

    @Autowired
    public ForecastSectionWeatherUpdater(ForecastSectionClient forecastSectionClient) {
        this.forecastSectionClient = forecastSectionClient;
    }

    @Transactional
    public void updateForecastSectionWeather() {

        ForecastSectionDataDto data = forecastSectionClient.getRoadConditions();
    }
}