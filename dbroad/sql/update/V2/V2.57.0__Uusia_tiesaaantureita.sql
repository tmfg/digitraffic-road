-- some new sensors to public
INSERT INTO ALLOWED_ROAD_STATION_SENSOR
    select nextval('SEQ_ALLOWED_SENSOR') as id, 147 as natural_id, 'WEATHER_STATION' as road_station_type union
    select nextval('SEQ_ALLOWED_SENSOR'), 148, 'WEATHER_STATION' union
    select nextval('SEQ_ALLOWED_SENSOR'), 149, 'WEATHER_STATION' union
    select nextval('SEQ_ALLOWED_SENSOR'), 150, 'WEATHER_STATION'
ON CONFLICT DO NOTHING;