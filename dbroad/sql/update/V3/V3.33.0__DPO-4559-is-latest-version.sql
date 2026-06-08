-- Add is_latest_version flag to data_datex2_situation.
-- New rows default to true; a trigger (update_data_datex2_situation_is_latest_version_t,
-- defined in afterMigrate__04_update_triggers.sql) marks the previous version as false on each insert.
-- The trigger function is defined in beforeMigrate__01_functions.sql.

ALTER TABLE data_datex2_situation
    ADD COLUMN IF NOT EXISTS is_latest_version boolean NOT NULL DEFAULT true;

-- Backfill: mark all non-latest versions as false
UPDATE data_datex2_situation
SET is_latest_version = false
WHERE datex2_id NOT IN (
    SELECT DISTINCT ON (situation_id) datex2_id
    FROM data_datex2_situation
    ORDER BY situation_id, situation_version DESC
);

-- Unique partial index — enforces at DB level that only one row per situation_id can be latest.
-- Also serves as a fast point-lookup index for WHERE situation_id = ? AND is_latest_version = true.
CREATE UNIQUE INDEX data_datex2_situation_latest_ui
    ON data_datex2_situation(situation_id)
    WHERE is_latest_version = true;

-- Partial index for type/time range queries — only covers the latest rows
CREATE INDEX data_datex2_situation_latest_by_type_i
    ON data_datex2_situation(situation_type, end_time, start_time)
    WHERE is_latest_version = true;


