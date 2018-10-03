ALTER TABLE ROAD_STATION_SENSOR
ADD PUBLISHABLE     BOOLEAN NOT NULL DEFAULT FALSE;

CREATE OR REPLACE FUNCTION f_trigger_vc$road_station_sensor()
RETURNS trigger
AS
$BODY$
BEGIN
    NEW.PUBLISHABLE :=
    CASE
        WHEN (NEW.IS_PUBLIC IS true AND NEW.OBSOLETE_DATE IS NULL AND NEW.LOTJU_ID IS NOT NULL)
        THEN true
        ELSE false
    END;
    RETURN NEW;
END;
$BODY$
LANGUAGE  plpgsql;

-- DROP TRIGGER IF EXISTS trigger_vc$road_station_sensor on road_station_sensor;
CREATE TRIGGER trigger_vc$road_station_sensor
BEFORE INSERT OR UPDATE
ON road_station_sensor
FOR EACH ROW
EXECUTE PROCEDURE f_trigger_vc$road_station_sensor();

UPDATE road_station_sensor
set id = id;


ALTER TABLE ROAD_STATION
ADD PURPOSE CHARACTER VARYING(4000);
