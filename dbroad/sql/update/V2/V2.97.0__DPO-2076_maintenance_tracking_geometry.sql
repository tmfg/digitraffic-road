DROP index maintenance_tracking_line_string_i;

ALTER TABLE maintenance_tracking
  ALTER COLUMN line_string TYPE GEOMETRY(GEOMETRYZ, 4326); -- 4326 = WGS84

ALTER TABLE maintenance_tracking
  RENAME COLUMN line_string TO geometry;

create index if not exists maintenance_tracking_geometry_i on maintenance_tracking using gist(geometry);

UPDATE maintenance_tracking
 SET geometry = last_point
WHERE geometry is null;

ALTER TABLE maintenance_tracking
  ALTER COLUMN geometry SET NOT NULL;

ALTER TABLE maintenance_tracking ADD CONSTRAINT maintenance_tracking_geometry_type_check
  CHECK (ST_GeometryType(geometry) IN ('ST_LineString', 'ST_Point'));
