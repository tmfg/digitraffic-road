---- Situation type ----
ALTER TABLE DATEX2_SITUATION_RECORD
    ADD COLUMN IF NOT EXISTS life_cycle_management_canceled boolean,
    ADD COLUMN IF NOT EXISTS effective_end_time TIMESTAMP(0) WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS created TIMESTAMPTZ(0) DEFAULT now() NOT NULL,
    ADD COLUMN IF NOT EXISTS modified TIMESTAMPTZ(0) DEFAULT now() NOT NULL; -- add modified column to see when data is last modified

-- init data to not null values
UPDATE DATEX2_SITUATION_RECORD
SET life_cycle_management_canceled = false,
    effective_end_time = 'infinity'::timestamp without time zone;

ALTER TABLE DATEX2_SITUATION_RECORD ALTER COLUMN life_cycle_management_canceled SET NOT NULL;
ALTER TABLE DATEX2_SITUATION_RECORD ALTER COLUMN effective_end_time SET NOT NULL;

DROP INDEX IF EXISTS datex2_situation_situation_id_id_i;
CREATE INDEX datex2_situation_situation_id_id_i ON datex2_situation using btree (situation_id DESC, id DESC, datex2_id DESC);
COMMENT ON INDEX datex2_situation_situation_id_id_i IS 'Used to get the latest versions of situations';

DROP INDEX IF EXISTS datex2_situation_type_id_i;
CREATE INDEX datex2_situation_type_id_i ON datex2 using btree (situation_type, id);
COMMENT ON INDEX datex2_situation_type_id_i IS 'Used to get the latest versions of situations';

drop index if exists datex2_situation_record_validy_search_i;
create index datex2_situation_record_validy_search_i on datex2_situation_record using btree (datex2_situation_id, effective_end_time) where life_cycle_management_canceled IS NOT TRUE;
COMMENT ON INDEX datex2_situation_record_validy_search_i IS 'Used to get the latest versions of situations';

-- Function that updates effective_end_time
CREATE OR REPLACE FUNCTION update_datex2_situation_record__effective_end_time()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.effective_end_time =
        case
            when NEW.life_cycle_management_canceled = true then NEW.version_time
            when NEW.validy_status = 'ACTIVE' then 'infinity'::timestamp without time zone
            when NEW.validy_status = 'DEFINED_BY_VALIDITY_TIME_SPEC' then coalesce(NEW.overall_end_time, 'infinity'::timestamp without time zone)
            when NEW.validy_status = 'SUSPENDED' then coalesce(NEW.overall_end_time, NEW.version_time)
        end;
    RETURN NEW;
END;
$$ language 'plpgsql';


-- Create trigger that calls update function when row is inserted or updated
DROP TRIGGER IF EXISTS datex2_situation_record_update_effective_end_time_t ON datex2_situation_record;
CREATE TRIGGER datex2_situation_record_update_effective_end_time_t BEFORE INSERT OR UPDATE ON datex2_situation_record FOR EACH ROW EXECUTE PROCEDURE update_datex2_situation_record__effective_end_time();

CREATE TRIGGER datex2_situation_record_modified_t BEFORE UPDATE ON datex2_situation_record FOR EACH ROW EXECUTE PROCEDURE update_modified_column();