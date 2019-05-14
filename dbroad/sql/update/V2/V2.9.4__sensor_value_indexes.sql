drop index if exists sensor_value_measured_i; -- road_station_id, road_station_sensor_id, measured -> duplicate

alter table sensor_value drop constraint if exists sensor_value_uk1; -- reorder
alter table sensor_value add constraint sensor_value_uk unique(road_station_id, road_station_sensor_id);