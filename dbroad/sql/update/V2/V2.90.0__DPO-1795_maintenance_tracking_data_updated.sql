-- Clean table for unused data
DELETE FROM data_updated
WHERE data_type = 'MAINTENANCE_TRACKING_DATA';

-- Search with created and domain
CREATE INDEX IF NOT EXISTS maintenance_tracking_created_domain_i ON maintenance_tracking USING btree (created, domain);

-- Change constraint to have also the version
DROP INDEX IF EXISTS data_updated_ui;
CREATE UNIQUE INDEX data_updated_ui ON data_updated (data_type, version);