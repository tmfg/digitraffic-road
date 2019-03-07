ALTER TABLE WORK_MACHINE_TRACKING
ADD COLUMN type TEXT,
ADD COLUMN handled TIMESTAMP(0) WITH TIME ZONE;

-- Point or LineString
update work_machine_tracking
set type = record -> 'observationFeatureCollection' -> 'features' -> 0 -> 'geometry' ->> 'type'
WHERE type IS NULL;