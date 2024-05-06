-- Prevent deletion of camera, tms and weather stations
DROP TRIGGER IF EXISTS camera_preset_prevent_delete_t ON camera_preset;
CREATE TRIGGER camera_preset_prevent_delete_t
  BEFORE DELETE
  ON camera_preset
  FOR EACH STATEMENT
EXECUTE PROCEDURE raise_prevent_delete_exception();

DROP TRIGGER IF EXISTS tms_station_prevent_delete_t ON tms_station;
CREATE TRIGGER tms_station_prevent_delete_t
  BEFORE DELETE
  ON tms_station
  FOR EACH STATEMENT
EXECUTE PROCEDURE raise_prevent_delete_exception();

DROP TRIGGER IF EXISTS weather_station_prevent_delete_t ON weather_station;
CREATE TRIGGER weather_station_prevent_delete_t
  BEFORE DELETE
  ON weather_station
  FOR EACH STATEMENT
EXECUTE PROCEDURE raise_prevent_delete_exception();

DROP TRIGGER IF EXISTS update_camera_preset_publishable_t ON camera_preset;
CREATE TRIGGER update_camera_preset_publishable_t
  BEFORE INSERT OR UPDATE
  ON camera_preset
  FOR EACH ROW
EXECUTE PROCEDURE update_camera_preset_publishable();

DROP TRIGGER IF EXISTS update_camera_preset_history_preset_seq_column_t ON camera_preset_history;
CREATE TRIGGER update_camera_preset_history_preset_seq_column_t
  BEFORE INSERT
  ON camera_preset_history
  FOR EACH ROW
EXECUTE PROCEDURE update_camera_preset_history_preset_seq_column();

DROP TRIGGER IF EXISTS update_datex2_situation_record_effective_end_time_t ON datex2_situation_record;
CREATE TRIGGER update_datex2_situation_record_effective_end_time_t
  BEFORE INSERT OR UPDATE
  ON datex2_situation_record
  FOR EACH ROW
EXECUTE PROCEDURE update_datex2_situation_record_effective_end_time();


DROP TRIGGER IF EXISTS update_forecast_section_weather_type_t ON forecast_section_weather;
CREATE TRIGGER update_forecast_section_weather_type_t
  BEFORE INSERT OR UPDATE
  ON forecast_section_weather
  FOR EACH ROW
EXECUTE PROCEDURE update_forecast_section_weather_type();

DROP TRIGGER IF EXISTS update_road_station_publishable_t ON road_station;
CREATE TRIGGER update_road_station_publishable_t
  BEFORE INSERT OR UPDATE
  ON road_station
  FOR EACH ROW
EXECUTE PROCEDURE update_road_station_publishable();

DROP TRIGGER IF EXISTS road_station_sensor_publishable_t ON road_station_sensor;
CREATE TRIGGER road_station_sensor_publishable_t
  BEFORE INSERT OR UPDATE
  ON road_station_sensor
  FOR EACH ROW
EXECUTE PROCEDURE road_station_sensor_publishable();

-- updates maintenance_tracking task-column after insert in maintenance_tracking_task table
DROP TRIGGER IF EXISTS maintenance_tracking_task_t ON maintenance_tracking_task;
CREATE TRIGGER maintenance_tracking_task_t
  AFTER INSERT
  ON maintenance_tracking_task
  FOR EACH ROW
EXECUTE PROCEDURE maintenance_tracking_task_update();

-- pic_last_modified is the source system update time, keep track of update time on Digitraffic side with column pic_last_updated_db
DROP TRIGGER IF EXISTS camera_preset_pic_last_modified_db_t ON camera_preset;
CREATE TRIGGER camera_preset_pic_last_modified_db_t BEFORE UPDATE ON camera_preset FOR EACH ROW EXECUTE PROCEDURE update_pic_last_modified_db_column();
