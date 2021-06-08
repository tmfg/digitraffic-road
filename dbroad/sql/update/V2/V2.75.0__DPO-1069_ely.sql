ALTER TABLE lam_station
DROP COLUMN IF EXISTS road_district_id;

ALTER TABLE IF EXISTS lam_station RENAME TO tms_station;
ALTER SEQUENCE IF EXISTS seq_lam_station RENAME TO seq_tms_station;

DO
$$
    BEGIN
        ALTER TABLE tms_station rename column lam_station_type TO tms_station_type;
    EXCEPTION
        WHEN undefined_column THEN
    END;
$$;

ALTER TABLE tms_sensor_constant ADD COLUMN IF NOT EXISTS created TIMESTAMPTZ(0) DEFAULT now();
ALTER TABLE tms_sensor_constant ADD COLUMN IF NOT EXISTS modified TIMESTAMPTZ(0) DEFAULT now();

ALTER TABLE tms_sensor_constant_value ADD COLUMN IF NOT EXISTS created TIMESTAMPTZ(0) DEFAULT now();
ALTER TABLE tms_sensor_constant_value ADD COLUMN IF NOT EXISTS modified TIMESTAMPTZ(0) DEFAULT now();

DROP TRIGGER IF EXISTS tms_sensor_constant_modified_trigger on tms_sensor_constant;
DROP TRIGGER IF EXISTS tms_sensor_constant_value_modified_trigger on tms_sensor_constant_value;

DO
$$
    BEGIN
        UPDATE tms_sensor_constant_value
        set modified = updated;
        UPDATE tms_sensor_constant
        set modified = updated;
    EXCEPTION
        WHEN undefined_column THEN
    END;
$$;

ALTER TABLE tms_sensor_constant_value drop column if exists updated;
ALTER TABLE tms_sensor_constant drop column if exists updated;

-- Automatic modified update
CREATE TRIGGER tms_sensor_constant_modified_trigger BEFORE UPDATE ON tms_sensor_constant FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
CREATE TRIGGER tms_sensor_constant_value_modified_trigger BEFORE UPDATE ON tms_sensor_constant_value FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

DROP TABLE IF EXISTS speed_limit_season_history;
ALTER TABLE road_section DROP COLUMN IF EXISTS road_district_id;
DROP TABLE IF EXISTS road_district;