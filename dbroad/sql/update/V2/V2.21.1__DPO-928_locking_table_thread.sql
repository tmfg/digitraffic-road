DELETE FROM locking_table;
ALTER TABLE locking_table
    drop column instance_id;

ALTER TABLE locking_table
    ADD COLUMN instance_id BIGINT NOT NULL;

CREATE UNIQUE INDEX locking_table_ui
    ON locking_table
        USING BTREE (lock_name ASC, instance_id ASC);