-- Execution time dropped to 1/4 of the original
CREATE INDEX maintenance_tracking_not_finished_work_machine_id_i
ON maintenance_tracking USING BTREE (work_machine_id) WHERE finished = false;
