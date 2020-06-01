create table device_data_datex2 (
	id bigint primary key,
	datex2 text not null,
	device_id text not null,
	effect_date timestamptz not null
);

ALTER TABLE device_data_datex2 ADD CONSTRAINT ddd_device_fk FOREIGN KEY (device_id) REFERENCES device (id);
create index ddd_device_fk_i on device_data_datex2(device_id);
create index ddd_effect_date_i on device_data_datex2(effect_date);
