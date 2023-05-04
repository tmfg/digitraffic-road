DROP TRIGGER IF EXISTS camera_preset_history_modified_trigger on camera_preset_history;
DROP TRIGGER IF EXISTS maintenance_tracking_domain_modified_trigger on maintenance_tracking_domain;

DROP TRIGGER IF EXISTS maintenance_tracking_contract_modified_trigger on maintenance_tracking_domain_contract;
DROP TRIGGER IF EXISTS maintenance_tracking_domain_task_mapping_modified_trigger on maintenance_tracking_domain_task_mapping;

DROP TRIGGER IF EXISTS maintenance_tracking_task_value_modified_trigger on maintenance_tracking_task_value;

DROP TRIGGER IF EXISTS tms_sensor_constant_modified_trigger on tms_sensor_constant;
DROP TRIGGER IF EXISTS tms_sensor_constant_value_modified_trigger on tms_sensor_constant_value;

DROP TRIGGER IF EXISTS trg_camera_preset_delete on camera_preset;
DROP FUNCTION IF EXISTS trg_camera_preset_delete$camera_preset;

DROP TRIGGER IF EXISTS trigger_vc$camera_preset on camera_preset;
DROP FUNCTION IF EXISTS f_trigger_vc$camera_preset;

DROP TRIGGER IF EXISTS camera_preset_history_preset_seq_trigger on camera_preset_history;
DROP FUNCTION IF EXISTS update_preset_seq_column;

DROP TRIGGER IF EXISTS datex2_situation_record_update_effective_end_time_t ON datex2_situation_record;
DROP FUNCTION IF EXISTS update_datex2_situation_record__effective_end_time;

DROP TRIGGER IF EXISTS trigger_vc$forecast_section on forecast_section;
DROP FUNCTION IF EXISTS f_trigger_vc$forecast_section;

DROP TRIGGER IF EXISTS trigger_vc$forecast_section_weather ON forecast_section_weather;
DROP FUNCTION IF EXISTS f_trigger_vc$forecast_section_weather;

DROP TRIGGER IF EXISTS trigger_vc$road_station on road_station;
DROP FUNCTION IF EXISTS f_trigger_vc$road_station;

DROP TRIGGER IF EXISTS trg_weather_station_delete on weather_station;
DROP FUNCTION IF EXISTS trg_weather_station_delete$weather_station;

DROP TRIGGER IF EXISTS trg_lam_station_delete on tms_station;
DROP FUNCTION IF EXISTS trg_lam_station_delete$lam_station;

DROP TRIGGER IF EXISTS trigger_vc$road_station_sensor on road_station_sensor;
DROP FUNCTION IF EXISTS f_trigger_vc$road_station_sensor;
