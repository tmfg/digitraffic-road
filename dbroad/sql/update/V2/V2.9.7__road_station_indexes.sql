create unique index road_station_publishable_tms_u on road_station(id, natural_id) where publishable = true and type = 1;
create unique index road_station_publishable_weather_u on road_station(id, natural_id) where publishable = true and type = 2;

drop index road_station_publishable_type_id_natural_i;

COMMENT ON INDEX road_station_publishable_tms_u is 'RoadStationSensorValueDtoRepository queries use this';
COMMENT ON INDEX road_station_publishable_weather_u is 'RoadStationSensorValueDtoRepository queries use this';