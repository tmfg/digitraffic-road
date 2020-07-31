-- Execution time of finding last tracking of work machine dropped to 1/4 of the original
-- and this is asked quite a lot
CREATE INDEX maintenance_tracking_not_finished_work_machine_id_i
ON maintenance_tracking USING BTREE (work_machine_id) WHERE finished = false;
