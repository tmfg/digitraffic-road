ALTER TABLE maintenance_tracking_task
    DROP CONSTRAINT maintenance_tracking_task_maintenance_tracking_id_fkey,
    ADD CONSTRAINT maintenance_tracking_task_maintenance_tracking_id_fkey
        FOREIGN KEY (MAINTENANCE_TRACKING_ID)
        REFERENCES MAINTENANCE_TRACKING(ID) ON DELETE CASCADE;
