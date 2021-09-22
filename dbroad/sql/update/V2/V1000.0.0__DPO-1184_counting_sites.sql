create table counting_site_domain (
	name 			text 	primary key,
	description 	text,
	added_date		timestamp(0) with time zone,
	removed_date	timestamp(0) with time zone null
);

create table counting_site_user_type (
	id				smallint	primary key,
	name			text
);

create table counting_site_counter (
	id				bigint 	    primary key,
	site_id			integer,
	domain_name		text,
	site_domain		text,
	location		geography(point),
	user_type		smallint,
	interval		smallint,
	direction		smallint,
	added_date		timestamp(0) with time zone,
	removed_date	timestamp(0) with time zone null
);

alter table counting_site_counter add constraint counting_site_counter_domain_fkey foreign key (domain_name) references counting_site_domain(name);
alter table counting_site_counter add constraint counting_site_counter_user_type_fkey foreign key (user_type) references counting_site_user_type(id);
alter table counting_site_counter add constraint counting_site_domain_site_key unique (domain_name, site_id);

create sequence counting_site_counter_id_seq;

create index counting_site_counter_domain_fk on counting_site_counter(domain_name);
create index counting_site_counter_user_type_fk on counting_site_counter(user_type);

create table counting_site_data (
	id					bigint	primary key,
	counter_id			bigint,
	data_timestamp		timestamp(0) with time zone,
	count				integer null,
	status				smallint null
);

alter table counting_site_data add constraint counting_site_data_counter_fkey foreign key (counter_id) references counting_site_counter(id);

create sequence counting_site_data_id_seq;

create index counting_site_data_site_fki on counting_site_counter(site_id);

insert into counting_site_user_type(id, name)
    values
    	(1, 'pedestrian'),
	    (2, 'bicycle'),
	    (3, 'horse'),
	    (4, 'car'),
	    (5, 'bus'),
	    (6, 'minibus'),
	    (7, 'undefined'),
	    (8, 'motorcycle'),
	    (9, 'kayak'),
	    (13, 'e-scooter'),
	    (14, 'truck');
