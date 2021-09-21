create table cs_domain (
	name 			text 	primary key,
	description 	text,
	added_date		timestamp with time zone,
	removed_date	timestamp with time zone null
);

create table cs_user_type (
	id				smallint	primary key,
	name			text
);

create table counting_site (
	id				bigint 	primary key,
	site_id			integer,
	domain_name		text,
	site_domain		text,
	location		geography(point),
	user_type		smallint,
	interval		smallint,
	direction		smallint,
	added_date		timestamp with time zone,
	removed_date	timestamp with time zone null
);

alter table counting_site add constraint counting_site_domain_fkey foreign key (domain_name) references cs_domain(name);
alter table counting_site add constraint counting_site_user_type_fkey foreign key (user_type) references cs_user_type(id);

create index counting_site_domain_fk on counting_site(domain_name);
create index counting_site_user_type_fk on counting_site(user_type);

create table cs_data (
	id					bigint	primary key,
	counting_site_id	bigint,
	data_timestamp		timestamp with time zone,
	count				integer null,
	status				smallint null
);

alter table cs_data add constraint cs_data_cs_fkey foreign key (counting_site_id) references counting_site(id);

create index cs_data_cs_fk on cs_data(counting_site_id);