DROP TABLE locking_table;

CREATE TABLE locking_table
(
  lock_name    TEXT NOT NULL PRIMARY KEY,
  lock_locked  TIMESTAMP(3) WITH TIME ZONE,
  lock_expires TIMESTAMP(3) WITH TIME ZONE,
  instance_id  TEXT NOT NULL
);
