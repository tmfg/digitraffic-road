create table counting_site_domain (
	name 			text 	primary key,
	description 	text    not null,
	added_timestamp		timestamp(0) with time zone not null,
	removed_timestamp	timestamp(0) with time zone
);

create table counting_site_user_type (
	id				smallint	primary key,
	name			text        not null
);

create table counting_site_counter (
	id			    	bigint 	    primary key,
	site_id			    integer     not null,
	domain_name	    	text        not null,
	site_domain 		text        not null,
	name                text        not null,
	location		    geography(point)    not null,
    user_type_id    	smallint    not null,
	interval		    smallint    not null,
	direction		    smallint    not null,
	added_timestamp	    timestamp(0) with time zone not null,
    last_data_timestamp timestamp(0) with time zone,
	removed_timestamp	timestamp(0) with time zone
);

alter table counting_site_counter add constraint counting_site_counter_domain_fkey foreign key (domain_name) references counting_site_domain(name);
alter table counting_site_counter add constraint counting_site_counter_user_type_fkey foreign key (user_type_id) references counting_site_user_type(id);
alter table counting_site_counter add constraint counting_site_domain_site_key unique (domain_name, site_id);

create sequence counting_site_counter_id_seq;

create index counting_site_counter_domain_fki on counting_site_counter(domain_name);
create index counting_site_counter_user_type_fki on counting_site_counter(user_type_id);

create table counting_site_data (
	id					bigint	primary key,
	counter_id			bigint  not null,
	interval            smallint    not null,
	data_timestamp		timestamp(0) with time zone not null,
	count				integer,
	status				smallint
);

alter table counting_site_data add constraint counting_site_data_counter_fkey foreign key (counter_id) references counting_site_counter(id);

create sequence counting_site_data_id_seq;

create index counting_site_data_counter_fki on counting_site_data(counter_id);

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
