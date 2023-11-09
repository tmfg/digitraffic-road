ALTER table sensor_value rename column updated TO modified;
ALTER table sensor_value ALTER column modified SET DEFAULT now();
ALTER table sensor_value ALTER column modified SET NOT NULL;
ALTER table sensor_value
  ADD COLUMN IF NOT EXISTS created  TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW();
