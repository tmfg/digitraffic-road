ALTER TABLE data_source_info
  ADD COLUMN IF NOT EXISTS recommended_fetch_interval TEXT DEFAULT 'PT5M' NOT NULL,
  ALTER COLUMN update_interval SET NOT NULL;

ALTER TABLE data_source_info
  DROP CONSTRAINT IF EXISTS recommended_fetch_interval_regexp_check,
  ADD CONSTRAINT recommended_fetch_interval_regexp_check
    CHECK (recommended_fetch_interval ~ '^([-+]?)P(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?$');

insert into data_updated(id, data_type, subtype, updated)
VALUES
  (nextval('seq_data_updated'), 'COUNTING_SITE_USER_TYPE_DATA', '-', '2021-10-12 07:04:59'),
  (nextval('seq_data_updated'), 'COUNTING_SITE_DIRECTION_DATA', '-', '2021-10-12 07:04:59')
ON CONFLICT (data_type, subtype) DO UPDATE
  set updated = EXCLUDED.updated;