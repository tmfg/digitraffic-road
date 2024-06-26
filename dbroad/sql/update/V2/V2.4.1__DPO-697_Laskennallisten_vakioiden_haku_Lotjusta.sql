CREATE TABLE TMS_SENSOR_CONSTANT
(
  LOTJU_ID            BIGINT,
  ROAD_STATION_ID     BIGINT NOT NULL,
  NAME                TEXT NOT NULL,
  UPDATED             TIMESTAMP(0) WITH TIME ZONE NOT NULL,
  OBSOLETE_DATE       TIMESTAMP(0) WITH TIME ZONE,
  CONSTRAINT TMS_SENSOR_CONSTANT_PKEY PRIMARY KEY (LOTJU_ID),
  CONSTRAINT U_TMS_SENSOR_CONSTANT UNIQUE(ROAD_STATION_ID, NAME)
);

CREATE TABLE TMS_SENSOR_CONSTANT_VALUE
(
  LOTJU_ID                  BIGINT,
  SENSOR_CONSTANT_LOTJU_ID  BIGINT NOT NULL REFERENCES TMS_SENSOR_CONSTANT (LOTJU_ID),
  VALUE                     INT NOT NULL,
  VALID_FROM                INT NOT NULL,
  VALID_TO                  INT NOT NULL,
  UPDATED                   TIMESTAMP(0) WITH TIME ZONE NOT NULL,
  OBSOLETE_DATE             TIMESTAMP(0) WITH TIME ZONE,
  CONSTRAINT TMS_SENSOR_CONSTANT_VALUE_PKEY PRIMARY KEY (LOTJU_ID)
);

CREATE INDEX TMS_SENSOR_CONSTANT_VALUE_FK_I ON TMS_SENSOR_CONSTANT_VALUE
USING BTREE (SENSOR_CONSTANT_LOTJU_ID ASC);


CREATE TABLE ALLOVED_TMS_SENSOR_CONSTANT
(
  NAME                      TEXT PRIMARY KEY
);

insert into ALLOVED_TMS_SENSOR_CONSTANT (NAME) values ('Tien_suunta');
insert into ALLOVED_TMS_SENSOR_CONSTANT (NAME) values ('VVAPAAS1');
insert into ALLOVED_TMS_SENSOR_CONSTANT (NAME) values ('VVAPAAS2');
insert into ALLOVED_TMS_SENSOR_CONSTANT (NAME) values ('MS1');
insert into ALLOVED_TMS_SENSOR_CONSTANT (NAME) values ('MS2');


-- Remove TIEN_SUUNTA
DELETE FROM allowed_road_station_sensor
WHERE natural_id = 6003;

