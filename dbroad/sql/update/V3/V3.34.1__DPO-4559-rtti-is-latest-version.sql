-- Add is_latest_version flag to datex2_rtti.
-- Same pattern as data_datex2_situation (V3.33.0).
-- New rows default to true; a trigger (update_datex2_rtti_is_latest_version_t,
-- defined in afterMigrate__04_update_triggers.sql) marks the previous row as false on each insert.
-- The trigger function is defined in beforeMigrate__01_functions.sql.

ALTER TABLE datex2_rtti
    ADD COLUMN IF NOT EXISTS is_latest_version boolean NOT NULL DEFAULT true;

-- Backfill: mark all non-latest rows as false, grouped by situation_id,
-- ordered by publication_time DESC.
UPDATE datex2_rtti
SET is_latest_version = false
WHERE datex2_id NOT IN (
    SELECT DISTINCT ON (situation_id) datex2_id
    FROM datex2_rtti
    ORDER BY situation_id, publication_time DESC
);

-- Unique partial index — enforces at DB level that only one row per situation_id can be latest.
-- Also serves as a fast point-lookup index for WHERE situation_id = ? AND is_latest_version = true.
CREATE UNIQUE INDEX datex2_rtti_latest_ui
    ON datex2_rtti(situation_id)
    WHERE is_latest_version = true;

-- Partial index for findAllTrafficData — only indexes the latest rows per situation
CREATE INDEX datex2_rtti_latest_i
    ON datex2_rtti(start_time, end_time, is_srti)
    WHERE is_latest_version = true;
