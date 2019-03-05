CREATE INDEX sensor_value_rs_measured_i
  ON sensor_value
  USING BTREE (road_station_sensor_id ASC, measured desc nulls last);

-- For Hibernate queries
CREATE INDEX camera_preset_publishable_lotju_i
  ON road.public.camera_preset
  USING BTREE (publishable DESC, lotju_id asc nulls last);

CREATE INDEX weather_station_lotju_i
  ON weather_station
  USING BTREE (lotju_id asc nulls last);

-- duplicate indexes
drop index dsr_search_i;
drop index situation_record_situation_fki;

CREATE INDEX situation_record_situation_fki
  ON datex2_situation_record
  USING BTREE (datex2_situation_id, validy_status, overall_end_time);

-- duplicate index with forecast_section_version
DROP INDEX forecast_section_index;
-- duplicate index with idx_qrtz_t_nft_st
DROP INDEX idx_qrtz_t_next_fire_time;
-- duplicate index with road_uk1
DROP INDEX road_index1;
-- duplicate index with road_section_uk1
DROP INDEX road_section_index3;