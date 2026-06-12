-- DPO-2771: Replace the '-' sentinel value for subtype with proper NULL values
-- and use a NULLS NOT DISTINCT unique index (PG15+) so that (data_type, NULL) is
-- treated as a single distinct combination, matching the previous behaviour of (data_type, '-').

-- 1. Remove the NOT NULL constraint and default that were added in V2.90.0 (must come first)
ALTER TABLE data_updated
    ALTER COLUMN subtype DROP NOT NULL,
    ALTER COLUMN subtype DROP DEFAULT;

-- 2. Migrate existing sentinel rows to NULL
UPDATE data_updated
SET subtype = NULL
WHERE subtype = '-';

-- 3. Recreate the unique index with NULLS NOT DISTINCT so that
--    ON CONFLICT (data_type, subtype) works correctly for NULL subtypes
DO $$
BEGIN
  IF current_setting('server_version_num')::int < 150000 THEN
    RAISE EXCEPTION 'V3.35.0 requires PostgreSQL 15+ to use NULLS NOT DISTINCT (server_version_num=%)', current_setting('server_version_num');
  END IF;
END $$;
DROP INDEX IF EXISTS data_updated_ui;
CREATE UNIQUE INDEX data_updated_ui
    ON data_updated (data_type, subtype) NULLS NOT DISTINCT;


