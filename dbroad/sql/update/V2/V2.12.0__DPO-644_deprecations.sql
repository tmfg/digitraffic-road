ALTER TABLE camera_preset
    DROP COLUMN roadstation_id,
    DROP COLUMN nearest_roadstation_id;


ALTER TABLE lam_station
    DROP CONSTRAINT lam_station_uk2,
    ADD CONSTRAINT lam_station_uk2 UNIQUE (road_station_id, obsolete_date);
ALTER TABLE lam_station
    DROP COLUMN obsolete;


ALTER TABLE road
    DROP CONSTRAINT road_uk1,
    ADD CONSTRAINT road_uk1 UNIQUE (natural_id, obsolete_date);
ALTER TABLE road
    DROP COLUMN obsolete;


ALTER TABLE road_district
    DROP CONSTRAINT road_district_uk1,
    ADD CONSTRAINT road_district_uk1 UNIQUE (obsolete_date, natural_id);
ALTER TABLE road_district
    DROP COLUMN obsolete;

ALTER TABLE road_section
    DROP CONSTRAINT road_section_uk1,
    ADD CONSTRAINT road_section_uk1 UNIQUE (road_id, natural_id, obsolete_date);
ALTER TABLE road_section
    DROP COLUMN obsolete;


DROP INDEX road_station_ui;
CREATE UNIQUE INDEX road_station_ui
ON road_station
    USING BTREE (natural_id ASC) where obsolete_date is null;
ALTER TABLE road_station
    DROP COLUMN obsolete;


DROP INDEX road_station_sensor_uki_fi;
CREATE UNIQUE INDEX road_station_sensor_uki_fi
    ON road_station_sensor
        USING BTREE (name_fi ASC, road_station_type ASC, natural_id ASC, obsolete_date ASC);
DROP INDEX road_station_sensor_uki_name;
CREATE UNIQUE INDEX road_station_sensor_uki_name
    ON road_station_sensor
        USING BTREE (name ASC, road_station_type ASC, natural_id ASC, obsolete_date ASC);
ALTER TABLE road_station_sensor
    DROP COLUMN obsolete;

DROP TABLE static_data_status;