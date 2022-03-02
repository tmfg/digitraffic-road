ALTER TABLE maintenance_tracking
ADD COLUMN IF NOT EXISTS previous_tracking_id BIGINT references maintenance_tracking(id);

DROP INDEX IF EXISTS maintenance_tracking_previous_tracking_id_fki;
CREATE INDEX maintenance_tracking_previous_tracking_id_fki ON maintenance_tracking (previous_tracking_id);

DROP INDEX IF EXISTS maintenance_tracking_domain_wm_id_end_time_i;
CREATE INDEX maintenance_tracking_domain_wm_id_end_time_i on maintenance_tracking USING btree (domain, work_machine_id, id, end_time) where (finished = false);