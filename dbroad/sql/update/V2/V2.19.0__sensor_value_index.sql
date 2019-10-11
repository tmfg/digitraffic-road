drop index sensor_value_search_i;

create index sensor_value_search_i on sensor_value(measured, road_station_id);
COMMENT ON INDEX sensor_value_search_i is 'Used to get the latest measurements';