-- some new sensors to public
INSERT INTO ALLOWED_ROAD_STATION_SENSOR
  select nextval('SEQ_ALLOWED_SENSOR') as id, 48 as natural_id, 'WEATHER_STATION' as road_station_type union
  select nextval('SEQ_ALLOWED_SENSOR'), 73, 'WEATHER_STATION' union
  select nextval('SEQ_ALLOWED_SENSOR'), 37, 'WEATHER_STATION' union
  select nextval('SEQ_ALLOWED_SENSOR'), 38, 'WEATHER_STATION' union
  select nextval('SEQ_ALLOWED_SENSOR'), 41, 'WEATHER_STATION' union
  select nextval('SEQ_ALLOWED_SENSOR'), 94, 'WEATHER_STATION' union
  select nextval('SEQ_ALLOWED_SENSOR'), 135, 'WEATHER_STATION' union
  select nextval('SEQ_ALLOWED_SENSOR'), 136, 'WEATHER_STATION' union
  select nextval('SEQ_ALLOWED_SENSOR'), 180, 'WEATHER_STATION'
ON CONFLICT DO NOTHING;