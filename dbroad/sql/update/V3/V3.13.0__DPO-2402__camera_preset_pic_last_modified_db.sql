-- as distinct from existing column 'pic_last_modified' which is the source system timestamp

-- column 'modified' in camera_preset must not be updated when 'pic_last_modified' is updated,
-- so this value is used instead for tracking preset update times
ALTER TABLE camera_preset ADD COLUMN pic_last_modified_db TIMESTAMP(0) WITH TIME ZONE;

CREATE OR REPLACE FUNCTION update_pic_last_modified_db_column()
  RETURNS TRIGGER AS $$
BEGIN
  IF (to_jsonb(OLD.pic_last_modified) <> to_jsonb(NEW.pic_last_modified)) THEN
    NEW.pic_last_modified_db = now();
END IF;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER camera_preset_pic_last_modified_db_t BEFORE UPDATE ON camera_preset FOR EACH ROW EXECUTE PROCEDURE update_pic_last_modified_db_column();
