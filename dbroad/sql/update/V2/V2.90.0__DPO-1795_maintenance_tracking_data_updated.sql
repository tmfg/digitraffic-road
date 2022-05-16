-- Clean table for unused data
DELETE FROM data_updated
WHERE data_type = 'MAINTENANCE_TRACKING_DATA';

ALTER TABLE data_updated
  rename version TO extension;

-- Change constraint to have also the extension
-- On pg 12 we could make virtual column by
-- ALTER TABLE data_updated
--   add column extension_virtual text GENERATED ALWAYS AS (coalesce(extension, 'null')) STORED;
-- And use it in unique index
-- Now we must use non null column
update data_updated set extension = '-' where extension is null;

ALTER TABLE data_updated
  alter column extension set default '-',
  alter column extension set not null;

DROP INDEX IF EXISTS data_updated_ui;
CREATE UNIQUE INDEX data_updated_ui ON data_updated (data_type, extension);

-- Search with created and domain
CREATE INDEX IF NOT EXISTS maintenance_tracking_domain_created_i
  ON maintenance_tracking USING btree (domain, created desc) where created is not null;

ALTER TABLE maintenance_tracking
  ALTER COLUMN created SET NOT NULL,
  ALTER COLUMN modified SET NOT NULL;