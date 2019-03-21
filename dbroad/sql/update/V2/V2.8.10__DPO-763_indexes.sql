CREATE INDEX publishable_weather_sensor_lotju_id_i
  ON road_station_sensor
  USING BTREE (lotju_id ASC)
  WHERE publishable = true AND road_station_sensor.road_station_type = 'WEATHER_STATION';

CREATE INDEX publishable_tms_sensor_lotju_id_i
  ON road_station_sensor
  USING BTREE (lotju_id ASC)
  WHERE publishable = true AND road_station_sensor.road_station_type = 'TMS_STATION';

-- test index drops
drop index if exists sensor_value_measured;
drop index if exists sensor_value_measured_i_test2;
drop index if exists sensor_value_road_station_fk_i;
drop index if exists sensor_value_search_u;