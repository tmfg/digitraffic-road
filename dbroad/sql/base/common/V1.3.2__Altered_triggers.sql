CREATE OR REPLACE FUNCTION f_trigger_vc$road_station()
RETURNS trigger
AS
$BODY$
BEGIN
    NEW.publishable :=
    CASE
        WHEN (NEW.obsolete_date IS NULL AND NEW.collection_status <> 'REMOVED_PERMANENTLY' AND NEW.is_public = true AND NEW.lotju_id IS NOT
        NULL) THEN true
        ELSE false
    END;
    RETURN NEW;
END;
$BODY$
LANGUAGE  plpgsql;

CREATE TRIGGER trigger_vc$road_station
BEFORE INSERT OR UPDATE
ON road_station
FOR EACH ROW
EXECUTE PROCEDURE f_trigger_vc$road_station();
