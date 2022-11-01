-- Updates modified column only when data has changed
CREATE OR REPLACE FUNCTION update_modified_column()
  RETURNS TRIGGER AS $$
BEGIN
  IF (to_jsonb(OLD.*) <> to_jsonb(NEW.*)) THEN
    NEW.modified = now();
  END IF;
  RETURN NEW;
END;
$$ language 'plpgsql';


-- Updates modified column also when data has not changed
CREATE OR REPLACE FUNCTION update_modified_column_always()
  RETURNS TRIGGER AS $$
BEGIN
  NEW.modified = now();
  RETURN NEW;
END;
$$ language 'plpgsql';


-- Updates modified column only when data is changed, excluding changes to pic_last_modified-field
CREATE OR REPLACE FUNCTION update_camera_preset_modified_column()
  RETURNS TRIGGER AS $$
BEGIN
  -- remove field from json with -
  IF (to_jsonb(OLD.*) - 'pic_last_modified') <> (to_jsonb(NEW.*) - 'pic_last_modified') THEN
    NEW.modified = now();
  END IF;
  RETURN NEW;
END;
$$ language 'plpgsql';
