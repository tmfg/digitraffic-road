CREATE TABLE CAMERA_PRESET_HISTORY (
    camera_preset_id    NUMERIC(10,0) NOT NULL REFERENCES CAMERA_PRESET (id),
    preset_id           CHARACTER VARYING(32) NOT NULL,
    version_id          CHARACTER VARYING(32) NOT NULL,
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

-- TODO remove camera preset public internal