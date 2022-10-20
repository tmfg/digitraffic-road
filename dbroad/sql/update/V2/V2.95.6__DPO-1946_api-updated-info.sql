ALTER TABLE road_station_sensors
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();

DROP TRIGGER IF EXISTS road_station_sensors_modified_t on road_station_sensors;
CREATE TRIGGER road_station_sensors_modified_t BEFORE UPDATE ON road_station_sensors FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

ALTER TABLE code_description
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();

DROP TRIGGER IF EXISTS code_description_modified_t on code_description;
CREATE TRIGGER code_description_modified_t BEFORE UPDATE ON code_description FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

create index if not exists road_station_type_id_modified_i on road_station using btree (road_station_type, id, modified);
create index if not exists tms_station_modified_i on tms_station using btree (modified);
create index if not exists weather_station_modified_i on weather_station using btree (modified);
create index if not exists road_station_sensors_rs_id_modified_i on road_station_sensors using btree (road_station_id, modified);
create index if not exists sensor_value_rs_id_updated_i on sensor_value using btree (road_station_id, updated);
create index if not exists device_updated_i on device using btree (updated_date);
create index if not exists device_data_created_i on device_data using btree (created_date);
create index if not exists device_data_datex2_updated_i on device_data_datex2 using btree (updated_timestamp);
create index if not exists counting_site_domain_added_i on counting_site_domain using btree (added_timestamp);
create index if not exists counting_site_counter_added_i on counting_site_counter using btree (added_timestamp);
create index if not exists counting_site_data_timestamp_i on counting_site_data using btree (data_timestamp);
