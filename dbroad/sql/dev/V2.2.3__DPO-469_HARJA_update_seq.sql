-- protect against concurrent inserts while you update the counter
LOCK TABLE work_machine_tracking IN EXCLUSIVE MODE;
-- Update the sequence
SELECT setval('SEQ_WORK_MACHINE_TRACKING', COALESCE((SELECT MAX(id)+1 FROM work_machine_tracking), 1), false);
