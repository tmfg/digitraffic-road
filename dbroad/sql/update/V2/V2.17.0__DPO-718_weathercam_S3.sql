CREATE TABLE CAMERA_PRESET_HISTORY (
    preset_id           CHARACTER VARYING(32) NOT NULL,
    version_id          CHARACTER VARYING(32) NOT NULL,
    camera_preset_id    NUMERIC(10,0) NOT NULL REFERENCES CAMERA_PRESET (id),
    last_modified       TIMESTAMP(6) WITH TIME ZONE  NOT NULL,
    publishable         BOOLEAN NOT NULL,
    size                INTEGER NOT NULL,
    created             TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

ALTER TABLE CAMERA_PRESET_HISTORY
    ADD CONSTRAINT PRESET_HISTORY_PK PRIMARY KEY (preset_id, version_id);

CREATE INDEX CAMERA_PRESET_HISTORY_FKI ON CAMERA_PRESET_HISTORY
    USING BTREE (camera_preset_id);

CREATE INDEX CAMERA_PRESET_HISTORY_LAST_MODIFIED_PUBLISHABLE_I
    ON CAMERA_PRESET_HISTORY
        USING BTREE (last_modified ASC, preset_id ASC, version_id) where publishable = TRUE;

ALTER TABLE CAMERA_PRESET
    DROP COLUMN public_internal;

ALTER TABLE CAMERA_PRESET
    RENAME COLUMN public_external TO is_public;

CREATE OR REPLACE FUNCTION f_trigger_vc$camera_preset()
    RETURNS trigger
AS
$BODY$
BEGIN
    NEW.publishable :=
            CASE
                WHEN (NEW.obsolete_date IS NULL AND NEW.lotju_id IS NOT NULL AND NEW.camera_lotju_id IS NOT NULL AND NEW.is_public = true) THEN true
                ELSE false
                END;
    RETURN NEW;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE INDEX camera_preset_publishable_i
ON camera_preset
USING BTREE (publishable, camera_id) where publishable = TRUE;