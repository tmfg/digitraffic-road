CREATE INDEX tms_sensor_constant_modified_i
  ON tms_sensor_constant
  USING BTREE (modified ASC);

CREATE INDEX tms_sensor_constant_value_modified_i
  ON tms_sensor_constant_value
  USING BTREE (modified ASC);