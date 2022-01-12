-- Drop unused table
DROP TABLE IF EXISTS maintenance_task_category;
DROP SEQUENCE IF EXISTS seq_maintenance_tracking_data;

-- Table for municipality domain
CREATE TABLE IF NOT EXISTS maintenance_tracking_municipality_domain
(
  name                    TEXT NOT NULL, -- Name of the domain ie. autori-oulu
  copyright               TEXT NOT NULL, -- Copyright text for the domain
  created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified                TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT maintenance_tracking_municipality_domain_pkey PRIMARY KEY (name)
);

-- Automatic update of modified-field
DROP TRIGGER IF EXISTS maintenance_tracking_municipality_domain_modified_trigger on maintenance_tracking_municipality_domain;
CREATE TRIGGER maintenance_tracking_municipality_domain_modified_trigger BEFORE UPDATE ON maintenance_tracking_municipality_domain FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- Table for municipality domain contract
CREATE TABLE IF NOT EXISTS maintenance_tracking_municipality_domain_contract
(
  domain                  TEXT NOT NULL references maintenance_tracking_municipality_domain(name),
  contract                TEXT NOT NULL, -- external id of contract
  name                    TEXT NOT NULL, -- external name of the contract
  copyright               TEXT, -- Copyright for the contract, will be used to override municipality domain
  created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified                TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT maintenance_tracking_municipality_domain_contract_pkey PRIMARY KEY (domain, contract)
);

-- Automatic update of modified-field
DROP TRIGGER IF EXISTS maintenance_tracking_municipality_contract_modified_trigger on maintenance_tracking_municipality_domain_contract;
CREATE TRIGGER maintenance_tracking_municipality_contract_modified_trigger BEFORE UPDATE ON maintenance_tracking_municipality_domain_contract FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- Add additional info for municipality data
ALTER TABLE maintenance_tracking
  ADD COLUMN IF NOT EXISTS municipality_domain TEXT,
  ADD COLUMN IF NOT EXISTS municipality_contract TEXT,
  ADD FOREIGN KEY (municipality_domain, municipality_contract)
    references maintenance_tracking_municipality_domain_contract(domain, contract),
  ADD COLUMN IF NOT EXISTS municipality_message_original_id TEXT;

-- index for fetching data with domain
DROP INDEX IF EXISTS maintenance_tracking_municipality_contract_fki;
CREATE INDEX maintenance_tracking_municipality_contract_fki
  ON maintenance_tracking (municipality_domain, municipality_contract)
  WHERE municipality_domain IS NOT NULL;

--drop table maintenance_tracking_task_value_map;
--drop table maintenance_tracking_task_value;
-- Table that corresponds to MaintenanceTrackingTask.java -enum values
CREATE TABLE IF NOT EXISTS maintenance_tracking_task_value
(
  name                    TEXT NOT NULL, -- Enum value in Java code
  name_harja              TEXT NOT NULL, -- Harja value
  name_fi                 TEXT NOT NULL,
  name_sv                 TEXT NOT NULL,
  name_en                 TEXT NOT NULL,
  created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified                TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT maintenance_tracking_task_value_pkey PRIMARY KEY (name)
);

-- Automatic update of modified-field
DROP TRIGGER IF EXISTS maintenance_tracking_task_value_modified_trigger on maintenance_tracking_task_value;
CREATE TRIGGER maintenance_tracking_task_value_modified_trigger BEFORE UPDATE ON maintenance_tracking_task_value FOR EACH ROW EXECUTE PROCEDURE update_modified_column();


-- Table to map values from municipality data to harja tasks
CREATE TABLE IF NOT EXISTS maintenance_tracking_municipality_task_mapping
(
  name                    TEXT REFERENCES maintenance_tracking_task_value(name), -- harja task
  original_id             TEXT NOT NULL, -- municipality data id
  info                    TEXT, -- column to store additional info
  ignore                  BOOLEAN NOT NULL, -- if true data will not be stored to db
  domain     TEXT NOT NULL REFERENCES maintenance_tracking_municipality_domain(name), -- municipality domain of the data
  created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified                TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  -- Order of the constraint is same as automatic index columns.
  CONSTRAINT maintenance_tracking_task_value_map_pkey PRIMARY KEY (municipality_domain, original_id)
);

-- Automatic update of modified-field
DROP TRIGGER IF EXISTS maintenance_tracking_task_value_map_modified_trigger on maintenance_tracking_municipality_task_mapping;
CREATE TRIGGER maintenance_tracking_task_value_map_modified_trigger BEFORE UPDATE ON maintenance_tracking_task_value FOR EACH ROW EXECUTE PROCEDURE update_modified_column();


DROP INDEX IF EXISTS maintenance_tracking_task_value_map_i;
CREATE INDEX maintenance_tracking_task_value_map_i
  ON maintenance_tracking_municipality_task_mapping USING BTREE (municipality_domain asc, original_id asc);
