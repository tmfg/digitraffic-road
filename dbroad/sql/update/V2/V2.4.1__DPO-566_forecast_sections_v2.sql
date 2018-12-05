CREATE TABLE IF NOT EXISTS forecast_section_coordinate_list (
  forecast_section_id                 NUMERIC(10),
  order_number                        INTEGER
);

CREATE SEQUENCE IF NOT EXISTS seq_forecast_section_coordinate INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS forecast_section_coordinate (
  forecast_section_id   NUMERIC(10),
  list_order_number     INTEGER,
  order_number          INTEGER,
  longitude             NUMERIC(6,3),
  latitude              NUMERIC(6,3)
);

ALTER TABLE forecast_section_coordinate ADD CONSTRAINT forecast_section_coordinate_pk PRIMARY KEY (forecast_section_id, list_order_number, order_number);

ALTER TABLE forecast_section_coordinate_list ADD CONSTRAINT forsec_coord_list_pk PRIMARY KEY(forecast_section_id, order_number);

ALTER TABLE forecast_section_coordinate_list
  ADD CONSTRAINT foresec_coord_list_fk FOREIGN KEY (forecast_section_id)
REFERENCES forecast_section (id)
ON DELETE CASCADE;

ALTER TABLE forecast_section_coordinate
  ADD CONSTRAINT foresec_coord_list_coord_fk FOREIGN KEY (forecast_section_id, list_order_number)
REFERENCES forecast_section_coordinate_list (forecast_section_id, order_number)
ON DELETE CASCADE;

ALTER TABLE forecast_section ADD COLUMN version INTEGER;

UPDATE forecast_section set version = 1;

create or replace function "f_trigger_vc$forecast_section"() returns trigger
language plpgsql
as $$
BEGIN
  NEW.road_number := SUBSTR(NEW.natural_id, 1, 5);
  NEW.road_section_number := SUBSTR(NEW.natural_id, 7, 3);
  NEW.road_section_version_number := CASE WHEN NEW.version = 1 THEN SUBSTR(NEW.natural_id, 11, 3) ELSE '0' END;
  RETURN NEW;
END;
$$
;