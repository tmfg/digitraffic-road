-- add modified and created columns to road station
ALTER TABLE road_station
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();
-- trigger to update modified column
DROP TRIGGER IF EXISTS ROAD_STATION_MODIFIED_T on road_station;
CREATE TRIGGER ROAD_STATION_MODIFIED_T BEFORE UPDATE ON road_station FOR EACH ROW EXECUTE PROCEDURE update_modified_column();



-- add modified and created columns to tms_station
ALTER TABLE tms_station
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();
-- trigger to update modified column
DROP TRIGGER IF EXISTS TMS_STATION_MODIFIED_T on tms_station;
CREATE TRIGGER TMS_STATION_MODIFIED_T BEFORE UPDATE ON tms_station FOR EACH ROW EXECUTE PROCEDURE update_modified_column();



-- add modified and created columns to weather_station
ALTER TABLE weather_station
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();
-- trigger to update modified column
DROP TRIGGER IF EXISTS WEATHER_STATION_MODIFIED_T on weather_station;
CREATE TRIGGER WEATHER_STATION_MODIFIED_T BEFORE UPDATE ON weather_station FOR EACH ROW EXECUTE PROCEDURE update_modified_column();



-- add modified and created columns to camera_preset
ALTER TABLE camera_preset
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();
-- trigger to update modified column
DROP TRIGGER IF EXISTS CAMERA_PRESET_MODIFIED_T on camera_preset;
CREATE TRIGGER CAMERA_PRESET_MODIFIED_T BEFORE UPDATE ON camera_preset FOR EACH ROW EXECUTE PROCEDURE update_camera_preset_modified_column();

