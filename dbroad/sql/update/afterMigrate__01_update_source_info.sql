INSERT INTO data_source_info (id, source, update_interval, recommended_fetch_interval)
VALUES
  ('TMS_STATION', null, 'PT0S', 'PT1H'),
  ('TMS_STATION_DATA', null, 'PT5M', 'PT1M'),
  ('WEATHER_STATION', null, 'PT0S', 'PT1H'),
  ('WEATHER_STATION_DATA', null, 'PT5M', 'PT1M'),
  ('WEATHERCAM_STATION', null, 'PT0S', 'PT1H'),
  ('WEATHERCAM_STATION_DATA', null, 'PT10M', 'PT1M'),
  ('VARIABLE_SIGN', null, 'P1D', 'P1D'),
  ('VARIABLE_SIGN_DATA', null, 'PT0S', 'PT1M'),
  ('TRAFFIC_MESSAGE', null, 'PT0S', 'PT1M'),
  ('TRAFFIC_MESSAGE_AREA', null, 'P365D', 'P1D'),
  ('TRAFFIC_MESSAGE_LOCATION', null, 'P365D', 'P1D'),
  ('FORECAST_SECTION', null, 'P365D', 'P1D'),
  ('FORECAST_SECTION_FORECAST', null, 'PT15M', 'PT5M'),
  ('MAINTENANCE_TRACKING', null, 'PT0S', 'PT1M'),
  ('MAINTENANCE_TRACKING_MUNICIPALITY', null, 'PT5M', 'PT1M'),
  ('COUNTING_SITE', null, 'PT1H', 'PT5M'),
  ('COUNTING_SITE_DATA', null, 'PT1H', 'PT5M')
ON CONFLICT (id) DO UPDATE
set source = EXCLUDED.source
  , update_interval = EXCLUDED.update_interval
  , recommended_fetch_interval = EXCLUDED.recommended_fetch_interval;