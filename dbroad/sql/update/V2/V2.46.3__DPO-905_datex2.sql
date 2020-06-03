drop table device_data_datex2;

create table device_data_datex2 (
	device_id 	text primary key,
	datex2 		text not null,
	effect_date timestamptz not null
);

drop sequence seq_device_data_datex2;
