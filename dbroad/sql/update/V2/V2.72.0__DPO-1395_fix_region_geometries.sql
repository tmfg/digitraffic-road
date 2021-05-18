-- Fix invalid geometries
UPDATE region_geometry
SET geometry = ST_SetSRID(ST_MakeValid(ST_GeomFromGeoJSON(ST_AsGeoJSON(geometry))), 4326)
WHERE NOT ST_IsValid(geometry);