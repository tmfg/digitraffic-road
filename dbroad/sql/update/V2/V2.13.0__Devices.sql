CREATE TABLE DEVICE (
    ID              TEXT    PRIMARY KEY,
    UPDATED_DATE    TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    TYPE            TEXT    NOT NULL,
    ROAD_ADDRESS    TEXT    NOT NULL,
    ETRS_TM35FIN_X  NUMERIC(18,9),
    ETRS_TM35FIN_Y  NUMERIC(18,9)
);

CREATE TABLE DEVICE_DATA (
    ID                      SERIAL,
    CREATED_DATE            TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    DEVICE_ID               TEXT    REFERENCES DEVICE(ID) NOT NULL,
    DISPLAY_VALUE           TEXT,
    ADDITIONAL_INFORMATION  TEXT,
    EFFECT_DATE             TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    CAUSE                   TEXT
);

CREATE INDEX DEVICE_DATA_DEVICE_FK ON DEVICE_DATA(DEVICE_ID);
CREATE INDEX DEVICE_DATA_EFFECT_DATE_I ON DEVICE_DATA(EFFECT_DATE);