alter table device_data add column reliability text;

update device_data set reliability='NORMAALI';

alter table device_data alter column reliability SET NOT NULL;

-- also drop foreign key
alter table device_data drop constraint device_data_device_id_fkey;