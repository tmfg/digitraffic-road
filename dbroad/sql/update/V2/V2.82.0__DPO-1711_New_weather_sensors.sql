INSERT INTO ALLOWED_ROAD_STATION_SENSOR
select nextval('SEQ_ALLOWED_SENSOR') as id, 160 as natural_id, 'WEATHER_STATION' as road_station_type union
select nextval('SEQ_ALLOWED_SENSOR'),       161,               'WEATHER_STATION' union
select nextval('SEQ_ALLOWED_SENSOR'),       162,               'WEATHER_STATION' union
select nextval('SEQ_ALLOWED_SENSOR'),       163,               'WEATHER_STATION'
ON CONFLICT DO NOTHING;