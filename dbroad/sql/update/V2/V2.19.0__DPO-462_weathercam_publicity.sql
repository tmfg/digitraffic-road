
ALTER TABLE ROAD_STATION
    ADD COLUMN PUBLICITY_START_TIME TIMESTAMP(0) WITH TIME ZONE,
    ADD COLUMN IS_PUBLIC_PREVIOUS boolean;

UPDATE road_station
set is_public_previous = is_public;

ALTER TABLE road_station ALTER COLUMN is_public_previous SET NOT NULL;

CREATE INDEX CAMERA_PRESET_HISTORY_LAST_MODIFIED_I ON camera_preset_history (last_modified);
COMMENT ON INDEX CAMERA_PRESET_HISTORY_LAST_MODIFIED_I is 'Updating history uses this when limiting start time';

ALTER TABLE camera_preset
ADD CONSTRAINT camera_preset_lotju_id_ui UNIQUE (lotju_id);