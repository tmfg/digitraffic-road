-- ------------ Write CREATE-SEQUENCE-stage scripts -----------


CREATE SEQUENCE seq_allowed_sensor
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_camera_preset
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_data_updated
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_datex2
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_datex2situation
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_datex2situationrecord
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;




CREATE SEQUENCE seq_fluency_class
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_forecast_section
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_journeytime_measurement
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_journeytime_median
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_journeytime_normal_value
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_lam_station
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_lam_station_data
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_latest_journeytime_median
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_road
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_road_address
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_road_district
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_road_section
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_road_station
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_road_station_sensor
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_road_weather_station
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;


CREATE SEQUENCE seq_sensor_value
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_situation_record_comment
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_speed_limit_history
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



CREATE SEQUENCE seq_travel_time_import_status
INCREMENT BY 1
MAXVALUE 9223372036854775807
MINVALUE 1
NO CYCLE;



-- ------------ Write CREATE-DOMAIN-stage scripts -----------

CREATE DOMAIN numbertable_type AS DOUBLE PRECISION ARRAY;



-- ------------ Write CREATE-TABLE-stage scripts -----------


CREATE TABLE IF NOT EXISTS camera_preset(
id NUMERIC(10,0) NOT NULL,
camera_id CHARACTER VARYING(24) NOT NULL,
preset_id CHARACTER VARYING(32) NOT NULL,
roadstation_id NUMERIC(10,0),
public_external boolean NOT NULL DEFAULT true,
public_internal boolean NOT NULL DEFAULT true,
preset_name_1 CHARACTER VARYING(800),
preset_name_2 CHARACTER VARYING(800),
nearest_roadstation_id NUMERIC(10,0),
pic_last_modified TIMESTAMP(6) WITHOUT TIME ZONE,
preset_order NUMERIC(10,0),
road_station_id NUMERIC(10,0),
nearest_rd_weather_station_id NUMERIC(10,0),
lotju_id NUMERIC(10,0),
in_collection boolean,
compression NUMERIC(10,0),
direction CHARACTER VARYING(200),
default_direction boolean,
resolution CHARACTER VARYING(200),
camera_lotju_id NUMERIC(10,0),
camera_type CHARACTER VARYING(100),
camera_description CHARACTER VARYING(200),
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE,
publishable boolean
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS data_updated(
id NUMERIC(10,0) NOT NULL,
data_type CHARACTER VARYING(128) NOT NULL,
updated TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
version CHARACTER VARYING(64)
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS datex2(
id NUMERIC(10,0) NOT NULL,
import_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
message TEXT,
publication_time TIMESTAMP(0) WITHOUT TIME ZONE
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS datex2_situation(
id NUMERIC(10,0) NOT NULL,
datex2_id NUMERIC(10,0) NOT NULL,
situation_id CHARACTER VARYING(200) NOT NULL,
version_time TIMESTAMP(0) WITHOUT TIME ZONE
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS datex2_situation_record(
id NUMERIC(10,0) NOT NULL,
datex2_situation_id NUMERIC(10,0) NOT NULL,
situation_record_id CHARACTER VARYING(200),
creation_time TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
version_time TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
observation_time TIMESTAMP(0) WITHOUT TIME ZONE,
validy_status CHARACTER VARYING(200) NOT NULL,
overall_start_time TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
overall_end_time TIMESTAMP(0) WITHOUT TIME ZONE,
type CHARACTER VARYING(200) NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS direction(
natural_id NUMERIC(10,0) NOT NULL,
name_fi CHARACTER VARYING(256),
name_sv CHARACTER VARYING(256),
name_en CHARACTER VARYING(256),
rdi CHARACTER VARYING(1),
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS forecast_condition_reason(
forecast_section_id NUMERIC(10,0) NOT NULL,
forecast_name CHARACTER VARYING(3) NOT NULL,
precipitation_condition CHARACTER VARYING(32),
road_condition CHARACTER VARYING(32),
wind_condition CHARACTER VARYING(32),
freezing_rain_condition boolean,
winter_slipperiness boolean,
visibility_condition CHARACTER VARYING(32),
friction_condition CHARACTER VARYING(32)
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS forecast_section(
id NUMERIC(10,0) NOT NULL,
natural_id CHARACTER VARYING(16) NOT NULL,
road_id NUMERIC(10,0),
description CHARACTER VARYING(128) NOT NULL,
start_road_section_id NUMERIC(10,0),
start_distance NUMERIC(5,0),
end_road_section_id NUMERIC(10,0),
end_distance NUMERIC(5,0),
length NUMERIC(6,0),
road_number CHARACTER VARYING(5),
road_section_number CHARACTER VARYING(3),
road_section_version_number CHARACTER VARYING(3),
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS forecast_section_coordinates(
forecast_section_id NUMERIC(10,0) NOT NULL,
order_number NUMERIC(3,0) NOT NULL,
longitude NUMERIC(6,3) NOT NULL,
latitude NUMERIC(6,3) NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS forecast_section_weather(
forecast_section_id NUMERIC(10,0) NOT NULL,
forecast_name CHARACTER VARYING(3) NOT NULL,
time TIMESTAMP(6) WITH TIME ZONE NOT NULL,
daylight boolean NOT NULL,
overall_road_condition CHARACTER VARYING(32) NOT NULL,
reliability CHARACTER VARYING(32) NOT NULL,
road_temperature CHARACTER VARYING(6) NOT NULL,
temperature CHARACTER VARYING(6) NOT NULL,
weather_symbol CHARACTER VARYING(4) NOT NULL,
wind_direction NUMERIC(3,0) NOT NULL,
wind_speed NUMERIC(4,1) NOT NULL,
type CHARACTER VARYING(11)
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS lam_station(
id NUMERIC(10,0) NOT NULL,
natural_id NUMERIC(10,0) NOT NULL,
name CHARACTER VARYING(200) NOT NULL,
obsolete boolean NOT NULL,
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE,
summer_free_flow_speed_1 NUMERIC(7,3) NOT NULL,
summer_free_flow_speed_2 NUMERIC(7,3) NOT NULL,
winter_free_flow_speed_1 NUMERIC(7,3) NOT NULL,
winter_free_flow_speed_2 NUMERIC(7,3) NOT NULL,
road_district_id NUMERIC(10,0),
road_station_id NUMERIC(10,0) NOT NULL,
lotju_id NUMERIC(10,0),
lam_station_type CHARACTER VARYING(100),
calculator_device_type CHARACTER VARYING(100),
direction_1_municipality CHARACTER VARYING(200),
direction_1_municipality_code NUMERIC(10,0),
direction_2_municipality CHARACTER VARYING(200),
direction_2_municipality_code NUMERIC(10,0)
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS lam_station_data(
id NUMERIC(10,0) NOT NULL,
measured TIMESTAMP(0) WITHOUT TIME ZONE,
traffic_volume_1 DOUBLE PRECISION,
traffic_volume_2 DOUBLE PRECISION,
average_speed_1 NUMERIC(7,3),
average_speed_2 NUMERIC(7,3),
last_modified TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
lam_station_id NUMERIC(10,0) NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS location(
version CHARACTER VARYING(64) NOT NULL,
location_code NUMERIC(6,0) NOT NULL,
subtype_code CHARACTER VARYING(8) NOT NULL,
road_junction CHARACTER VARYING(64),
road_name CHARACTER VARYING(64),
first_name CHARACTER VARYING(64),
second_name CHARACTER VARYING(64),
area_ref NUMERIC(6,0),
linear_ref NUMERIC(6,0),
neg_offset NUMERIC(6,0),
pos_offset NUMERIC(6,0),
urban boolean,
wgs84_lat NUMERIC(7,5),
wgs84_long NUMERIC(7,5),
etrs_tm35fin_x NUMERIC(18,9),
etrs_tm35fin_y NUMERIC(18,9),
neg_direction CHARACTER VARYING(64),
pos_direction CHARACTER VARYING(64),
geocode CHARACTER VARYING(16),
order_of_point NUMERIC(3,0)
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS location_subtype(
version CHARACTER VARYING(64) NOT NULL,
subtype_code CHARACTER VARYING(8) NOT NULL,
description_en CHARACTER VARYING(32) NOT NULL,
description_fi CHARACTER VARYING(32) NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS location_type(
version CHARACTER VARYING(64) NOT NULL,
type_code CHARACTER VARYING(8) NOT NULL,
description_en CHARACTER VARYING(32) NOT NULL,
description_fi CHARACTER VARYING(32) NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS location_version(
version CHARACTER VARYING(64) NOT NULL,
updated TIMESTAMP(6) WITH TIME ZONE NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS locking_table(
lock_name CHARACTER VARYING(40) NOT NULL,
instance_id CHARACTER VARYING(80),
lock_locked TIMESTAMP(0) WITHOUT TIME ZONE,
lock_expires TIMESTAMP(0) WITHOUT TIME ZONE
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS road(
id NUMERIC(10,0) NOT NULL,
natural_id NUMERIC(10,0) NOT NULL,
obsolete boolean NOT NULL,
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE,
name CHARACTER VARYING(400)
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS road_address(
id NUMERIC(10,0) NOT NULL,
road_number NUMERIC(10,0),
road_section NUMERIC(10,0),
distance_from_road_section_st NUMERIC(10,0),
carriageway NUMERIC(10,0),
side NUMERIC(10,0),
road_maintenance_class CHARACTER VARYING(200),
contract_area CHARACTER VARYING(200),
contract_area_code NUMERIC(10,0)
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS road_district(
id NUMERIC(10,0) NOT NULL,
name CHARACTER VARYING(200),
obsolete boolean NOT NULL,
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE,
natural_id NUMERIC(10,0) NOT NULL,
speed_limit_season NUMERIC(1,0) NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS road_section(
id NUMERIC(10,0) NOT NULL,
natural_id NUMERIC(10,0) NOT NULL,
obsolete boolean NOT NULL,
begin_distance NUMERIC(10,0),
end_distance NUMERIC(10,0),
road_district_id NUMERIC(10,0) NOT NULL,
road_id NUMERIC(10,0) NOT NULL,
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS road_station(
id NUMERIC(10,0) NOT NULL,
natural_id NUMERIC(10,0) NOT NULL,
name CHARACTER VARYING(200) NOT NULL,
type NUMERIC(1,0) NOT NULL,
obsolete boolean NOT NULL,
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE,
name_fi CHARACTER VARYING(200),
name_sv CHARACTER VARYING(200),
name_en CHARACTER VARYING(200),
latitude NUMERIC(10,0),
longitude NUMERIC(10,0),
collection_interval NUMERIC(10,0),
collection_status CHARACTER VARYING(200),
municipality CHARACTER VARYING(200),
municipality_code CHARACTER VARYING(200),
province CHARACTER VARYING(200),
province_code CHARACTER VARYING(200),
additional_information CHARACTER VARYING(200),
road_address_id NUMERIC(10,0),
livi_id CHARACTER VARYING(200),
start_date TIMESTAMP(0) WITHOUT TIME ZONE,
repair_maintenance_date TIMESTAMP(0) WITHOUT TIME ZONE,
annual_maintenance_date TIMESTAMP(0) WITHOUT TIME ZONE,
state CHARACTER VARYING(200),
location CHARACTER VARYING(200),
country CHARACTER VARYING(200),
is_public boolean NOT NULL,
road_station_type CHARACTER VARYING(200) NOT NULL,
lotju_id NUMERIC(10,0),
publishable boolean,
altitude NUMERIC(10,2)
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS road_station_sensor(
id NUMERIC(10,0) NOT NULL,
natural_id NUMERIC(10,0) NOT NULL,
name CHARACTER VARYING(200) NOT NULL,
obsolete boolean NOT NULL,
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE,
lotju_id NUMERIC(10,0),
description CHARACTER VARYING(200),
name_fi CHARACTER VARYING(200),
short_name_fi CHARACTER VARYING(200),
unit CHARACTER VARYING(10),
accuracy NUMERIC(10,0),
road_station_type CHARACTER VARYING(200)
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS road_station_sensors(
road_station_id NUMERIC(10,0) NOT NULL,
road_station_sensor_id NUMERIC(10,0) NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS road_station_type(
road_station_type CHARACTER VARYING(200) NOT NULL,
type NUMERIC(1,0) NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS sensor_value(
id NUMERIC(12,0) NOT NULL,
value NUMERIC(10,2) NOT NULL,
measured TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
road_station_sensor_id NUMERIC(10,0) NOT NULL,
road_station_id NUMERIC(10,0) NOT NULL,
updated TIMESTAMP(0) WITHOUT TIME ZONE,
time_window_start TIMESTAMP(0) WITHOUT TIME ZONE,
time_window_end TIMESTAMP(0) WITHOUT TIME ZONE
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS sensor_value_description(
sensor_id NUMERIC(10,0) NOT NULL,
sensor_value NUMERIC(10,2) NOT NULL,
description_fi CHARACTER VARYING(64) NOT NULL,
description_en CHARACTER VARYING(64) NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS site(
natural_id NUMERIC(10,0) NOT NULL,
name_fi CHARACTER VARYING(256),
name_sv CHARACTER VARYING(256),
name_en CHARACTER VARYING(256),
road_section_id NUMERIC(10,0),
road_section_begin_distance NUMERIC(38,0),
x_coord_kkj3 NUMERIC(38,0),
y_coord_kkj3 NUMERIC(38,0),
longitude_wgs84 NUMERIC(6,3),
latitude_wgs84 NUMERIC(6,3),
obsolete_date TIMESTAMP(0) WITHOUT TIME ZONE
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS situation_record_comment_i18n(
id NUMERIC(10,0) NOT NULL,
datex2_situation_record_id NUMERIC(10,0) NOT NULL,
lang CHARACTER VARYING(2) NOT NULL,
value TEXT NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS speed_limit_season_history(
id NUMERIC(10,0) NOT NULL,
new_speed_limit_season NUMERIC(1,0) NOT NULL,
road_district_id NUMERIC(10,0) NOT NULL,
changed TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS static_data_status(
id NUMERIC(10,0) NOT NULL,
link_data_last_updated TIMESTAMP(0) WITHOUT TIME ZONE,
lam_data_last_updated TIMESTAMP(0) WITHOUT TIME ZONE,
rws_data_last_updated TIMESTAMP(0) WITHOUT TIME ZONE,
camerapreset_data_last_updated TIMESTAMP(0) WITHOUT TIME ZONE,
rw_sensor_data_last_updated TIMESTAMP(0) WITHOUT TIME ZONE
)
WITH (
OIDS=FALSE
);



CREATE TABLE IF NOT EXISTS weather_station(
id NUMERIC(10,0) NOT NULL,
weather_station_type CHARACTER VARYING(100),
road_station_id NUMERIC(10,0) NOT NULL,
lotju_id NUMERIC(10,0),
master boolean NOT NULL
)
WITH (
OIDS=FALSE
);



-- ------------ Write CREATE-INDEX-stage scripts -----------


CREATE INDEX camera_preset_nearest_rws_fki
ON camera_preset
USING BTREE (nearest_rd_weather_station_id ASC);


CREATE INDEX camera_preset_road_station_fki
ON camera_preset
USING BTREE (road_station_id ASC);



CREATE INDEX cp_pic_last_modified_i
ON camera_preset
USING BTREE (pic_last_modified ASC);



CREATE UNIQUE INDEX data_updated_ui
ON data_updated
USING BTREE (data_type ASC);



CREATE INDEX situation_datex2_fk_i
ON datex2_situation
USING BTREE (datex2_id ASC);



CREATE INDEX situation_record_situation_fki
ON datex2_situation_record
USING BTREE (datex2_situation_id ASC);



CREATE INDEX forsec_ers_fk_i
ON forecast_section
USING BTREE (end_road_section_id ASC);



CREATE INDEX forsec_road_fk_i
ON forecast_section
USING BTREE (road_id ASC);



CREATE INDEX forsec_srs_fk_i
ON forecast_section
USING BTREE (start_road_section_id ASC);



CREATE INDEX lam_station_road_district_fki
ON lam_station
USING BTREE (road_district_id ASC);



CREATE UNIQUE INDEX lam_station_uk1
ON lam_station
USING BTREE (natural_id ASC, (CASE
    WHEN obsolete_date IS NULL THEN 0
    ELSE id
END) ASC);


CREATE INDEX loc_loc_area_fk_i
ON location
USING BTREE (version ASC, area_ref ASC);



CREATE INDEX loc_loc_linear_fk_i
ON location
USING BTREE (version ASC, linear_ref ASC);



CREATE INDEX loc_lst_fk_i
ON location
USING BTREE (version ASC, subtype_code ASC);



CREATE UNIQUE INDEX locking_table_ui
ON locking_table
USING BTREE (lock_name ASC, instance_id ASC);



CREATE INDEX road_index1
ON road
USING BTREE (natural_id ASC);



CREATE INDEX road_district_i
ON road_district
USING BTREE (id ASC, speed_limit_season ASC);



CREATE INDEX road_district_index1
ON road_district
USING BTREE (natural_id ASC);



CREATE INDEX road_section_index1
ON road_section
USING BTREE (natural_id ASC);



CREATE INDEX road_section_index2
ON road_section
USING BTREE (road_district_id ASC);



CREATE INDEX road_section_index3
ON road_section
USING BTREE (road_id ASC);



CREATE INDEX road_station_lotju_i
ON road_station
USING BTREE (lotju_id ASC, road_station_type ASC, obsolete_date ASC);



CREATE UNIQUE INDEX road_station_lotju_ui
ON road_station
USING BTREE (road_station_type ASC, obsolete_date ASC, (CASE
    WHEN lotju_id IS NULL THEN (- 1) * id
    ELSE lotju_id
END) ASC);



CREATE INDEX road_station_ni_i
ON road_station
USING BTREE (natural_id ASC);


CREATE UNIQUE INDEX road_station_ra_ui
ON road_station
USING BTREE (road_address_id ASC);



CREATE INDEX road_station_type_i1
ON road_station
USING BTREE (road_station_type ASC);



CREATE UNIQUE INDEX road_station_ui
ON road_station
USING BTREE (natural_id ASC, obsolete ASC, (CASE
    WHEN obsolete_date IS NOT NULL THEN id
    ELSE 1
END) ASC);



CREATE UNIQUE INDEX rs_type_lotju_id_fk_ui
ON road_station
USING BTREE (road_station_type ASC, (CASE
    WHEN lotju_id IS NULL THEN (- 1) * id
    ELSE lotju_id
END) ASC);



CREATE UNIQUE INDEX road_station_sensor_lotju_ui
ON road_station_sensor
USING BTREE (road_station_type ASC, obsolete_date ASC, (CASE
    WHEN lotju_id IS NULL THEN (- 1) * id
    ELSE lotju_id
END) ASC);



CREATE INDEX road_station_sensor_osolete_i
ON road_station_sensor
USING BTREE (obsolete ASC, id ASC);



CREATE UNIQUE INDEX road_station_sensor_uki
ON road_station_sensor
USING BTREE (road_station_type ASC, natural_id ASC, lotju_id ASC);



CREATE UNIQUE INDEX road_station_sensor_uki_fi
ON road_station_sensor
USING BTREE (name_fi ASC, road_station_type ASC, natural_id ASC, obsolete ASC, obsolete_date ASC);



CREATE UNIQUE INDEX road_station_sensor_uki_name
ON road_station_sensor
USING BTREE (name ASC, road_station_type ASC, natural_id ASC, obsolete ASC, obsolete_date ASC);



CREATE INDEX rs_sensor_rs_type_fk_i
ON road_station_sensor
USING BTREE (road_station_type ASC, lotju_id ASC, obsolete_date ASC);



CREATE UNIQUE INDEX rss_haku_u
ON road_station_sensor
USING BTREE (obsolete_date ASC, natural_id ASC, id ASC);



CREATE UNIQUE INDEX road_station_sensors_rs_ufki
ON road_station_sensors
USING BTREE (road_station_id ASC, road_station_sensor_id ASC);



CREATE INDEX road_station_sensors_rss_fk_i
ON road_station_sensors
USING BTREE (road_station_sensor_id ASC);



CREATE INDEX sensor_value_road_station_fk_i
ON sensor_value
USING BTREE (road_station_id ASC, measured ASC);



CREATE UNIQUE INDEX sensor_value_search_u
ON sensor_value
USING BTREE (measured ASC, road_station_id ASC, id ASC, value ASC, updated ASC, road_station_sensor_id ASC, time_window_start ASC, time_window_end ASC);



CREATE INDEX sensor_value_updated_i
ON sensor_value
USING BTREE (updated ASC);



CREATE INDEX svd_sensor_fk_i
ON sensor_value_description
USING BTREE (sensor_value ASC);



CREATE INDEX site_road_section_fk_i
ON site
USING BTREE (road_section_id ASC);



CREATE INDEX situation_record_comment_fk_i
ON situation_record_comment_i18n
USING BTREE (datex2_situation_record_id ASC);



CREATE INDEX speed_limit_s_history_fk_i
ON speed_limit_season_history
USING BTREE (road_district_id ASC);



CREATE INDEX rws_road_station_fki
ON weather_station
USING BTREE (road_station_id ASC);



-- ------------ Write CREATE-CONSTRAINT-stage scripts -----------


ALTER TABLE camera_preset
ADD CONSTRAINT camera_preset_pk PRIMARY KEY (id);



ALTER TABLE camera_preset
ADD CONSTRAINT check_camera_lotju_id_nn CHECK (camera_lotju_id IS NOT NULL);



ALTER TABLE camera_preset
ADD CONSTRAINT check_lotju_id_nn CHECK (lotju_id IS NOT NULL);



ALTER TABLE camera_preset
ADD CONSTRAINT check_preset_id CHECK (preset_id ~ '^C[0-9]{7}$');



ALTER TABLE data_updated
ADD CONSTRAINT metad_updated_pk PRIMARY KEY (id);




ALTER TABLE datex2
ADD CONSTRAINT datex2_import_date UNIQUE (import_date);



ALTER TABLE datex2
ADD CONSTRAINT datex2_pk PRIMARY KEY (id);



ALTER TABLE datex2_situation
ADD CONSTRAINT datex2_situation_pk PRIMARY KEY (id);



ALTER TABLE datex2_situation_record
ADD CONSTRAINT datex2_situation_record_pk PRIMARY KEY (id);



ALTER TABLE direction
ADD CONSTRAINT direction_pk PRIMARY KEY (natural_id);



ALTER TABLE forecast_condition_reason
ADD CONSTRAINT foresecweather_reason_pk PRIMARY KEY (forecast_section_id, forecast_name);



ALTER TABLE forecast_section
ADD CONSTRAINT forsec_pk PRIMARY KEY (id);



ALTER TABLE forecast_section_coordinates
ADD CONSTRAINT foresec_coord_pk PRIMARY KEY (forecast_section_id, order_number);



ALTER TABLE forecast_section_weather
ADD CONSTRAINT foresec_weather_pk PRIMARY KEY (forecast_section_id, forecast_name);



ALTER TABLE forecast_section_weather
ADD CONSTRAINT sys_c001324296 CHECK (forecast_name IN ('0h', '2h', '4h', '6h', '12h'));



ALTER TABLE lam_station
ADD CONSTRAINT lam_station_pk PRIMARY KEY (id);



ALTER TABLE lam_station
ADD CONSTRAINT lam_station_uk2 UNIQUE (road_station_id, obsolete, obsolete_date);



ALTER TABLE lam_station_data
ADD CONSTRAINT lam_station_data_pk PRIMARY KEY (id);



ALTER TABLE lam_station_data
ADD CONSTRAINT lam_station_data_uk1 UNIQUE (lam_station_id);



ALTER TABLE location
ADD CONSTRAINT loc_pk PRIMARY KEY (version, location_code);



ALTER TABLE location_subtype
ADD CONSTRAINT lst_pk PRIMARY KEY (version, subtype_code);



ALTER TABLE location_type
ADD CONSTRAINT lty_pk PRIMARY KEY (version, type_code);



ALTER TABLE location_version
ADD CONSTRAINT locv_pk PRIMARY KEY (version);



ALTER TABLE locking_table
ADD CONSTRAINT sys_c001324223 PRIMARY KEY (lock_name);



ALTER TABLE road
ADD CONSTRAINT road_pk PRIMARY KEY (id);



ALTER TABLE road
ADD CONSTRAINT road_uk1 UNIQUE (natural_id, obsolete, obsolete_date);



ALTER TABLE road_address
ADD CONSTRAINT road_address_pk PRIMARY KEY (id);



ALTER TABLE road_district
ADD CONSTRAINT road_district_chk2 CHECK (speed_limit_season IN (1, 2));



ALTER TABLE road_district
ADD CONSTRAINT road_district_pk PRIMARY KEY (id);



ALTER TABLE road_district
ADD CONSTRAINT road_district_uk1 UNIQUE (obsolete, obsolete_date, natural_id);



ALTER TABLE road_section
ADD CONSTRAINT road_part_pk PRIMARY KEY (id);



ALTER TABLE road_section
ADD CONSTRAINT road_section_uk1 UNIQUE (road_id, natural_id, obsolete, obsolete_date);



ALTER TABLE road_station
ADD CONSTRAINT road_station_chk3 CHECK (type BETWEEN 1 AND 3);



ALTER TABLE road_station
ADD CONSTRAINT road_station_pk PRIMARY KEY (id);




ALTER TABLE road_station_sensor
ADD CONSTRAINT road_station_sensor_pk PRIMARY KEY (id);



ALTER TABLE road_station_type
ADD CONSTRAINT road_station_type_pk PRIMARY KEY (road_station_type);



ALTER TABLE road_station_type
ADD CONSTRAINT road_station_type_u UNIQUE (type);



ALTER TABLE sensor_value
ADD CONSTRAINT sensor_value_pk PRIMARY KEY (id);



ALTER TABLE sensor_value
ADD CONSTRAINT sensor_value_uk1 UNIQUE (road_station_sensor_id, road_station_id);



ALTER TABLE sensor_value_description
ADD CONSTRAINT svd_pk PRIMARY KEY (sensor_id, sensor_value);



ALTER TABLE site
ADD CONSTRAINT site_pk PRIMARY KEY (natural_id);



ALTER TABLE situation_record_comment_i18n
ADD CONSTRAINT situation_record_comment_pk PRIMARY KEY (id);



ALTER TABLE speed_limit_season_history
ADD CONSTRAINT speed_limit_season_history_pk PRIMARY KEY (id);



ALTER TABLE static_data_status
ADD CONSTRAINT static_data_status_pk PRIMARY KEY (id);



ALTER TABLE weather_station
ADD CONSTRAINT rws_pk PRIMARY KEY (id);



-- ------------ Write CREATE-FOREIGN-KEY-CONSTRAINT-stage scripts -----------


ALTER TABLE camera_preset
ADD CONSTRAINT camera_preset_nearest_rws_fk FOREIGN KEY (nearest_rd_weather_station_id)
REFERENCES weather_station (id)
ON DELETE NO ACTION;



ALTER TABLE camera_preset
ADD CONSTRAINT camera_preset_road_station_fk FOREIGN KEY (road_station_id)
REFERENCES road_station (id)
ON DELETE NO ACTION;



ALTER TABLE datex2_situation
ADD CONSTRAINT situation_datex2_fk FOREIGN KEY (datex2_id)
REFERENCES datex2 (id)
ON DELETE NO ACTION;



ALTER TABLE datex2_situation_record
ADD CONSTRAINT situation_record_situation_fk FOREIGN KEY (datex2_situation_id)
REFERENCES datex2_situation (id)
ON DELETE NO ACTION;



ALTER TABLE forecast_condition_reason
ADD CONSTRAINT weather_reason_weather_fk FOREIGN KEY (forecast_section_id, forecast_name)
REFERENCES forecast_section_weather (forecast_section_id, forecast_name)
ON DELETE NO ACTION;



ALTER TABLE forecast_section
ADD CONSTRAINT forsec_end_road_section_fk FOREIGN KEY (end_road_section_id)
REFERENCES road_section (id)
ON DELETE NO ACTION;



ALTER TABLE forecast_section
ADD CONSTRAINT forsec_road_fk FOREIGN KEY (road_id)
REFERENCES road (id)
ON DELETE NO ACTION;



ALTER TABLE forecast_section
ADD CONSTRAINT forsec_start_road_section_fk FOREIGN KEY (start_road_section_id)
REFERENCES road_section (id)
ON DELETE NO ACTION;



ALTER TABLE forecast_section_coordinates
ADD CONSTRAINT foresec_coord_foresec_fk FOREIGN KEY (forecast_section_id)
REFERENCES forecast_section (id)
ON DELETE NO ACTION;



ALTER TABLE forecast_section_weather
ADD CONSTRAINT foresec_weather_foresec_fk FOREIGN KEY (forecast_section_id)
REFERENCES forecast_section (id)
ON DELETE NO ACTION;



ALTER TABLE lam_station
ADD CONSTRAINT lam_station_road_district_fk FOREIGN KEY (road_district_id)
REFERENCES road_district (id)
ON DELETE NO ACTION;



ALTER TABLE lam_station
ADD CONSTRAINT lam_station_road_station_fk FOREIGN KEY (road_station_id)
REFERENCES road_station (id)
ON DELETE NO ACTION;



ALTER TABLE lam_station_data
ADD CONSTRAINT lam_station_data_fk FOREIGN KEY (lam_station_id)
REFERENCES lam_station (id)
ON DELETE NO ACTION;


ALTER TABLE location
ADD CONSTRAINT loc_area_fk FOREIGN KEY (version, area_ref)
REFERENCES location (version, location_code)
ON DELETE NO ACTION;



ALTER TABLE location
ADD CONSTRAINT loc_linear_fk FOREIGN KEY (version, linear_ref)
REFERENCES location (version, location_code)
ON DELETE NO ACTION;



ALTER TABLE location
ADD CONSTRAINT loc_lst_fk FOREIGN KEY (version, subtype_code)
REFERENCES location_subtype (version, subtype_code)
ON DELETE NO ACTION;



ALTER TABLE road_section
ADD CONSTRAINT road_section_road_district_fk FOREIGN KEY (road_district_id)
REFERENCES road_district (id)
ON DELETE NO ACTION;



ALTER TABLE road_section
ADD CONSTRAINT road_section_road_fk FOREIGN KEY (road_id)
REFERENCES road (id)
ON DELETE NO ACTION;



ALTER TABLE road_station
ADD CONSTRAINT road_station_road_address_fk FOREIGN KEY (road_address_id)
REFERENCES road_address (id)
ON DELETE NO ACTION;



ALTER TABLE road_station
ADD CONSTRAINT road_station_type_fk FOREIGN KEY (road_station_type)
REFERENCES road_station_type (road_station_type)
ON DELETE NO ACTION;



ALTER TABLE road_station_sensor
ADD CONSTRAINT rs_sensor_rs_type_fk FOREIGN KEY (road_station_type)
REFERENCES road_station_type (road_station_type)
ON DELETE NO ACTION;



ALTER TABLE road_station_sensors
ADD CONSTRAINT road_station_sensors_rs_fk FOREIGN KEY (road_station_id)
REFERENCES road_station (id)
ON DELETE NO ACTION;



ALTER TABLE road_station_sensors
ADD CONSTRAINT road_station_sensors_rss_fk FOREIGN KEY (road_station_sensor_id)
REFERENCES road_station_sensor (id)
ON DELETE NO ACTION;



ALTER TABLE sensor_value
ADD CONSTRAINT sensor_value_fk FOREIGN KEY (road_station_sensor_id)
REFERENCES road_station_sensor (id)
ON DELETE NO ACTION;



ALTER TABLE sensor_value
ADD CONSTRAINT sensor_value_road_station_fk FOREIGN KEY (road_station_id)
REFERENCES road_station (id)
ON DELETE NO ACTION;



ALTER TABLE sensor_value_description
ADD CONSTRAINT svd_sensor_fk FOREIGN KEY (sensor_id)
REFERENCES road_station_sensor (id)
ON DELETE NO ACTION;



ALTER TABLE site
ADD CONSTRAINT site_road_section_fk FOREIGN KEY (road_section_id)
REFERENCES road_section (id)
ON DELETE NO ACTION;



ALTER TABLE situation_record_comment_i18n
ADD CONSTRAINT situation_record_comment_fk FOREIGN KEY (datex2_situation_record_id)
REFERENCES datex2_situation_record (id)
ON DELETE NO ACTION;



ALTER TABLE speed_limit_season_history
ADD CONSTRAINT speed_limit_season_history_fk FOREIGN KEY (road_district_id)
REFERENCES road_district (id)
ON DELETE NO ACTION;



ALTER TABLE weather_station
ADD CONSTRAINT rws_road_station_fk FOREIGN KEY (road_station_id)
REFERENCES road_station (id)
ON DELETE NO ACTION;



-- ------------ Write CREATE-FUNCTION-stage scripts -----------

CREATE OR REPLACE FUNCTION date_to_seconds_from_daystart(IN d TIMESTAMP WITHOUT TIME ZONE)
RETURNS DOUBLE PRECISION
AS
$BODY$
DECLARE
    seconds DOUBLE PRECISION;
BEGIN
    seconds := TO_CHAR(d, 'SSSSS')::NUMERIC;
    RETURN seconds;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION floor_minutes(IN d TIMESTAMP WITHOUT TIME ZONE, IN resolution DOUBLE PRECISION)
RETURNS TIMESTAMP WITHOUT TIME ZONE
AS
$BODY$
DECLARE
    ret TIMESTAMP(0) WITHOUT TIME ZONE;
BEGIN
    ret := (aws_oracle_ext.TRUNC(d, 'HH24') + ((TO_CHAR(d, 'mi')::NUMERIC - MOD(TO_CHAR(d, 'mi'), resolution)) / (24 * 60)::NUMERIC || ' days')::INTERVAL);
    RETURN ret;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION map_to_normal_value_date(IN sourcedate TIMESTAMP WITHOUT TIME ZONE)
RETURNS TIMESTAMP WITHOUT TIME ZONE
AS
$BODY$
DECLARE
    ret TIMESTAMP(0) WITHOUT TIME ZONE;
BEGIN
    ret := (TO_DATE('2007/01/01/00:00', 'yyyy/mm/dd/HH24:MI') + ((MOD(TO_CHAR(sourcedate, 'J')::NUMERIC, 7) + 1)::NUMERIC || ' days')::INTERVAL - (1::NUMERIC || ' days')::INTERVAL + (aws_oracle_ext.TRUNC(sourcedate, 'MI') - aws_oracle_ext.TRUNC(sourcedate)));
    RETURN ret;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION rowgenerator(IN start_num DOUBLE PRECISION, IN end_num DOUBLE PRECISION)
RETURNS SETOF numbertable_type
AS
$BODY$
BEGIN
    FOR i IN start_num..end_num LOOP
        RETURN NEXT ARRAY[i];
    END LOOP;
    RETURN;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION f_trigger_vc$camera_preset()
RETURNS trigger
AS
$BODY$
BEGIN
    NEW.publishable :=
    CASE
        WHEN (NEW.obsolete_date IS NULL AND NEW.lotju_id IS NOT NULL AND NEW.camera_lotju_id IS NOT NULL AND NEW.public_internal = true AND
        NEW.public_external = true) THEN true
        ELSE false
    END;
    RETURN NEW;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION f_trigger_vc$forecast_section()
RETURNS trigger
AS
$BODY$
BEGIN
    NEW.road_number := SUBSTR(NEW.natural_id, 1, 5);
    NEW.road_section_number := SUBSTR(NEW.natural_id, 7, 3);
    NEW.road_section_version_number := SUBSTR(NEW.natural_id, 11, 3);
    RETURN NEW;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION f_trigger_vc$forecast_section_weather()
RETURNS trigger
AS
$BODY$
BEGIN
    NEW.type :=
    CASE NEW.forecast_name
        WHEN '0h' THEN 'OBSERVATION'
        ELSE 'FORECAST'
    END;
    RETURN NEW;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION trg_camera_preset_delete$camera_preset()
RETURNS trigger
AS
$BODY$
BEGIN
    RAISE USING hint = -20100, message = 'You can not delete camera_preset', detail = 'User-defined exception';
    RETURN NULL;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION trg_lam_station_delete$lam_station()
RETURNS trigger
AS
$BODY$
BEGIN
    RAISE USING hint = -20100, message = 'You can not delete lam_station', detail = 'User-defined exception';
    RETURN NULL;
END;
$BODY$
LANGUAGE  plpgsql;



CREATE OR REPLACE FUNCTION trg_weather_station_delete$weather_station()
RETURNS trigger
AS
$BODY$
BEGIN
    RAISE USING hint = -20100, message = 'You can not delete weather_station', detail = 'User-defined exception';
    RETURN NULL;
END;
$BODY$
LANGUAGE  plpgsql;



-- ------------ Write CREATE-TRIGGER-stage scripts -----------

CREATE TRIGGER trg_camera_preset_delete
BEFORE DELETE
ON camera_preset
FOR EACH STATEMENT
EXECUTE PROCEDURE trg_camera_preset_delete$camera_preset();



CREATE TRIGGER trigger_vc$camera_preset
BEFORE INSERT OR UPDATE
ON camera_preset
FOR EACH ROW
EXECUTE PROCEDURE f_trigger_vc$camera_preset();



CREATE TRIGGER trigger_vc$forecast_section
BEFORE INSERT OR UPDATE
ON forecast_section
FOR EACH ROW
EXECUTE PROCEDURE f_trigger_vc$forecast_section();



CREATE TRIGGER trigger_vc$forecast_section_weather
BEFORE INSERT OR UPDATE
ON forecast_section_weather
FOR EACH ROW
EXECUTE PROCEDURE f_trigger_vc$forecast_section_weather();



CREATE TRIGGER trg_lam_station_delete
BEFORE DELETE
ON lam_station
FOR EACH STATEMENT
EXECUTE PROCEDURE trg_lam_station_delete$lam_station();



CREATE TRIGGER trg_weather_station_delete
BEFORE DELETE
ON weather_station
FOR EACH STATEMENT
EXECUTE PROCEDURE trg_weather_station_delete$weather_station();



CREATE TABLE IF NOT EXISTS allowed_road_station_sensor(
id NUMERIC(10,0) NOT NULL,
natural_id numeric(10,0) NOT NULL,
road_station_type CHARACTER VARYING(200) NOT NULL
)
WITH (
OIDS=FALSE
);



-- ------------ Write CREATE-INDEX-stage scripts -----------

CREATE UNIQUE INDEX allowed_road_station_sensor_ui
ON allowed_road_station_sensor
USING BTREE (natural_id ASC, road_station_type ASC);



CREATE INDEX allowed_rs_sensor_type_fk_i
ON allowed_road_station_sensor
USING BTREE (road_station_type ASC);



-- ------------ Write CREATE-CONSTRAINT-stage scripts -----------

ALTER TABLE allowed_road_station_sensor
ADD CONSTRAINT allowed_road_station_sensor_pk PRIMARY KEY (id);



-- ------------ Write CREATE-FOREIGN-KEY-CONSTRAINT-stage scripts -----------

ALTER TABLE allowed_road_station_sensor
ADD CONSTRAINT allowed_rs_sensor_type_fk FOREIGN KEY (road_station_type)
REFERENCES road_station_type (road_station_type)
ON DELETE NO ACTION;