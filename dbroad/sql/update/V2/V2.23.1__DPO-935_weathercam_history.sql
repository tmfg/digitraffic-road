-- C01502
ALTER TABLE CAMERA_PRESET_HISTORY
    ADD COLUMN CAMERA_ID CHARACTER VARYING(6);

UPDATE CAMERA_PRESET_HISTORY
SET CAMERA_ID = SUBSTRING(preset_id, 1, 6);

ALTER TABLE CAMERA_PRESET_HISTORY ALTER COLUMN CAMERA_ID SET NOT NULL;

CREATE INDEX CAMERA_PRESET_HISTORY_CAMERA_ID_LAST_MODIFIED_PUBLISHABLE_I
    ON CAMERA_PRESET_HISTORY
        USING BTREE (camera_id ASC, last_modified DESC) where publishable = TRUE;
COMMENT ON INDEX CAMERA_PRESET_HISTORY_CAMERA_ID_LAST_MODIFIED_PUBLISHABLE_I is 'Used when searching history for camera';

DROP INDEX CAMERA_PRESET_HISTORY_PRESET_ID_LAST_MODIFIED_I;
CREATE INDEX CAMERA_PRESET_HISTORY_PRESET_ID_LAST_MODIFIED_PUBLISHABLE_I
    ON CAMERA_PRESET_HISTORY
        USING BTREE (preset_id ASC, last_modified DESC) where publishable = TRUE;
COMMENT ON INDEX CAMERA_PRESET_HISTORY_PRESET_ID_LAST_MODIFIED_PUBLISHABLE_I is 'Used when searching history for preset';