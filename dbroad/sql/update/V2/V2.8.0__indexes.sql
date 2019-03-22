-- sensor_value indexes

drop index if exists sensor_value_measured; -- road_station_id, measured
drop index if exists sensor_value_measured_i_test2; -- road_station_id, road_station_sensor_id, measured DESC -> rename
drop index if exists sensor_value_road_station_fk_i; -- road_station_id, measured -> duplicate

-- were for old max(measured) query, not used any more
drop index if exists sensor_value_search_u; -- measured, road_station_id, id, value, updated, road_station_sensor_id, time_window_start, time_window_end
drop index if exists sensor_value_remove; -- measured, road_station_sensor_id

CREATE INDEX sensor_value_rs_rss_measured_i ON public.sensor_value USING btree (road_station_id, road_station_sensor_id, measured DESC);
COMMENT ON INDEX sensor_value_rs_rss_measured_i is 'RoadStationSensorValueDtoRepository queries use this';


-- road_station indexes

drop index if exists road_station_search_i; -- publishable, type, id
drop index if exists road_station_search_i2; -- publishable, type, id, natural_id -> rename
drop index if exists road_station_publishable_i; -- unused functional index

create index road_station_publishable_type_id_natural_i on road_station(publishable, type, id, natural_id);
COMMENT ON INDEX road_station_publishable_type_id_natural_i is 'RoadStationSensorValueDtoRepository queries use this';


-- road_section

drop index if exists road_section_index1; -- natural_id, unused index
drop index if exists road_section_index2; -- road_district_id, fk-index with wrong name

create index road_section_road_district_fk_i on road_section(road_district_id); -- renamed fk-index
