## Diagram

![diagram](./DATABASE.svg)

## Indexes

### `allowed_tms_sensor_constant`

- `alloved_tms_sensor_constant_pkey`

### `camera_preset`

- `camera_preset_id_key`
- `camera_preset_lotju_id_publishable_idx`
- `camera_preset_lotju_id_ui`
- `camera_preset_nearest_rws_fki`
- `camera_preset_pk`
- `camera_preset_publishable_i`
- `camera_preset_road_station_fki`

### `camera_preset_history`

- `camera_preset_history_fki`
- `camera_preset_history_last_modified_publishable_i`
- `camera_preset_history_modified_i`
- `camera_preset_history_preset_id_preset_seq_ui`
- `camera_preset_history_search_1_i`
- `camera_preset_history_search_2_i`
- `camera_preset_history_search_3_i`
- `camera_preset_history_search_5_i`
- `camera_preset_history_search_6_i`
- `preset_history_pk`

### `code_description`

- `code_description_pk`

### `counting_site_counter`

- `counting_site_counter_pkey`
- `counting_site_counter_user_type_fki`
- `counting_site_domain_site_key`

### `counting_site_data`

- `counting_site_data_counter_fki`
- `counting_site_data_modified_idx`
- `counting_site_data_pkey`
- `counting_site_data_timestamp_i`

### `counting_site_domain`

- `counting_site_domain_pkey`

### `counting_site_user_type`

- `counting_site_user_type_pkey`

### `cs2_data`

- `cs2_data_fetch2_i`
- `cs2_data_fetch_i`

### `cs2_site`

- `cs2_site_pkey`

### `data_datex2_situation`

- `data_datex2_situation_pkey`
- `data_datex2_situation_search_by_type_key`
- `data_datex2_situation_search_key`

### `data_datex2_situation_message`

- `data_datex2_situation_message_key`
- `data_datex2_situation_message_pkey`

### `data_incoming`

- `data_incoming_created_i`
- `data_incoming_pkey`
- `data_incoming_source_type_i`

### `data_source_info`

- `data_source_info_pkey`

### `data_updated`

- `data_updated_ui`
- `metad_updated_pk`

### `datex2`

- `datex2_id_json_message_not_null_key`
- `datex2_modified_type_i`
- `datex2_pk`
- `datex2_situation_type_id_i`
- `datex2_type_modified_i`

### `datex2_rtti`

- `datex2_rtti_pkey`
- `datex2_rtti_situation_publication_time`
- `datex2_rtti_start_time`

### `datex2_situation`

- `datex2_situation_pk`
- `datex2_situation_situation_id_id_i`
- `situation_datex2_fk_i`

### `datex2_situation_record`

- `datex2_situation_record_pk`
- `situation_record_situation_fki`

### `device`

- `device_id_not_deleted_key`
- `device_pkey`
- `device_updated_i`

### `device_data`

- `device_data_created_i`
- `device_data_effect_date_i`
- `device_data_effect_date_key`
- `device_data_modified_idx`
- `device_data_pkey`

### `device_data_datex2`

- `device_data_datex2_pkey`
- `device_data_datex2_updated_i`

### `device_data_row`

- `device_data_row_device_data_fkey`
- `device_data_row_pkey`

### `forecast_condition_reason`

- `foresecweather_reason_pk`

### `forecast_section`

- `forecast_section_geometry_i`
- `forecast_section_ui`
- `forecast_section_unique`
- `forecast_section_version_modified_i`
- `forecast_section_version_natural_id_key`
- `forsec_pk`

### `forecast_section_weather`

- `foresec_weather_pk`

### `link_id`

- `link_id_pk`

### `location`

- `loc_loc_area_fk_i`
- `loc_loc_linear_fk_i`
- `loc_lst_fk_i`
- `loc_pk`

### `location_subtype`

- `lst_pk`

### `location_type`

- `lty_pk`

### `location_version`

- `locv_pk`

### `locking_table`

- `locking_table_pkey`

### `maintenance_tracking`

- `maintenance_tracking_contract_fki`
- `maintenance_tracking_created_domain_end_time_i`
- `maintenance_tracking_domain_end_time_created_i`
- `maintenance_tracking_domain_wm_id_end_time_i`
- `maintenance_tracking_end_time_domain_created_i`
- `maintenance_tracking_geometry_i`
- `maintenance_tracking_last_point_i`
- `maintenance_tracking_not_finished_work_machine_id_i`
- `maintenance_tracking_pkey`
- `maintenance_tracking_previous_tracking_id_fki`
- `maintenance_tracking_tasks_i`
- `maintenance_tracking_work_machine_id_fkey_i`

### `maintenance_tracking_domain`

- `maintenance_tracking_domain_pkey`

### `maintenance_tracking_domain_contract`

- `maintenance_tracking_domain_contract_domain_contract_source_i`
- `maintenance_tracking_domain_contract_pkey`

### `maintenance_tracking_domain_task_mapping`

- `maintenance_tracking_task_value_map_pkey`

### `maintenance_tracking_observation_data`

- `maintenance_tracking_observation_data_hash_ui`
- `maintenance_tracking_observation_data_pkey`
- `maintenance_tracking_observation_data_status_time_id_i`

### `maintenance_tracking_observation_data_tracking`

- `maintenance_tracking_observation_data_tracking_pkey`
- `maintenance_tracking_observation_data_tracking_tracking_fkey_i`

