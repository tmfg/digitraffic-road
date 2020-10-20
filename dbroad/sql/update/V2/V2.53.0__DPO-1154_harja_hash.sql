-- ok, first add missing primary key to DEVICE_DATA
ALTER TABLE maintenance_tracking_data ADD COLUMN IF NOT EXISTS hash TEXT;
DROP INDEX  IF EXISTS maintenance_tracking_data_hash_ui;
CREATE UNIQUE INDEX maintenance_tracking_data_hash_ui on maintenance_tracking_data(hash);