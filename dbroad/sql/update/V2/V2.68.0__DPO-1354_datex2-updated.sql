-- add new column to track when datex2 was updated
alter table device_data_datex2 add column if not exists updated_timestamp timestamp with time zone;

-- delete removed datex2-messages
delete from device_data_datex2 where device_id like '%_POISTETTU';
