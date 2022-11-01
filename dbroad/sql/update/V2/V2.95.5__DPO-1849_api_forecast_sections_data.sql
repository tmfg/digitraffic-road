ALTER TABLE forecast_section_weather
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();

ALTER TABLE forecast_condition_reason
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();


DROP TRIGGER IF EXISTS forecast_section_weather_modified_t on forecast_section_weather;
CREATE TRIGGER forecast_section_weather_modified_t BEFORE UPDATE ON forecast_section_weather FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

DROP TRIGGER IF EXISTS forecast_condition_reason_modified_t on forecast_condition_reason;
CREATE TRIGGER forecast_condition_reason_modified_t BEFORE UPDATE ON forecast_condition_reason FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

CREATE INDEX forecast_section_geometry_i ON forecast_section USING GIST (geometry);