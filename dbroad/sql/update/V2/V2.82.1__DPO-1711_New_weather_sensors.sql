INSERT INTO ALLOWED_ROAD_STATION_SENSOR
select nextval('SEQ_ALLOWED_SENSOR') as id, 167 as natural_id, 'WEATHER_STATION' as road_station_type union
select nextval('SEQ_ALLOWED_SENSOR'),       168,               'WEATHER_STATION' union
select nextval('SEQ_ALLOWED_SENSOR'),       169,               'WEATHER_STATION' union
select nextval('SEQ_ALLOWED_SENSOR'),       170,               'WEATHER_STATION'
ON CONFLICT DO NOTHING;