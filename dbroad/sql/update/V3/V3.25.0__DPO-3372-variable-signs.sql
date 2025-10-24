alter table device_data_datex2 add column version text;
update device_data_datex2 set version = 'DATEXII_2_2_3';
alter table device_data_datex2 alter column version set not null;

alter table device_data_datex2 add column type text;
update device_data_datex2 set type = 'SITUATION';
alter table device_data_datex2 alter column type set not null;

alter table device_data_datex2 drop constraint device_data_datex2_pkey;
alter table device_data_datex2 add constraint device_data_datex2_pkey unique (device_id, version, type);
