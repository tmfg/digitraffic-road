-- C01502
ALTER TABLE CAMERA_PRESET_HISTORY
    ADD COLUMN preset_public BOOLEAN;

UPDATE CAMERA_PRESET_HISTORY
SET preset_public = true;

ALTER TABLE CAMERA_PRESET_HISTORY ALTER COLUMN preset_public SET NOT NULL;