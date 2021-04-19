---- Situation type ----
ALTER TABLE DATEX2_SITUATION_RECORD ADD COLUMN IF NOT EXISTS life_cycle_management_canceled boolean;

-- set history to false for all old data
UPDATE DATEX2_SITUATION_RECORD
SET life_cycle_management_canceled = false;

ALTER TABLE DATEX2_SITUATION_RECORD ALTER COLUMN life_cycle_management_canceled SET NOT NULL;

DROP INDEX IF EXISTS datex2_situation_type_i;
CREATE INDEX datex2_situation_type_i ON datex2 USING btree (situation_type);
COMMENT ON INDEX datex2_situation_type_i IS 'Used to get the latest versions of situations';

DROP INDEX IF EXISTS datex2_situation_situation_id_id_i;
CREATE INDEX datex2_situation_situation_id_id_i ON datex2_situation using btree (situation_id DESC, id DESC, datex2_id DESC);
COMMENT ON INDEX datex2_situation_situation_id_id_i IS 'Used to get the latest versions of situations';

DROP INDEX IF EXISTS datex2_situation_type_id_i;
CREATE INDEX datex2_situation_type_id_i ON datex2 using btree (situation_type, id);
COMMENT ON INDEX datex2_situation_type_id_i IS 'Used to get the latest versions of situations';

drop index if exists datex2_situation_record_validy_status_i;
create index datex2_situation_record_validy_status_i on datex2_situation_record using btree (validy_status) where life_cycle_management_canceled is not true;
COMMENT ON INDEX datex2_situation_record_validy_status_i IS 'Used to get the latest versions of situations';