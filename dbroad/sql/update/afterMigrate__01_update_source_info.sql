INSERT INTO data_source_info (id, source, update_interval)
VALUES
  ('TMS_STATION', null, 'PT12H'),
  ('TMS_STATION_DATA', null, 'PT5M'),
  ('WEATHER_STATION', null, 'PT12H'),
  ('WEATHER_STATION_DATA', null, 'PT5M'),
  ('WEATHERCAM_STATION', null, 'PT12H'),
  ('WEATHERCAM_STATION_DATA', null, 'PT10M'),
  ('VARIABLE_SIGN', null, 'P1D'),
  ('VARIABLE_SIGN_DATA', null, 'PT0S'),
  ('TRAFFIC_MESSAGE', null, 'PT0S'),
  ('TRAFFIC_MESSAGE_AREA', null, 'P365D'),
  ('TRAFFIC_MESSAGE_LOCATION', null, 'P365D'),
  ('FORECAST_SECTION', null, 'P365D'),
  ('FORECAST_SECTION_FORECAST', null, 'PT15M'),
  ('MAINTENANCE_TRACKING', null, 'PT0S'),
  ('MAINTENANCE_TRACKING_MUNICIPALITY', null, 'PT5M'),
  ('COUNTING_SITE', null, 'PT1H'),
  ('COUNTING_SITE_DATA', null, 'PT1H')
ON CONFLICT (id) DO UPDATE
set source = EXCLUDED.source, update_interval = EXCLUDED.update_interval;