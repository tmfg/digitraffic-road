DROP INDEX   rs_sensor_rs_type_fk_i;
CREATE INDEX rs_sensor_rs_type_fk_i
  ON road_station_sensor
  USING BTREE (road_station_type ASC, lotju_id ASC, publishable DESC);