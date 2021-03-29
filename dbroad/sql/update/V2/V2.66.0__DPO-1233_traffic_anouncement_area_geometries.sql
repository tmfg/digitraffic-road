CREATE TABLE REGION_GEOMETRY (
    id                      SERIAL PRIMARY KEY, -- just for ordering from latest to oldest
    name                    TEXT NOT NULL,
    location_code           INTEGER NOT NULL, -- ie. 00003_Suomi.json -> 3
    type                    TEXT NOT NULL,
    effective_date          TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    geometry                GEOMETRY(GEOMETRY, 4326),     -- 4326 = WGS84
    version_date            TIMESTAMP(0) WITH TIME ZONE NOT NULL, -- creation date from the version control
    git_id                  TEXT NOT NULL,
    git_path                TEXT NOT NULL,
    git_commit_id           TEXT NOT NULL
);

CREATE SEQUENCE SEQ_REGION_GEOMETRY;

CREATE INDEX REGION_GEOMETRY_CODE_I on REGION_GEOMETRY(location_code, id);

CREATE UNIQUE INDEX REGION_GEOMETRY_CODE_COMMIT_KEY on REGION_GEOMETRY(location_code, git_commit_id);