CREATE OR REPLACE FUNCTION update_modified_column()
  RETURNS TRIGGER AS $$
BEGIN
  -- Update modified column only when data is changed
  IF (to_jsonb(OLD.*) <> to_jsonb(NEW.*)) THEN
    NEW.modified = now();
  END IF;
  RETURN NEW;
END;
$$ language 'plpgsql';



CREATE OR REPLACE FUNCTION update_camera_preset_modified_column()
  RETURNS TRIGGER AS $$
BEGIN
  -- Update modified column only when data is changed and excluding changes to pic_last_modified field
  IF (to_jsonb(OLD.*) - 'pic_last_modified') <> (to_jsonb(NEW.*) - 'pic_last_modified') THEN
    NEW.modified = now();
  END IF;
  RETURN NEW;
END;
$$ language 'plpgsql';


