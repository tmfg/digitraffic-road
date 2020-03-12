ALTER TABLE camera_preset_history
    ADD COLUMN preset_seq_prev BIGINT;

UPDATE camera_preset_history
SET preset_seq_prev = preset_seq-1;

ALTER TABLE camera_preset_history
    ALTER COLUMN preset_seq_prev SET NOT NULL;

-- Function to generate new sequence number on every insert per preset history
CREATE OR REPLACE FUNCTION update_preset_seq_column()
    RETURNS TRIGGER AS $$
DECLARE
    _preset_seq          BIGINT;
BEGIN
    SELECT MAX(preset_seq) + 1 INTO _preset_seq FROM camera_preset_history WHERE preset_id = NEW.preset_id;
    NEW.preset_seq := coalesce(_preset_seq, 1);
    NEW.preset_seq_prev := coalesce(_preset_seq, 1) - 1;
    RETURN NEW;
END;
$$ language 'plpgsql';

create index camera_preset_history_search_1_i on camera_preset_history(preset_seq_prev, preset_id, publishable, modified);
create index camera_preset_history_search_2_i on camera_preset_history(preset_seq, preset_id, publishable);
create index camera_preset_history_search_3_i on camera_preset_history(camera_id, modified);
create index camera_preset_history_search_4_i on camera_preset_history(preset_id, modified);
