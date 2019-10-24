DELETE FROM locking_table;
ALTER TABLE locking_table
    drop column instance_id;
ALTER TABLE locking_table
    ADD COLUMN thread_id BIGINT NOT NULL;