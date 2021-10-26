ALTER TABLE road_station_sensor
ADD CONSTRAINT road_station_sensor_obsolete_c CHECK (((obsolete = false AND obsolete_date IS NULL) OR (obsolete = true AND
obsolete_date IS NOT
NULL)));

ALTER TABLE road_station
ADD CONSTRAINT road_station_obsolete_c CHECK (((obsolete = false AND obsolete_date IS NULL) OR (obsolete = true AND obsolete_date IS NOT
NULL)));

ALTER TABLE lam_station
ADD CONSTRAINT lam_station_obsolete_c CHECK (obsolete = false OR obsolete_date IS NOT NULL);

ALTER TABLE road_district
ADD CONSTRAINT road_district_obsolete_c CHECK (obsolete = false OR obsolete_date IS NOT NULL);

ALTER TABLE road_section
ADD CONSTRAINT road_section_obsolete_c CHECK (obsolete = false OR obsolete_date IS NOT NULL);

ALTER TABLE road
ADD CONSTRAINT road_obsolete_c CHECK (obsolete = false OR obsolete_date IS NOT NULL);