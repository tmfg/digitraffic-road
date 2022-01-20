-- Drop unused table
DROP TABLE IF EXISTS maintenance_task_category;
DROP SEQUENCE IF EXISTS seq_maintenance_tracking_data;

-- Table for domain
CREATE TABLE IF NOT EXISTS maintenance_tracking_domain
(
  name                    TEXT NOT NULL, -- Name of the domain ie. autori-oulu
  copyright               TEXT, -- Copyright text for the domain
  created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified                TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT maintenance_tracking_domain_pkey PRIMARY KEY (name)
);
INSERT into maintenance_tracking_domain(name) values ('harja') on conflict (name) do nothing;

-- Automatic update of modified-field
DROP TRIGGER IF EXISTS maintenance_tracking_domain_modified_trigger on maintenance_tracking_domain;
CREATE TRIGGER maintenance_tracking_domain_modified_trigger BEFORE UPDATE ON maintenance_tracking_domain FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- Table for domain contract
CREATE TABLE IF NOT EXISTS maintenance_tracking_domain_contract
(
  domain                  TEXT NOT NULL references maintenance_tracking_domain(name),
  contract                TEXT NOT NULL, -- external id of contract
  name                    TEXT NOT NULL, -- external name of the contract
  copyright               TEXT, -- Copyright for the contract, will be used to override domain
  start_date              TIMESTAMP(0) WITH TIME ZONE,
  end_date                TIMESTAMP(0) WITH TIME ZONE,
  data_last_updated       TIMESTAMP(3) WITH TIME ZONE, -- Latest time when data for contract has been updated
  created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified                TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  CONSTRAINT maintenance_tracking_domain_contract_pkey PRIMARY KEY (domain, contract)
);

-- Automatic update of modified-field
DROP TRIGGER IF EXISTS maintenance_tracking_contract_modified_trigger on maintenance_tracking_domain_contract;
CREATE TRIGGER maintenance_tracking_contract_modified_trigger BEFORE UPDATE ON maintenance_tracking_domain_contract FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- Add additional info for domain data
ALTER TABLE maintenance_tracking
  ADD COLUMN IF NOT EXISTS domain TEXT default 'harja' -- to not break current HARJA implementation
    references maintenance_tracking_domain(name),
  ADD COLUMN IF NOT EXISTS contract TEXT,
  ADD FOREIGN KEY (domain, contract)
    references maintenance_tracking_domain_contract (domain, contract),
  ADD COLUMN IF NOT EXISTS message_original_id TEXT;

-- indexes for fetching data
DROP INDEX IF EXISTS maintenance_tracking_end_time_id_i; -- replace this with new index
DROP INDEX IF EXISTS maintenance_tracking_domain_end_time_i;
CREATE INDEX maintenance_tracking_domain_end_time_i on maintenance_tracking (domain, end_time, id);
DROP INDEX IF EXISTS maintenance_tracking_contract_fki;
CREATE INDEX maintenance_tracking_contract_fki ON maintenance_tracking (domain, contract);

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


-- Table to map values from domain data to harja tasks
CREATE TABLE IF NOT EXISTS maintenance_tracking_domain_task_mapping
(
  name                    TEXT REFERENCES maintenance_tracking_task_value(name),      -- harja task
  original_id             TEXT NOT NULL,                                              -- domain data id
  info                    TEXT,                                                       -- column to store additional info
  ignore                  BOOLEAN NOT NULL,                                           -- if true data will not be stored to db
  domain                  TEXT NOT NULL REFERENCES maintenance_tracking_domain(name), -- domain of the data
  created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  modified                TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  -- Order of the constraint is same as automatic index columns.
  CONSTRAINT maintenance_tracking_task_value_map_pkey PRIMARY KEY (domain, original_id)
);

-- Automatic update of modified-field
DROP TRIGGER IF EXISTS maintenance_tracking_domain_task_mapping_modified_trigger on maintenance_tracking_domain_task_mapping;
CREATE TRIGGER maintenance_tracking_domain_task_mapping_modified_trigger BEFORE UPDATE ON maintenance_tracking_domain_task_mapping FOR EACH ROW EXECUTE PROCEDURE update_modified_column();


DROP INDEX IF EXISTS maintenance_tracking_task_value_map_i;
CREATE INDEX maintenance_tracking_task_value_map_i
  ON maintenance_tracking_domain_task_mapping USING BTREE (domain asc, original_id asc);
