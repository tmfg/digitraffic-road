-- duplicate index
DROP INDEX IF EXISTS maintenance_tracking_data_tracking_data_id_fkey_i;

-- delete uses this
DROP INDEX IF EXISTS sensor_value_history_measured_i;
CREATE INDEX sensor_value_history_measured_i ON sensor_value_history USING BTREE (measured);
COMMENT ON INDEX sensor_value_history_measured_i is 'Cleanup uses this to delete older than 24h';
