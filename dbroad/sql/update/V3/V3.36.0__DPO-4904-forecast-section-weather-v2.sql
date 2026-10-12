-- DPO-4904: Weather forecast V2 data (new forecast data source). Separate from forecast_section_weather,
-- which is still used by weather API V1.
CREATE TABLE IF NOT EXISTS forecast_section_weather_v2
(
    forecast_section_id    NUMERIC(10, 0)           NOT NULL,
    forecast_name          VARCHAR(50)          NOT NULL,
    time                   TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    type                   VARCHAR(50)          NOT NULL,
    daylight               BOOLEAN                  NOT NULL,
    road_temperature       NUMERIC(5, 1),
    temperature            NUMERIC(5, 1),
    wind_speed             NUMERIC(5, 1),
    wind_direction         INTEGER,
    overall_road_condition VARCHAR(50),
    road_condition         VARCHAR(50),
    weather_symbol         INTEGER,
    created_at             TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    modified_at            TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT forecast_section_weather_v2_pk PRIMARY KEY (forecast_section_id, forecast_name),
    CONSTRAINT forecast_section_weather_v2_forecast_section_fk FOREIGN KEY (forecast_section_id)
        REFERENCES forecast_section (id) ON DELETE CASCADE
);

COMMENT ON TABLE forecast_section_weather_v2 IS 'Forecast section observation (type OBSERVATION, forecast_name 0h) and forecasts';
COMMENT ON COLUMN forecast_section_weather_v2.weather_symbol IS 'Smart symbol code, 100 is added to the code when it is dark';
