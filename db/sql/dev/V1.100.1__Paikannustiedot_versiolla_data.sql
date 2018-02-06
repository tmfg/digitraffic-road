insert into location_version(version, updated) values ('1.0', current_timestamp - - interval '1 days');
insert into location_version(version, updated) values ('1.1', current_timestamp);

insert into location_type(version, type_code, description_en, description_fi) values ('1.1', 'L1.0', 'Road', 'Tie');
insert into location_type(version, type_code, description_en, description_fi) values ('1.1', 'L2.0', 'Ring road', 'Kehätie');
insert into location_type(version, type_code, description_en, description_fi) values ('1.1', 'P1.0', 'Junction', 'Liittymä');
insert into location_type(version, type_code, description_en, description_fi) values ('1.1', 'A1.0', 'Continent', 'Maanosa');
insert into location_type(version, type_code, description_en, description_fi) values ('1.1', 'A2.0', 'Country group', 'Maaryhmä');
insert into location_type(version, type_code, description_en, description_fi) values ('1.1', 'A3.0', 'Country', 'Maa');
insert into location_type(version, type_code, description_en, description_fi) values ('1.1', 'A7.0', '1st order area', 'AVI-alue');
insert into location_type(version, type_code, description_en, description_fi) values ('1.0', 'A1.0', 'Continent', 'Maanosa');

insert into location_subtype(version, subtype_code, description_en, description_fi) values ('1.1', 'L1.1', 'Motorway', 'Moottoritie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values ('1.1', 'L1.2', '1st class road', 'Valta- tai kantatie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values ('1.1', 'L1.3', '2nd class road', 'Seututie');
insert into location_subtype(version, subtype_code, description_en, description_fi) values ('1.1', 'A1.0', 'Continent', 'Maanosa');
insert into location_subtype(version, subtype_code, description_en, description_fi) values ('1.1', 'A2.0', 'Country group', 'Maaryhmä');
insert into location_subtype(version, subtype_code, description_en, description_fi) values ('1.1', 'A3.0', 'Country', 'Maa');
insert into location_subtype(version, subtype_code, description_en, description_fi) values ('1.1', 'A7.0', '1st order area', 'Lääni');
insert into location_subtype(version, subtype_code, description_en, description_fi) values ('1.0', 'A1.0', 'Continent', 'Maanosa');

insert into location(version, location_code, subtype_code, road_name, first_name, second_name, area_ref, linear_ref) values ('1.1', 1, 'A1.0', null, 'Eurooppa', null, null, null);
insert into location(version, location_code, subtype_code, road_name, first_name, second_name, area_ref, linear_ref) values ('1.1', 12187, 'A2.0', null, 'Pohjoismaat', null, 1, null);
insert into location(version, location_code, subtype_code, road_name, first_name, second_name, area_ref, linear_ref) values ('1.1', 3, 'A3.0', null, 'Suomi', null, 12187, null);
insert into location(version, location_code, subtype_code, road_name, first_name, second_name, area_ref, linear_ref) values ('1.1', 5, 'A7.0', null, 'Etelä-Suomi', null, 3, null);
insert into location(version, location_code, subtype_code, road_name, first_name, second_name, area_ref, linear_ref) values ('1.0', 1, 'A1.0', null, 'Eurooppa', null, null, null);
