ALTER TABLE maintenance_tracking_data
ADD COLUMN IF NOT EXISTS sending_time TIMESTAMP(3) WITH TIME ZONE;

update maintenance_tracking_data
SET sending_time = '2000-01-01T00:00:00Z'
WHERE sending_time IS NULL;

ALTER TABLE maintenance_tracking_data
    ALTER COLUMN sending_time SET NOT NULL;

drop index if exists maintenance_tracking_data_sending_time_i;
create index maintenance_tracking_data_sending_time_i on maintenance_tracking_data USING BTREE (sending_time);
