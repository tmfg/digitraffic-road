-- After this script has run there should be two weather and TMS stations each with publishable = false

UPDATE road_station_sensor
SET is_public = false
WHERE id = 356 AND road_station_type = 'WEATHER_STATION';

UPDATE road_station_sensor
SET obsolete_date = NOW()
WHERE id = 357 AND road_station_type = 'WEATHER_STATION';

UPDATE road_station_sensor
SET is_public = false
WHERE id = 1 AND road_station_type = 'TMS_STATION';

UPDATE road_station_sensor
SET obsolete_date = NOW()
WHERE id = 2 AND road_station_type = 'TMS_STATION';