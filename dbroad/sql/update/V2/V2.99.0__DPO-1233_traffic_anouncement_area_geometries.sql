CREATE TABLE AREA_LOCATION_REGION (
    id                      SERIAL PRIMARY KEY, -- just for ordering from latest to oldest
    location_code           INTEGER NOT NULL, -- ie. 00003_Suomi.json -> 3
    type                    TEXT NOT NULL,
    effective_date          TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    geometry                TEXT NOT NULL,
    version_date            TIMESTAMP(0) WITH TIME ZONE NOT NULL -- creation date from the version control
);

CREATE SEQUENCE SEQ_AREA_LOCATION_REGION;

CREATE INDEX AREA_LOCATION_REGION_code_i on AREA_LOCATION_REGION(location_code, id);
