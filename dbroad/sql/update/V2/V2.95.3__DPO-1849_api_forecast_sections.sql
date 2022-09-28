-- change text fields to numeric
alter table forecast_section alter column road_number TYPE NUMERIC(10) USING (road_number::NUMERIC(10));
alter table forecast_section alter column road_section_number TYPE NUMERIC(10) USING (road_section_number::NUMERIC(10));
alter table forecast_section alter column road_section_version_number TYPE NUMERIC(10) USING (road_section_version_number::NUMERIC(10));

-- add modified and created columns to road station
ALTER TABLE forecast_section
  ADD COLUMN IF NOT EXISTS geometry GEOMETRY(GEOMETRYZ, 4326),     -- 4326 = WGS84
  ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();

-- trigger to update modified column
DROP TRIGGER IF EXISTS forecast_section_modified_t on forecast_section;
CREATE TRIGGER forecast_section_modified_t BEFORE UPDATE ON forecast_section FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

CREATE INDEX IF NOT EXISTS forecast_section_version_modified_i on forecast_section(version, modified);