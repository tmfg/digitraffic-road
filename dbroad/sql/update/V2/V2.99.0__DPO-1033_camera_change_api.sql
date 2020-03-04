-- Delete over 24h history
DELETE FROM camera_preset_history
WHERE last_modified < now() - interval '25 hour';

-- add some new columns
ALTER TABLE camera_preset_history
    ADD COLUMN IF NOT EXISTS preset_seq BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_tmp TIMESTAMPTZ(0) DEFAULT now(),
    ADD COLUMN IF NOT EXISTS modified TIMESTAMPTZ(0) DEFAULT now(),
    ADD COLUMN IF NOT EXISTS last_modified_tmp TIMESTAMPTZ(0) DEFAULT now();

UPDATE camera_preset_history tgt
SET preset_seq = (
        SELECT preset_seq
        FROM (
                 SELECT row_number() OVER (PARTITION BY h.preset_id ORDER BY h.last_modified) preset_seq, h.preset_id, h.last_modified
                 FROM camera_preset_history h
        ) src
        WHERE src.preset_id = tgt.preset_id
          AND src.last_modified = tgt.last_modified),
    created_tmp = created,
    modified = last_modified,
    last_modified_tmp = last_modified;

-- replace old columns with new ones
ALTER TABLE camera_preset_history
    DROP COLUMN created,
    DROP COLUMN last_modified;
ALTER TABLE camera_preset_history
    RENAME COLUMN created_tmp TO created;
ALTER TABLE camera_preset_history
    RENAME COLUMN last_modified_tmp TO last_modified;
-- not null constraints to all
ALTER TABLE camera_preset_history
    ALTER COLUMN preset_seq SET NOT NULL,
    ALTER COLUMN created SET NOT NULL,
    ALTER COLUMN modified SET NOT NULL,
    ALTER COLUMN last_modified SET NOT NULL;

-- Function to update sequence number per preset
CREATE OR REPLACE FUNCTION update_preset_seq_column()
    RETURNS TRIGGER AS $$
DECLARE
    _preset_seq          BIGINT;
BEGIN
    SELECT MAX(preset_seq) + 1 INTO _preset_seq FROM camera_preset_history WHERE preset_id = NEW.preset_id;
    NEW.preset_seq := coalesce(_preset_seq, 1);
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Automatic modified update
DROP TRIGGER IF EXISTS camera_preset_history_modified_trigger on camera_preset_history;
CREATE TRIGGER camera_preset_history_modified_trigger BEFORE UPDATE ON camera_preset_history FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
-- Automatic preset sequence increment
DROP TRIGGER IF EXISTS camera_preset_history_preset_seq_trigger on camera_preset_history;
CREATE TRIGGER camera_preset_history_preset_seq_trigger BEFORE INSERT ON camera_preset_history FOR EACH ROW EXECUTE PROCEDURE update_preset_seq_column();

-- Mark old to negative, they will disappear in 24h
CREATE SEQUENCE SEQ_TMP;
UPDATE camera_preset_history tgt
SET preset_seq = -1 * NEXTVAL('SEQ_TMP')
WHERE preset_seq = 0;
DROP SEQUENCE SEQ_TMP;

CREATE UNIQUE INDEX camera_preset_history_preset_id_preset_seq_ui ON camera_preset_history USING BTREE (preset_id, preset_seq);
CREATE INDEX camera_preset_history_modified_i ON camera_preset_history USING BTREE (modified, preset_id);
