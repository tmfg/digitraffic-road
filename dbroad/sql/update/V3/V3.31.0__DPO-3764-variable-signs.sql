-- Optimize device_data DISTINCT ON query performance
-- Drops redundant index and recreates composite index with INCLUDE clause

-- Drop redundant index that was causing poor query plan choices
DROP INDEX IF EXISTS device_data_effect_date_i;

-- Drop and recreate device_data_device_id_effect_date_i with INCLUDE clause
-- This allows covering index (no heap fetches for id column)
DROP INDEX IF EXISTS device_data_device_id_effect_date_i;

CREATE INDEX device_data_device_id_effect_date_i
  ON device_data (device_id, effect_date DESC) INCLUDE (id);

