CREATE TABLE IF NOT EXISTS forecast_section_coordinate_list (
  forecast_section_id                 NUMERIC(10),
  order_number                        INTEGER
);

CREATE TABLE IF NOT EXISTS forecast_section_coordinate (
  forecast_section_id   NUMERIC(10),
  list_order_number     INTEGER,
  order_number          INTEGER,
  longitude             NUMERIC(10,7),
  latitude              NUMERIC(10,7)
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

ALTER TABLE forecast_section ALTER COLUMN natural_id TYPE varchar(30);

ALTER TABLE forecast_section
  ADD CONSTRAINT forecast_section_unique EXCLUDE (natural_id WITH =, version WITH =, (CASE
                                                                                      WHEN obsolete_date IS NULL
                                                                                        THEN '-1' :: integer :: numeric
                                                                                      ELSE id
                                                                                      END) WITH =);

CREATE INDEX forecast_section_index ON forecast_section (id, natural_id, version);

create unique index if not exists forecast_section_ui
  on forecast_section (natural_id, version, (
    CASE
    WHEN obsolete_date IS NULL THEN '-1'::integer::numeric
    ELSE id
    END))
;

CREATE TABLE IF NOT EXISTS road_segment (
  forecast_section_id NUMERIC(10),
  order_number        INTEGER,
  start_distance      INTEGER,
  end_distance        INTEGER,
  carriageway         INTEGER
);

ALTER TABLE road_segment ADD CONSTRAINT road_segment_pk PRIMARY KEY (forecast_section_id, order_number);

ALTER TABLE road_segment
  ADD CONSTRAINT road_segment_foresec_fk FOREIGN KEY (forecast_section_id)
REFERENCES forecast_section (id)
ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS link_id (
  forecast_section_id NUMERIC(10),
  order_number        INTEGER,
  link_id             BIGINT
);

ALTER TABLE link_id ADD CONSTRAINT link_id_pk PRIMARY KEY (forecast_section_id, order_number);

ALTER TABLE link_id
  ADD CONSTRAINT link_id_foresec_fk FOREIGN KEY (forecast_section_id)
REFERENCES forecast_section (id)
ON DELETE CASCADE;

DROP TABLE forecast_section_coordinates;

CREATE INDEX forecast_section_version ON forecast_section (id, natural_id, version);

ALTER TABLE forecast_section_weather DROP CONSTRAINT foresec_weather_foresec_fk;
ALTER TABLE forecast_section_weather
  ADD CONSTRAINT foresec_weather_foresec_fk FOREIGN KEY (forecast_section_id)
REFERENCES forecast_section (id)
ON DELETE CASCADE;

ALTER TABLE forecast_condition_reason DROP CONSTRAINT weather_reason_weather_fk;
ALTER TABLE forecast_condition_reason
  ADD CONSTRAINT weather_reason_weather_fk FOREIGN KEY (forecast_section_id, forecast_name)
REFERENCES forecast_section_weather (forecast_section_id, forecast_name)
ON DELETE CASCADE;