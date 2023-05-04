WITH src AS (
  SELECT rs.id AS road_station_id
       , CASE WHEN (rs.natural_id > 23000) THEN rs.natural_id - 23000 ELSE rs.natural_id END AS tms_natural_id
  FROM road_station rs
  INNER JOIN tms_station ts ON rs.id = ts.road_station_id
)
UPDATE tms_station target
SET natural_id = src.tms_natural_id
FROM src
WHERE target.road_station_id = src.road_station_id
  AND target.natural_id <> src.tms_natural_id;