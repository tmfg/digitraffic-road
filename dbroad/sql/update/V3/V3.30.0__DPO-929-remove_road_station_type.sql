alter table road_station drop column type;
alter table road_station rename column road_station_type to type;

CREATE INDEX ROAD_STATION_TYPE_FK_I ON ROAD_STATION USING BTREE (type);
