insert into device(id, updated_date, type, road_address, etrs_tm35fin_x, etrs_tm35fin_y) values ('id1', current_timestamp, 'speed sign',
'address', 10.2, 11.2);
insert into device(id, updated_date, type, road_address, etrs_tm35fin_x, etrs_tm35fin_y) values ('id2', current_timestamp, 'text sign',
'address', 11.2, 12.2);

insert into device_data(device_id, created_date, display_value, additional_information, effect_date, cause) values('id1',
current_timestamp, '80', null, current_timestamp, null);
insert into device_data(device_id, created_date, display_value, additional_information, effect_date, cause) values('id2',
current_timestamp, 'warning!', null, current_timestamp, null);