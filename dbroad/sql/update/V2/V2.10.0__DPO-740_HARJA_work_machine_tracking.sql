ALTER TABLE WORK_MACHINE_TRACKING
ADD COLUMN type TEXT,
ADD COLUMN created TIMESTAMP(0) WITH TIME ZONE,
ADD COLUMN handled TIMESTAMP(0) WITH TIME ZONE;

-- Point or LineString
update work_machine_tracking
set type = record -> 'observationFeatureCollection' -> 'features' -> 0 -> 'geometry' ->> 'type'
WHERE type IS NULL;

-- We have to trust timestamp in message for history data
update work_machine_tracking
set created = (record -> 'caption' ->> 'sendingTime')::timestamptz
WHERE created IS NULL;

ALTER TABLE WORK_MACHINE_TRACKING ALTER COLUMN created SET NOT NULL;

CREATE INDEX WORK_MACHINE_TRACKING_HANDLED_CREATED_I ON WORK_MACHINE_TRACKING
USING BTREE (handled nulls first, created);

CREATE INDEX WORK_MACHINE_TRACKING_TYPE_I ON WORK_MACHINE_TRACKING
USING BTREE (type nulls last);