### `maintenance_tracking_task`

- `maintenance_tracking_task_pkey`

### `maintenance_tracking_task_value`

- `maintenance_tracking_task_value_pkey`

### `maintenance_tracking_work_machine`

- `maintenance_tracking_work_machine_pkey`
- `mtwm_harja_id_urakka_id_ui`

### `ocpi_cpo`

- `ocpi_cpo_pkey`

### `ocpi_cpo_business_details`

- `ocpi_cpo_business_details_pkey`

### `ocpi_cpo_module_endpoint`

- `ocpi_cpo_module_endpoint_pkey`

### `ocpi_cpo_version`

- `ocpi_cpo_version_ocpi_version_fkey_i`
- `ocpi_cpo_version_pkey`

### `ocpi_location`

- `ocpi_location_cpo_modified_cpo_i`
- `ocpi_location_pkey`

### `ocpi_version`

- `ocpi_version_pkey`

### `open311_service`

- `open311_service_pkey`

### `open311_service_request`

- `open311_service_request_pkey`

### `open311_service_request_state`

- `open311_service_request_state_pkey`

### `open311_subject`

- `open311_subject_pkey`

### `open311_subsubject`

- `open311_subsubject_pkey`

### `permit`

- `permit_pkey`
- `permit_source_id_source_key`

### `pghero_query_stats`

- `pghero_query_stats_database_captured_at_idx`
- `pghero_query_stats_pkey`

### `pghero_space_stats`

- `pghero_space_stats_database_captured_at_idx`
- `pghero_space_stats_pkey`

### `qrtz_blob_triggers`

- `qrtz_blob_triggers_pkey`

### `qrtz_calendars`

- `qrtz_calendars_pkey`

### `qrtz_cron_triggers`

- `qrtz_cron_triggers_pkey`

### `qrtz_fired_triggers`

- `idx_qrtz_ft_job_group`
- `idx_qrtz_ft_job_name`
- `idx_qrtz_ft_job_req_recovery`
- `idx_qrtz_ft_trig_group`
- `idx_qrtz_ft_trig_inst_name`
- `idx_qrtz_ft_trig_name`
- `idx_qrtz_ft_trig_nm_gp`
- `qrtz_fired_triggers_pkey`

### `qrtz_job_details`

- `idx_qrtz_j_req_recovery`
- `qrtz_job_details_pkey`

### `qrtz_locks`

- `qrtz_locks_pkey`

### `qrtz_paused_trigger_grps`

- `qrtz_paused_trigger_grps_pkey`

### `qrtz_scheduler_state`

- `qrtz_scheduler_state_pkey`

### `qrtz_simple_triggers`

- `qrtz_simple_triggers_pkey`

### `qrtz_simprop_triggers`

- `qrtz_simprop_triggers_pkey`

### `qrtz_triggers`

- `idx_qrtz_t_nft_st`
- `idx_qrtz_t_state`
- `qrtz_triggers_pkey`

### `region_geometry`

- `region_geometry_code_commit_key`
- `region_geometry_pkey`

### `road_address`

- `road_address_pk`

### `road_segment`

- `road_segment_pk`

### `road_station`

- `road_station_lotju_i`
- `road_station_lotju_ui`
- `road_station_ni_i`
- `road_station_pk`
- `road_station_ra_ui`
- `road_station_type_fk_i`
- `road_station_type_id_modified_i`
- `road_station_ui`
- `rs_type_lotju_id_fk_ui`

### `road_station_sensor`

- `road_station_sensor_lotju_ui`
- `road_station_sensor_pk`
- `road_station_sensor_search_key`
- `road_station_sensor_search_tms_u`
- `road_station_sensor_search_weather_u`
- `road_station_sensor_uki`
- `road_station_sensor_uki_fi`
- `road_station_sensor_uki_name`
- `rs_sensor_rs_type_fk_i`
- `rss_haku_u`

### `road_station_sensors`

- `road_station_sensors_rs_id_modified_i`
- `road_station_sensors_rs_ufki`
- `road_station_sensors_rss_fk_i`

### `road_station_type`

- `road_station_type_pk`
- `road_station_type_u`

### `sensor_value`

- `sensor_value_pk`
- `sensor_value_rs_id_updated_i`
- `sensor_value_rs_measured_i`
- `sensor_value_rs_rss_measured_i`
- `sensor_value_search_i`
- `sensor_value_uk`
- `sensor_value_updated_i`

### `sensor_value_description`

- `svd_pk`
- `svd_sensor_fk_i`

### `sensor_value_history`

- `sensor_value_history_measured_i`
- `sensor_value_history_pkey`
- `sensor_value_history_road_station_i`

### `situation_record_comment_i18n`

- `situation_record_comment_fk_i`
- `situation_record_comment_pk`

### `tms_sensor_constant`

- `tms_sensor_constant_pkey`
- `u_tms_sensor_constant`

### `tms_sensor_constant_value`

- `tms_sensor_constant_value_fk_i`
- `tms_sensor_constant_value_pkey`

### `tms_station`

- `lam_station_pk`
- `lam_station_uk1`
- `lam_station_uk2`
- `tms_station_modified_i`

### `weather_station`

- `rws_pk`
- `rws_road_station_fki`
- `weather_station_lotju_i`
- `weather_station_modified_i`
