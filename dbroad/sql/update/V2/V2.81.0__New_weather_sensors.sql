INSERT INTO ALLOWED_ROAD_STATION_SENSOR
select nextval('SEQ_ALLOWED_SENSOR') as id, 126 as natural_id, 'WEATHER_STATION' as road_station_type union
select nextval('SEQ_ALLOWED_SENSOR'),       127,               'WEATHER_STATION' union
select nextval('SEQ_ALLOWED_SENSOR'),       128,               'WEATHER_STATION'
ON CONFLICT DO NOTHING;