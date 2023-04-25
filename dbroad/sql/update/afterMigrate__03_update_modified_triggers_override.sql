-- If needed add here drop for automatically created modified trigger and create manually new i.e.
-- DROP TRIGGER IF EXISTS vessel_location_modified_t on vessel_location;
-- CREATE TRIGGER vessel_location_modified_t BEFORE UPDATE ON vessel_location FOR EACH ROW EXECUTE PROCEDURE update_modified_column_always();

DROP TRIGGER IF EXISTS CAMERA_PRESET_MODIFIED_T on camera_preset;
CREATE TRIGGER CAMERA_PRESET_MODIFIED_T
  BEFORE UPDATE
  ON camera_preset
  FOR EACH ROW
EXECUTE PROCEDURE update_camera_preset_modified_column();

