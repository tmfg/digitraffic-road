-- Clean table for unused data
DELETE FROM data_updated
WHERE data_type = 'MAINTENANCE_TRACKING_DATA';

-- Change constraint to have also the version
-- On pg 12 we could make virtual column by
-- ALTER TABLE data_updated
--   add column version_virtual text GENERATED ALWAYS AS (coalesce(version, 'null')) STORED;
-- And use it in unique index
-- Now we must use non null column

-- handle error if column has been already renamed
DO
$$
  BEGIN
    ALTER TABLE data_updated
      RENAME COLUMN version TO subtype;
  EXCEPTION
    WHEN undefined_column THEN
  END;
$$;

update data_updated set subtype = '-' where subtype is null;

ALTER TABLE data_updated
  alter column subtype set default '-',
  alter column subtype set not null;

DROP INDEX IF EXISTS data_updated_ui;
CREATE UNIQUE INDEX data_updated_ui ON data_updated (data_type, subtype);

-- Search with created and domain
CREATE INDEX IF NOT EXISTS maintenance_tracking_domain_created_i
  ON maintenance_tracking USING btree (domain, created desc) where created is not null;

ALTER TABLE maintenance_tracking
  ALTER COLUMN created SET NOT NULL,
  ALTER COLUMN modified SET NOT NULL;