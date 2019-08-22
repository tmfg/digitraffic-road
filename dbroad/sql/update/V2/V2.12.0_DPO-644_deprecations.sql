ALTER TABLE camera_preset
    DROP COLUMN roadstation_id,
    DROP COLUMN nearest_roadstation_id;

ALTER TABLE lam_station
    DROP COLUMN obsolete;

ALTER TABLE road
    DROP COLUMN obsolete;