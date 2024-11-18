create table if not exists cs2_site (
	id 					numeric primary key,
	name    			text not null,
	domain				text not null,
	description			text,
	custom_id			text,
	latitude			numeric(8,6) not null,
	longitude			numeric(8,6) not null,
	granularity			text not null,
	directional			boolean not null,
	travel_modes		text[] not null,
	removed_timestamp	timestamp with time zone,
	last_data_timestamp timestamp with time zone,
	created				timestamp with time zone default current_timestamp not null,
	modified			timestamp with time zone default current_timestamp not null
);

create table if not exists cs2_data (
	site_id			numeric not null,
	travel_mode		text not null,
	direction		text not null,
	data_timestamp	timestamp with time zone not null,
	granularity		text not null,
	counts			numeric not null,
	created			timestamp with time zone default current_timestamp not null,
	modified		timestamp with time zone default current_timestamp not null
);

create index if not exists cs2_data_fetch_i on cs2_data(site_id, data_timestamp, travel_mode);
create index if not exists cs2_data_fetch2_i on cs2_data(data_timestamp, site_id, travel_mode);
