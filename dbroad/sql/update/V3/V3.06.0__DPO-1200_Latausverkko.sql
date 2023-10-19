-- Collation to order numbers naturally ie. versions
CREATE COLLATION IF NOT EXISTS en_natural
(
  LOCALE = 'en-US-u-kn-true',
  PROVIDER = 'icu'
);
-- drop table if exists ocpi_cpo_version_endpoint;
-- drop table if exists ocpi_cpo_version;
-- drop ocpi_cpo_business_details
-- drop table if exists ocpi_cpo;
-- drop table if exists ocpi_version;

CREATE TABLE IF NOT EXISTS ocpi_version
(
  version  TEXT collate en_natural PRIMARY KEY, -- ie. ie. 2.1.1
  created  TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

BEGIN;
ALTER TABLE ocpi_version
  DROP CONSTRAINT IF EXISTS ocpi_version_check;
ALTER TABLE ocpi_version
  ADD CONSTRAINT ocpi_version_check
    CHECK (version ~* ('^(\d+\.)?(\d+\.)?(\d+)$')); -- i.e. 2, 2.1, 2.1.1
COMMIT;
CREATE TABLE IF NOT EXISTS ocpi_cpo
(
  dt_cpo_id         TEXT PRIMARY KEY, -- Internal identifier ie. 'CHARGE_MAN'
  dt_cpo_name       TEXT,             -- Internal name ie. 'Charge Man Global'
  party_id          VARCHAR(3),       -- CPO or eMSP ID of this party. (following the 15118 ISO standard).
  country_code      VARCHAR(2),       -- Country code of the country this party is operating in. ie. FI
  versions_endpoint TEXT,             -- ie. https://example.com/ocpi/cpo/versions, endpoint that returns endpoints of the versions
  token_a           TEXT,             -- initial auth key from cpo
  token_b           TEXT,             -- our generated key
  token_c           TEXT,             -- auth key got from the hand shake with cpo, will disable usage of token_a
  created           TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified          TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS ocpi_cpo_business_details
(
  dt_cpo_id         TEXT PRIMARY KEY REFERENCES ocpi_cpo (dt_cpo_id), -- ie. 'CHARGE_MAN'
  name              TEXT,             -- ie. 'Charge Man Global'
  logo_url          TEXT,
  logo_thumbnail    TEXT,
  logo_category     TEXT check(logo_category in ('HARGER', 'ENTRANCE', 'LOCATION', 'NETWORK', 'OPERATOR', 'OTHER', 'OWNER')),
  logo_type         TEXT,
  logo_width        INTEGER,
  logo_height       INTEGER,
  website           TEXT,
  created           TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified          TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS ocpi_cpo_version
(
  dt_cpo_id          TEXT REFERENCES ocpi_cpo (dt_cpo_id) NOT NULL,
  ocpi_version       TEXT collate en_natural REFERENCES ocpi_version (version), -- ie. 2.1.1
  endpoints_endpoint TEXT                           NOT NULL,                   --  i.e. https://example.com/ocpi/cpo/2.1.1/, endpoint that returns endpoints of the version
  created            TIMESTAMP(0) WITH TIME ZONE    NOT NULL DEFAULT NOW(),
  modified           TIMESTAMP(0) WITH TIME ZONE    NOT NULL DEFAULT NOW(),
  PRIMARY KEY (dt_cpo_id, ocpi_version)
);

CREATE TABLE IF NOT EXISTS ocpi_cpo_module_endpoint
(
  module       TEXT                        NOT NULL, -- ie. credentials, locations
  dt_cpo_id    TEXT                        NOT NULL,
  ocpi_version TEXT collate en_natural     NOT NULL, -- ie. 2.1.1
  endpoint     TEXT                        NOT NULL, -- ie. https://example.com/ocpi/emsp/2.0/credentials
  created      TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified     TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  PRIMARY KEY (module, dt_cpo_id, ocpi_version),
  FOREIGN KEY (dt_cpo_id, ocpi_version) REFERENCES ocpi_cpo_version (dt_cpo_id, ocpi_version)
);

CREATE TABLE IF NOT EXISTS ocpi_location
(
  id              TEXT                        NOT NULL, -- id from cpo
  dt_cpo_id       TEXT                        NOT NULL,
  ocpi_version    TEXT collate en_natural     NOT NULL, -- ie. 2.1.1
  location_object JSONB                       NOT NULL, -- ie. https://example.com/ocpi/emsp/2.0/credentials
  geometry        GEOMETRY(POINT, 4326) NOT NULL, -- 4326 = WGS84
  modified_cpo    TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(), -- Timestamp when cpo has modified/created data
  removed         BOOLEAN GENERATED ALWAYS AS ( -- If there is zero active evses then the location is discontinued
    coalesce(NOT jsonb_path_exists(location_object->'evses', '$[*] ? (@.status <> "REMOVED")'), true)
  ) STORED NOT NULL,
  created         TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified        TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  PRIMARY KEY (id, dt_cpo_id), -- version could be here also, but we keep only one version (newest) in db
  FOREIGN KEY (dt_cpo_id, ocpi_version) REFERENCES ocpi_cpo_version (dt_cpo_id, ocpi_version)
);

insert into ocpi_version (version)
VALUES ('2.1.1')
ON CONFLICT ON CONSTRAINT ocpi_version_pkey DO NOTHING;

CREATE INDEX IF NOT EXISTS ocpi_location_cpo_modified_cpo_i on ocpi_location using BTREE (dt_cpo_id, modified_cpo DESC);

CREATE INDEX IF NOT EXISTS OCPI_CPO_VERSION_OCPI_VERSION_FKEY_I ON OCPI_CPO_VERSION USING BTREE (ocpi_version);
