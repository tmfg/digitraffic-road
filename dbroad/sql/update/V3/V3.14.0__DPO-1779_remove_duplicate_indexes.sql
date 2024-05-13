-- Drop some of unused indexes
-- DROP INDEX IF EXISTS camera_preset_nearest_rws_fki; FK-index
DROP INDEX IF EXISTS camera_preset_publishable_lotju_i;
-- DROP INDEX IF EXISTS camera_preset_history_fki;
DROP INDEX IF EXISTS camera_preset_history_search_4_i;
DROP INDEX IF EXISTS counting_site_counter_added_i;
-- DROP INDEX IF EXISTS counting_site_counter_user_type_fki; FK-index
-- DROP INDEX IF EXISTS flyway_schema_history_s_idx; Small table
-- DROP INDEX IF EXISTS loc_loc_area_fk_i; FK-index
-- DROP INDEX IF EXISTS loc_loc_linear_fk_i; FK-index
-- DROP INDEX IF EXISTS ocpi_cpo_version_ocpi_version_fkey_i; FK-index
-- DROP INDEX IF EXISTS ocpi_location_cpo_modified_cpo_i; Not in use yet
-- DROP INDEX IF EXISTS idx_qrtz_ft_job_group; Small table
-- DROP INDEX IF EXISTS idx_qrtz_ft_job_name; Small table
-- DROP INDEX IF EXISTS idx_qrtz_ft_job_req_recovery; Small table
-- DROP INDEX IF EXISTS idx_qrtz_ft_trig_group; Small table
-- DROP INDEX IF EXISTS idx_qrtz_ft_trig_inst_name; Small table
-- DROP INDEX IF EXISTS idx_qrtz_ft_trig_name; Small table
-- DROP INDEX IF EXISTS idx_qrtz_j_req_recovery; Small table
-- DROP INDEX IF EXISTS idx_qrtz_t_nft_st; Small table
-- DROP INDEX IF EXISTS idx_qrtz_t_state; Small table
DROP INDEX IF EXISTS road_station_publishable_i;
-- DROP INDEX IF EXISTS situation_record_comment_fk_i; FK-index
DROP INDEX IF EXISTS tms_sensor_constant_modified_i;
DROP INDEX IF EXISTS tms_sensor_constant_value_modified_i;
DROP INDEX IF EXISTS tms_sensor_constant_value_valid_key;

-- Duplicate test index
DROP INDEX IF EXISTS maintenance_tracking_line_string_i_ccnew;
-- Replace duplicate indices with new index
DROP INDEX IF EXISTS device_data_effect_date_i;
DROP INDEX IF EXISTS device_data_test;
CREATE INDEX device_data_effect_date_i ON device_data(effect_date, device_id);

-- Create on index for mt observation data
DROP INDEX IF EXISTS maintenance_tracking_observation_data_handling_order_i;
DROP INDEX IF EXISTS maintenance_tracking_observation_data_status_time_id_i;
CREATE INDEX maintenance_tracking_observation_data_status_time_id_i
  ON MAINTENANCE_TRACKING_OBSERVATION_DATA
    USING BTREE (status, observation_time ASC, id ASC);
