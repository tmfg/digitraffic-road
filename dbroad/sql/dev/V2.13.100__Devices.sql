insert into device(id, type, road_address, etrs_tm35fin_x, etrs_tm35fin_y) values ('id1', 'speed sign', 'address', 10.2, 11.2);
insert into device(id, type, road_address, etrs_tm35fin_x, etrs_tm35fin_y) values ('id2', 'text sign', 'address', 11.2, 12.2);

insert into device_data(device_id, display_value, additional_information, effect_date, cause) values('id1', '80', null, current_date, null);
insert into device_data(device_id, display_value, additional_information, effect_date, cause) values('id2', 'warning!', null, current_date, null);