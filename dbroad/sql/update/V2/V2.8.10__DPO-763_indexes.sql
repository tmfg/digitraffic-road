-- These indexes are used by SensorValueDao's UPDATE and INSERT queries.
CREATE INDEX publishable_weather_sensor_lotju_id_i
  ON road_station_sensor
  USING BTREE (lotju_id ASC)
  WHERE publishable = true AND road_station_sensor.road_station_type = 'WEATHER_STATION';

CREATE INDEX publishable_tms_sensor_lotju_id_i
  ON road_station_sensor
  USING BTREE (lotju_id ASC)
  WHERE publishable = true AND road_station_sensor.road_station_type = 'TMS_STATION';