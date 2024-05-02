-- as distinct from existing column 'pic_last_modified' which is the source system timestamp

-- column 'modified' in camera_preset must not be updated when 'pic_last_modified' is updated,
-- so this value is used instead for tracking preset update times
ALTER TABLE camera_preset ADD COLUMN IF NOT EXISTS pic_last_modified_db TIMESTAMP(0) WITH TIME ZONE;


