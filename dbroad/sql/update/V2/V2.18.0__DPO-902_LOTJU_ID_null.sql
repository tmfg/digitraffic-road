ALTER TABLE camera_preset ALTER COLUMN lotju_id SET NOT NULL;
ALTER TABLE camera_preset ALTER COLUMN camera_lotju_id SET NOT NULL;
ALTER TABLE lam_station ALTER COLUMN lotju_id SET NOT NULL;
ALTER TABLE road_station ALTER COLUMN lotju_id SET NOT NULL;
ALTER TABLE road_station_sensor ALTER COLUMN lotju_id SET NOT NULL;
ALTER TABLE weather_station ALTER COLUMN lotju_id SET NOT NULL;

DROP INDEX road_station_lotju_ui;
CREATE UNIQUE INDEX road_station_lotju_ui
    ON road_station
        USING BTREE (road_station_type ASC, obsolete_date ASC, lotju_id ASC);

DROP INDEX rs_type_lotju_id_fk_ui;
CREATE UNIQUE INDEX rs_type_lotju_id_fk_ui
    ON road_station
        USING BTREE (road_station_type ASC, lotju_id ASC);


DROP INDEX road_station_sensor_lotju_ui;
CREATE UNIQUE INDEX road_station_sensor_lotju_ui
    ON road_station_sensor
        USING BTREE (road_station_type ASC, obsolete_date ASC, lotju_id ASC);

DROP INDEX camera_preset_publishable_lotju_i;
CREATE INDEX camera_preset_publishable_lotju_i
    ON road.public.camera_preset
        USING BTREE (publishable DESC, lotju_id asc);

DROP INDEX weather_station_lotju_i;
CREATE INDEX weather_station_lotju_i
    ON weather_station
        USING BTREE (lotju_id asc);

CREATE OR REPLACE FUNCTION f_trigger_vc$camera_preset()
    RETURNS trigger
AS
$BODY$
BEGIN
    NEW.publishable :=
            CASE
                WHEN (NEW.obsolete_date IS NULL AND NEW.is_public = true) THEN true
                ELSE false
                END;
    RETURN NEW;
END;
$BODY$
    LANGUAGE  plpgsql;


CREATE OR REPLACE FUNCTION f_trigger_vc$road_station()
    RETURNS trigger
AS
$BODY$
BEGIN
    NEW.publishable :=
            CASE
                WHEN (NEW.obsolete_date IS NULL AND NEW.collection_status <> 'REMOVED_PERMANENTLY' AND NEW.is_public = true) THEN true
                ELSE false
                END;
    RETURN NEW;
END;
$BODY$
    LANGUAGE  plpgsql;

CREATE INDEX road_station_publishable_i
    ON road_station
        USING BTREE ((CASE
                          WHEN (obsolete_date IS NULL AND collection_status <> 'REMOVED_PERMANENTLY' AND is_public = true) THEN true
                          ELSE false
            END) ASC, type ASC);

CREATE OR REPLACE FUNCTION f_trigger_vc$road_station_sensor()
    RETURNS trigger
AS
$BODY$
BEGIN
    NEW.PUBLISHABLE :=
            CASE
                WHEN (NEW.IS_PUBLIC IS true AND NEW.OBSOLETE_DATE IS NULL) THEN true
                ELSE false
                END;
    RETURN NEW;
END;
$BODY$
    LANGUAGE  plpgsql;