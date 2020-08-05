DROP INDEX MAINTENANCE_TRACKING_DATA_HANDLING_I;
CREATE INDEX maintenance_tracking_data_status_unhandled_id_i
ON maintenance_tracking_data USING BTREE (id) WHERE status = 'UNHANDLED';
