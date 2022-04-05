CREATE INDEX IF NOT EXISTS maintenance_tracking_end_created_domain_i ON maintenance_tracking USING btree (end_time, created, domain);
CREATE INDEX maintenance_tracking_created_i ON maintenance_tracking (created, domain);
