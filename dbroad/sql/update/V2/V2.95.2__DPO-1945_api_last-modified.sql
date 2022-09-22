-- add modified and created columns to road station
ALTER TABLE datex2
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();

update datex2
  set created = import_date,
      modified = import_date
where created = NOW();

-- trigger to update modified column
DROP TRIGGER IF EXISTS datex2_MODIFIED_T on datex2;
CREATE TRIGGER datex2_MODIFIED_T BEFORE UPDATE ON datex2 FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

CREATE INDEX IF NOT EXISTS datex2_type_modified_i on datex2(situation_type, modified);
CREATE INDEX IF NOT EXISTS datex2_modified_type_i on datex2(modified, situation_type);