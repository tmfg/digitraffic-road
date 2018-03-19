--select 'insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) ' ||
-- 'values(nextval(''seq_sensor_value''),' || value || ',''' || to_char(measured, 'yyyy-mm-dd hh24:mm:ss') || ''',' ||
--road_station_sensor_id || ',' || road_station_id || ',' || case when updated is null then 'null' else '''' || to_char(updated,
--'yyyy-mm-dd hh24:mm:ss') || '''' end || ',' || case when time_window_start is null then 'null' else '''' || to_char(time_window_start,
--'yyyy-mm-dd hh24:mm:ss') || '''' end
--|| ',' || case when time_window_end is null then 'null' else '''' || to_char(time_window_end, 'yyyy-mm-dd hh24:mm:ss') || '''' end
--|| ');'
--from sensor_value where road_station_id = 371;

insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),0,'2015-08-26 07:08:00',2,371,null,null,null);
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),225,'2018-03-19 12:03:59',201,371,'2018-03-19 12:03:23','2018-03-19 11:03:00','2018-03-19 12:03:00');
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),187,'2018-03-19 12:03:59',202,371,'2018-03-19 12:03:23','2018-03-19 11:03:00','2018-03-19 12:03:00');
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),83,'2018-03-19 12:03:59',203,371,'2018-03-19 12:03:23','2018-03-19 11:03:00','2018-03-19 12:03:00');
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),85,'2018-03-19 12:03:59',204,371,'2018-03-19 12:03:23','2018-03-19 11:03:00','2018-03-19 12:03:00');
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),16,'2018-03-19 12:03:59',207,371,'2018-03-19 12:03:23','2018-03-19 11:03:00','2018-03-19 12:03:00');
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),13,'2018-03-19 12:03:59',216,371,'2018-03-19 12:03:23','2018-03-19 11:03:00','2018-03-19 12:03:00');
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),228,'2018-03-19 12:03:57',61,371,'2018-03-19 12:03:23',null,null);
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),204,'2018-03-19 12:03:57',62,371,'2018-03-19 12:03:23',null,null);
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),84,'2018-03-19 12:03:57',63,371,'2018-03-19 12:03:23',null,null);
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),89,'2018-03-19 12:03:57',64,371,'2018-03-19 12:03:23',null,null);
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),99,'2018-03-19 12:03:57',240,371,'2018-03-19 12:03:23',null,null);
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),104,'2018-03-19 12:03:57',241,371,'2018-03-19 12:03:23',null,null);
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),16,'2018-03-19 12:03:57',218,371,'2018-03-19 12:03:23',null,null);
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),15,'2018-03-19 12:03:57',242,371,'2018-03-19 12:03:23',null,null);
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),101,'2018-03-19 12:03:50',205,371,'2018-03-19 12:03:24','2018-03-19 12:03:00','2018-03-19 12:03:00');
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),97,'2018-03-19 12:03:50',209,371,'2018-03-19 12:03:24','2018-03-19 12:03:00','2018-03-19 12:03:00');
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),18,'2018-03-19 12:03:50',206,371,'2018-03-19 12:03:24','2018-03-19 12:03:00','2018-03-19 12:03:00');
insert into sensor_value(id,value,measured,road_station_sensor_id,road_station_id,updated,time_window_start,time_window_end) values(nextval('seq_sensor_value'),19,'2018-03-19 12:03:50',208,371,'2018-03-19 12:03:24','2018-03-19 12:03:00','2018-03-19 12:03:00');