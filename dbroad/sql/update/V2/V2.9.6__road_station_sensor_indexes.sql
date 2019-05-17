create unique index road_station_sensor_search_tms_u on road_station_sensor(lotju_id, id) where road_station_type = 'TMS_STATION' and publishable = true;
create unique index road_station_sensor_search_weather_u on road_station_sensor(lotju_id, id) where road_station_type = 'WEATHER_STATION' and publishable = true;

drop index road_station_sensor_search_u;

create index sensor_value_search_i on sensor_value(measured);
COMMENT ON INDEX sensor_value_search_i is 'Used to get the latest measurements';