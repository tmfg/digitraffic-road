-- handle error if column has been already renamed
DO
$$
  BEGIN
    ALTER TABLE location_version
      RENAME COLUMN updated TO modified;
  EXCEPTION
    WHEN undefined_column THEN
  END;
$$;

ALTER TABLE location_version
  ALTER COLUMN modified SET DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();

DROP TRIGGER IF EXISTS location_version_modified_t on location_version;

update location_version
  set created = modified
  where created > modified;

CREATE TRIGGER location_version_modified_t BEFORE UPDATE ON location_version FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

