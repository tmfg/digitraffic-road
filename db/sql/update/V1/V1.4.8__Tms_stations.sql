-- select 'insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, ' ||
-- 'winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, ' ||
-- 'calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
-- direction_2_municipality_code)
--values(' || id || ',' || natural_id || ',''' || name || ''',' || case when obsolete = 0 then 'false' else 'true' end || ',' || case
-- when obsolete_date is null then 'null' else '''' || to_char(obsolete_date, 'yyyy-mm-dd hh24:mm:ss') || '''' end || ',' ||
--summer_free_flow_speed_1 || ',' || summer_free_flow_speed_2 || ',' || winter_free_flow_speed_1 || ',' || winter_free_flow_speed_2 || ','
-- || case when road_district_id is null then 'null' else '' || road_district_id end || ',' || road_station_id || ',' || lotju_id || ','''
-- || lam_station_type || ''',' || case when calculator_device_type is null then 'null' else '''' || calculator_device_type || '''' end ||
-- ',' || case when direction_1_municipality is null then 'null' else '''' || direction_1_municipality || '''' end || ',' || case when
--direction_1_municipality_code is null then 'null' else '' || direction_1_municipality_code end || ',' || case when
--direction_2_municipality is null then 'null' else '''' || direction_2_municipality || '''' end || ',' || case when
--direction_2_municipality_code is null then 'null' else '' || direction_2_municipality_code end || ');' from lam_station;

insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2906,172,'vt7_Helsinki_Bäcknäs',false,null,0,0,0,0,100,6111,641,'DSL_4',null,'Porvoo',638,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2907,198,'vt7_Porvoo',false,null,0,0,0,0,100,6112,661,'DSL_4',null,'Lappeenranta',405,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2908,6,'st102_Hiidenkallio',false,null,0,0,0,0,100,6113,742,'DSL_4',null,'Karamalmi',555,'Matinkylä',556);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2909,1466,'vt4_Syväsenvaara',false,null,0,0,0,0,108,6114,882,'DSL_4',null,'Sodankylä',758,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2910,504,'vt7_Vaalimaa_rekkaparkki1',false,null,0,0,0,0,103,6115,862,'DSL_6',null,'Vaalimaa',1000,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2911,505,'vt7_Vaalimaa_rekkaparkki2',false,null,0,0,0,0,103,6116,863,'DSL_6',null,'Vaalimaa',1000,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2920,506,'vt7_Hamina_Lelu',false,null,0,0,0,0,103,6361,922,'DSL_6',null,'Vaalimaa',1000,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(163,585,'OLD_vt7_Karhula',true,'2016-11-08 00:11:00',91,92,86,87,103,63,253,'DSL_4',null,'Hamina',75,'Karhula',1019);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2960,98,'kt51_Espoo_Piispansilta',false,null,0,0,0,0,100,6483,942,'DSL_4',null,'Kirkkonummi',257,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2980,161,'OLD_vt1_Sysimetsä',true,'2017-06-06 00:06:00',0,0,0,0,null,6583,62,'DSL_4',null,'Helsinki',91,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3000,307,'vt7_HaVa_LAM038601',false,null,0,0,0,0,100,6663,962,'DSL_6',null,'Vaalimaa',1000,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3001,308,'vt7_HaVa_LAM038501',false,null,0,0,0,0,100,6664,963,'DSL_6',null,'Vaalimaa',1000,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3002,310,'vt7_HaVa_LAM038701',false,null,0,0,0,0,100,6665,964,'DSL_6',null,'Vaalimaa',1000,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3003,309,'vt7_HaVa_LAM038801',false,null,0,0,0,0,100,6666,982,'DSL_6',null,'Vaalimaa',1000,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3020,937,'kt77_Viitasaari_Taimoniemi',false,null,0,0,0,0,105,6703,1002,'DSL_6',null,'Siilinjärvi',749,'Viitasaari',931);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3040,507,'OLD_vt6_vahinko_Luumäki',true,'2017-09-19 00:09:00',0,0,0,0,103,6763,1022,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3060,9,'kt45_Vantaa_Ilola',false,null,0,0,0,0,100,6823,1063,'DSL_6',null,'Hyvinkää',106,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3061,10,'st101_Espoo_Laajalahti',false,null,0,0,0,0,100,6824,1064,'DSL_6',null,'Itäkeskus',2003,'Tapiola',2002);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3062,11,'st101_Helsinki_Vartiokylä',false,null,0,0,0,0,100,6825,1065,'DSL_6',null,'Itäkeskus',2003,'Tapiola',2002);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(482,199,'OLD_Olari',true,'2016-12-09 00:12:00',50,50,50,50,null,363,91,'DSL_4',null,'Kirkkonummi',257,null,1015);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(479,192,'vt1_Veikkola',false,null,0,0,0,0,100,360,85,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(124,238,'vt1_Kaarina_Kurkela_itä',false,null,102,99,99,96,101,25,119,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(183,629,'vt23_Siikamäki',false,null,93,94,85,87,104,83,280,'DSL_4',null,'Varkaus',915,'Pieksämäen',594);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(621,732,'vt6_Kousa',false,null,100,100,80,80,104,507,297,'DSL_4',null,'Joensuu',167,'Imatra',153);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(907,557,'vt6_Lavola',false,null,0,0,0,0,103,1628,231,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(341,1235,'vt8_Lapinkangas',false,null,93,94,87,86,107,227,423,'DSL_4',null,'Oulu',564,'Raahe',678);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(347,1241,'vt22_Maikkula',false,null,78,77,74,75,107,233,429,'DSL_4',null,'Kajaani',205,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(337,1231,'vt4_Kalimenoja',false,null,87,87,100,96,107,224,419,'DSL_4',null,'Kemi',240,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1040,935,'vt4_Toivakka2',false,null,0,0,0,0,105,1758,340,'DSL_4',null,'Jyväskylä',179,'Joutsa',172);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2940,257,'yt1851_Turku_Härkämäki',false,null,0,0,0,0,101,6443,137,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2941,304,'vt7_Hamina_Husula',false,null,0,0,0,0,103,6444,141,'DSL_4',null,'Vaalimaa',1000,'Kotka',285);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(246,725,'vt6_Juuka2',false,null,95,101,90,86,104,142,291,'DSL_4',null,'Nurmes',541,'Joensuu',167);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(252,803,'vt5_Siilinjärvi',false,null,82,79,81,79,104,147,301,'DSL_4',null,'Iisalmi',140,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(254,805,'kt75_Rautavaara',false,null,101,99,87,83,104,149,303,'DSL_4',null,'Nurmes',541,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(257,821,'vt9_Suonenjoki',false,null,96,92,88,86,104,151,306,'DSL_4',null,'Kuopio',297,'Jyväskylä',179);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(259,824,'kt75_Nilsiä',false,null,87,83,85,79,104,153,308,'DSL_4',null,'Nilsiä',534,'Siilinjärvi',749);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(263,828,'vt5_Rahusenlampi',false,null,100,100,98,98,104,157,312,'DSL_6',null,'Siilinjärvi',749,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(132,247,'vt1_Salo_Isokylä_länsi',false,null,90,100,0,0,101,33,127,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(134,249,'vt8_Ihala',false,null,99,99,93,94,101,35,129,'DSL_4',null,'Rauma',684,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(137,252,'st185_Artukainen',false,null,81,81,75,75,101,38,132,'DSL_4',null,'Naantali',529,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(189,401,'vt3_Lempäälä_Sääksjärvi',false,null,104,100,95,98,102,89,143,'DSL_4',null,'Tampere',837,'Hämeenlinna',109);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(192,404,'vt9_Kangasala_Suinula',false,null,96,90,92,90,102,92,146,'DSL_4',null,'Orivesi',562,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(194,406,'vt11_Nokia_Linnavuori',false,null,96,92,89,85,102,94,148,'DSL_4',null,'Pori',609,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(198,421,'kt65_Ylöjärvi',false,null,86,84,86,83,102,98,152,'DSL_4',null,'Hämeenkyrö',108,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(202,425,'vt10_Tammela',false,null,92,92,86,87,100,102,156,'DSL_4',null,'Hämeenlinna',109,'Forssa',61);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(204,428,'kt54_Loppi',false,null,91,93,85,85,100,104,158,'DSL_4',null,'Loppi',433,'Forssa',61);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(206,430,'vt24_Asikkala',false,null,86,79,83,80,100,106,160,'DSL_4',null,'Asikkala',16,'Lahti',398);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(208,432,'vt3_Janakkala',false,null,110,110,99,98,100,108,162,'DSL_4',null,'Hämeenlinna',109,'Riihimäki',694);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(213,438,'vt12_Tre_Paasikiventie',false,null,71,69,69,72,102,112,167,'DSL_4',null,'Tampere',837,'Nokia',536);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(215,440,'vt3_Lempäälä_Lippo',false,null,111,112,101,102,102,114,169,'DSL_4',null,'Tampere',837,'Hämeenlinna',109);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(218,443,'Tre_Pispalan_valtatie',false,null,49,46,51,47,null,117,172,'DSL_4',null,'Tampere',837,'Nokia',536);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(223,448,'vt9_Längelmäki',false,null,100,102,97,97,102,121,177,'DSL_4',null,'Jämsä',182,'Orivesi',562);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(228,453,'Tampere_Mustalahti',false,null,50,47,52,50,null,126,182,'DSL_4',null,'Tampere',837,'Nokia',536);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(230,455,'kt65_Tre_Epilänharju',false,null,76,75,74,74,102,128,184,'DSL_4',null,'Ylöjärvi',980,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(235,460,'vt3_Pirkkala_Huovi',false,null,70,75,69,72,102,133,189,'DSL_4',null,'Nokia',536,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(141,503,'vt26_Saaramaa',false,null,86,88,86,87,103,42,198,'DSL_4',null,'Luumäki',441,'Hamina',75);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(145,524,'vt6_Rautjärvi',false,null,95,91,87,85,103,46,202,'DSL_4',null,'Parikkala',580,'Imatra',153);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(147,526,'vt13_Suomenniemi',false,null,92,97,85,88,104,48,204,'DSL_4',null,'Lappeenranta',405,'Mikkeli',491);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(152,531,'vt6_Vuoksenniska',false,null,102,102,100,100,103,52,209,'DSL_4',null,'Joensuu',167,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(154,533,'vt13_Karhusjärvi',false,null,85,83,82,82,103,54,211,'DSL_4',null,null,1007,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2820,578,'vt7_Ahvenkoski_Itä',false,null,0,0,0,0,103,4364,246,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(342,1236,'vt4_Ala-Temmes',false,null,81,81,81,81,107,228,424,'DSL_4',null,'Oulu',564,null,1016);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(345,1239,'st815_Lentokentäntie',false,null,77,76,80,79,107,231,427,'DSL_4',null,'Oulunsalo',567,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(352,1247,'st847_Oulunlahti',false,null,61,62,61,62,107,238,434,'DSL_4',null,'Oulu',564,'Kempele',244);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(354,1249,'st815_Lunki',false,null,60,63,61,61,107,240,436,'DSL_4',null,'Oulunsalo',567,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(356,1301,'vt5_Kajaani_Mineraali',false,null,76,84,74,78,107,242,442,'DSL_4',null,null,1005,'Kajaani',205);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(359,1321,'vt5_Hatulanmäki',false,null,95,94,74,73,107,245,445,'DSL_4',null,'Kajaani',205,'Iisalmi',140);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(364,1326,'vt28_Vuottolahti',false,null,96,97,79,79,107,249,450,'DSL_4',null,'Kajaani',205,'Pyhäntä',630);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(366,1328,'vt5_Tapiola',false,null,102,101,90,94,107,251,452,'DSL_4',null,'Kuusamo',305,'Kajaani',205);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(371,1404,'st921_Kaakamo',false,null,83,81,81,78,108,255,457,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(373,1421,'vt21_Kolari',false,null,100,96,95,90,108,257,459,'DSL_4',null,'Muonio',498,'Ylitornio',976);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(376,1424,'vt4_Olkkajärvi',false,null,92,94,86,86,108,259,462,'DSL_4',null,'Sodankylä',758,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(379,1427,'kt83_Raanujärvi',false,null,94,94,91,90,108,262,465,'DSL_4',null,'Rovaniemi',698,'Pello',854);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(383,1432,'kt98_Aavasaksa',false,null,50,42,52,46,108,265,469,'DSL_4',null,'Ruotsi',1010,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(386,1435,'st954_Muonio',false,null,32,29,31,32,108,268,472,'DSL_4',null,'Ruotsi',1010,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(389,1438,'vt4_mp2_Jokisuu',false,null,0,0,0,0,108,271,475,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(391,1440,'vt29_mp1c_Tornio',false,null,0,0,0,0,108,273,477,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(395,1444,'vt4_Utsjoki',false,null,32,30,33,28,108,276,481,'DSL_4',null,'Norja',1020,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(316,1103,'vt28_Sievi',false,null,94,99,87,91,107,205,397,'DSL_4',null,'Nivala',535,'Kannus',217);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(318,1105,'vt27_Haapajärvi',false,null,98,97,84,85,107,207,399,'DSL_4',null,'Nivala',535,'Kiuruvesi',263);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(314,1122,'vt13_Veteli',false,null,97,98,85,87,106,204,401,'DSL_4',null,'Kokkola',272,'Kyyjärvi',312);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(322,1201,'vt4_Oulu_Mäntylä',false,null,102,104,99,101,107,210,404,'DSL_4',null,'Oulu',564,'Kempele',244);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(325,1204,'vt8_Raahe',false,null,58,56,67,64,107,213,407,'DSL_4',null,'Kalajoki',208,'Liminka',425);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(327,1221,'vt4_Rantsila',false,null,95,95,88,86,107,215,409,'DSL_4',null,'Pulkkila',617,'Liminka',425);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(332,1226,'vt4_Kempele',false,null,104,102,101,96,107,219,414,'DSL_4',null,'Kempele',244,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(334,1228,'st847_Haukipudas',false,null,78,79,79,78,107,221,416,'DSL_4',null,'Haukipudas',84,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(338,1232,'vt20_Kettumäki',false,null,97,97,88,89,107,225,420,'DSL_4',null,'Kuusamo',305,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(269,902,'vt4_Äänekoski',false,null,90,91,85,84,105,162,322,'DSL_4',null,'Viitasaari',931,'Äänekoski',992);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(272,905,'vt23_Keuruu1',false,null,85,85,82,81,105,165,325,'DSL_4',null,'Jyväskylä',179,'Virrat',936);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(279,927,'vt9_Korpilahti',false,null,92,95,85,88,105,171,332,'DSL_4',null,'Jyväskylä',179,'Jämsä',182);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(281,929,'vt4_Hupeli',false,null,59,65,67,68,105,173,334,'DSL_4',null,'Jyväskylä',179,'Joutsa',172);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(283,931,'vt18_Petäjävesi',false,null,93,93,85,85,105,175,336,'DSL_4',null,'Jyväskylä',179,'Keuruu',249);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(286,1001,'vt3_Huissi',false,null,100,99,90,89,106,178,343,'DSL_4',null,'Laihia',399,'Kurikka',301);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(290,1005,'vt19_Luopajärvi',false,null,97,94,90,86,106,182,347,'DSL_4',null,'Seinäjoki',743,'Jalasjärvi',164);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(292,1007,'vt19_Lapua',false,null,91,91,83,83,106,184,349,'DSL_4',null,'Lapua',408,'Seinäjoki',743);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(294,1022,'vt8_Närpiö',false,null,98,95,94,92,106,375,351,'DSL_4',null,'Vaasa',905,'Pori',609);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(295,1023,'vt8_Kokkola',false,null,80,81,79,76,106,186,352,'DSL_4',null,'Kalajoki',208,'Kokkola',272);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(298,1027,'vt16_Alajärvi',false,null,93,95,85,88,106,189,355,'DSL_4',null,'Kyyjärvi',312,'Lapua',408);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(302,1031,'vt19_Alahärmä_Voltti',false,null,93,94,91,90,106,193,359,'DSL_4',null,'Uusikaarlepyy',893,'Lapua',408);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(304,1033,'vt18_Alavus_Tuuri',false,null,91,91,84,82,106,195,361,'DSL_4',null,'Töysä',863,'Alavus',10);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(305,1034,'vt67_Seinäjoki',false,null,79,77,79,79,106,196,362,'DSL_4',null,'Lapua',408,'Ilmajoki',145);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(308,1037,'vt18_Nurmo_Uittoo',false,null,91,92,84,86,106,199,365,'DSL_4',null,'Kuortane',300,'Seinäjoki',743);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(312,1041,'vt13_Kaustinen_Viiperi',false,null,98,99,91,92,106,202,369,'DSL_4',null,'Kaustinen',236,'Kokkola',272);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1739,1043,'yt17367_Kurikka_Panttil',false,null,0,0,0,0,106,3576,371,'DSL_4',null,'Kurikka',301,'Kurikka',301);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(760,1601,'vt1_Lakiamäki',false,null,120,120,100,100,101,1481,500,'E_18',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(763,1604,'vt1_Lahnajärvi',false,null,120,120,100,100,101,1484,503,'E_18',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(765,1606,'vt1_Pitkämäki',false,null,120,120,100,100,100,1486,505,'E_18',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2840,2,'OLD_vt7_Rita2',true,'2016-11-08 00:11:00',0,0,0,0,100,6045,2,'DSL_4',null,null,1004,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2841,119,'vt7_Helsinki_Itäsalmi',false,null,0,0,0,0,100,6046,20,'DSL_4',null,'Porvoo',638,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2842,120,'OLD_st101_Laajalahti',true,'2016-11-08 00:11:00',0,0,0,0,100,6047,21,'DSL_4',null,'Itäkeskus',1003,'Tapiola',1015);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2843,157,'OLD_vt7_Sarvilahti',true,'2016-11-08 00:11:00',0,0,0,0,100,6048,58,'DSL_4',null,'Loviisa',434,'Porvoo',638);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2844,166,'OLD_vt3_Perkiö',true,'2016-11-08 00:11:00',0,0,0,0,100,6049,67,'DSL_4',null,'Hämeenlinna',109,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2845,176,'vt7_Sipoonlahti',false,null,0,0,0,0,100,6050,72,'DSL_4',null,'Porvoo',638,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2846,177,'vt7_Sipoo_Kallback',false,null,0,0,0,0,100,6051,73,'DSL_4',null,'Porvoo',638,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2847,178,'vt7_Sipoo_Kulloo',false,null,0,0,0,0,100,6052,74,'DSL_4',null,'Porvoo',638,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2848,202,'OLD_st110_Piikkiö',true,'2016-11-08 00:11:00',0,0,0,0,101,6053,93,'DSL_4',null,'Turku',853,'Salo',734);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2849,132,'OLD_vt7_Koskenkylä',true,'2016-11-08 00:11:00',0,0,0,0,100,6054,33,'DSL_4',null,'Koskenkylä',1004,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2850,588,'OLD_vt6_Selkäharju',true,'2016-11-08 00:11:00',0,0,0,0,103,6055,256,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2851,589,'OLD_vt7_Ahvenkoski',true,'2016-11-08 00:11:00',0,0,0,0,103,6056,257,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2852,601,'OLD_vt4_Vierumäki',true,'2016-11-08 00:11:00',0,0,0,0,102,6057,268,'DSL_4',null,'Heinola',111,'Lahti',398);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2853,626,'OLD_st4557_Kuvansi',true,'2016-11-08 00:11:00',0,0,0,0,103,6058,277,'DSL_4',null,'Varkaus',915,'Joroinen',171);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2854,721,'OLD_vt6_Pyhäselkä',true,'2016-11-08 00:11:00',0,0,0,0,104,6059,287,'DSL_4',null,'Joensuu',167,'Kitee',260);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2855,722,'OLD_vt6_Joensuu',true,'2016-11-08 00:11:00',0,0,0,0,104,6060,288,'DSL_4',null,'Kajaani',205,'Kitee',260);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2856,802,'OLD_vt5_Kuopio',true,'2016-11-08 00:11:00',0,0,0,0,104,6061,300,'DSL_4',null,'Siilinjärvi',749,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2857,829,'OLD_vt5_Tikkalansaari',true,'2016-11-08 00:11:00',0,0,0,0,104,6062,313,'DSL_4',null,'Siilinjärvi',749,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2858,830,'OLD_vt5_Kettulanlahti',true,'2016-11-08 00:11:00',0,0,0,0,104,6063,314,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2859,831,'OLD_vt5_Suosaari',true,'2016-11-08 00:11:00',0,0,0,0,104,6064,315,'DSL_4',null,'Siilinjärvi',749,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2860,834,'vt5_Tikkalansaari',false,null,0,0,0,0,104,6065,318,'DSL_4',null,'Siilinjärvi',749,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2861,835,'vt5_Sorsasalo_Vuorela',false,null,0,0,0,0,104,6066,319,'DSL_4',null,'Siilinjärvi',749,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2862,836,'vt5_Vuorela',false,null,0,0,0,0,104,6067,320,'DSL_4',null,'Siilinjärvi',749,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2863,256,'OLD_st180_Kirjalansalmi',true,'2016-11-08 00:11:00',0,0,0,0,101,6068,136,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2864,301,'OLD_vt7_Hamina_Summa',true,'2016-11-08 00:11:00',0,0,0,0,103,6069,138,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2865,302,'vt7_Hamina_Lankamalmi',false,null,0,0,0,0,103,6070,139,'DSL_4',null,'Vaalimaa',1000,'Kotka',285);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2866,303,'vt7_Hamina_Ruissalo',false,null,0,0,0,0,103,6071,140,'DSL_4',null,'Vaalimaa',1000,'Kotka',285);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2867,305,'vt7_Hamina_Kolsila_itä',false,null,0,0,0,0,103,6072,142,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2868,423,'OLD_vt9_Längelmäki',true,'2016-11-08 00:11:00',0,0,0,0,105,6073,154,'DSL_4',null,'Jämsä',182,'Orivesi',562);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2869,444,'OLD_vt3_Pirkkala',true,'2016-11-08 00:11:00',0,0,0,0,102,6074,173,'DSL_4',null,'Nokia',536,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2870,459,'OLD_vt3_Pirkkala_Toivio',true,'2016-11-08 00:11:00',0,0,0,0,102,6075,188,'DSL_4',null,'Nokia',536,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2871,523,'OLD_vt6_Lappeenranta',true,'2016-11-08 00:11:00',0,0,0,0,103,6076,201,'DSL_4',null,'Imatra',153,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2872,543,'OLD_vt13_Nuijamaa_MP1',true,'2016-11-08 00:11:00',0,0,0,0,103,6077,217,'DSL_4',null,'Viipuri',1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2873,544,'OLD_vt13_Nuijamaa_MP2',true,'2016-11-08 00:11:00',0,0,0,0,103,6078,218,'DSL_4',null,'Viipuri',1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2874,545,'OLD_vt13_Nuijamaa_MP3',true,'2016-11-08 00:11:00',0,0,0,0,103,6079,219,'DSL_4',null,'Viipuri',1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2875,546,'OLD_vt13_Nuijamaa_MP4',true,'2016-11-08 00:11:00',0,0,0,0,103,6080,220,'DSL_4',null,'Viipuri',1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2876,548,'OLD_vt13_Nuijamaa_MP6',true,'2016-11-08 00:11:00',0,0,0,0,103,6081,222,'DSL_4',null,'Viipuri',1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2877,549,'OLD_vt13_Nuijamaa_MP7',true,'2016-11-08 00:11:00',0,0,0,0,103,6082,223,'DSL_4',null,'Viipuri',1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2878,552,'OLD_vt7_LAM030707_Uski',true,'2016-11-08 00:11:00',0,0,0,0,103,6083,226,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2879,553,'OLD_vt7_LAM030708_Kattilainen',true,'2016-11-08 00:11:00',0,0,0,0,103,6084,227,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2880,554,'OLD_vt7_LAM030709',true,'2016-11-08 00:11:00',0,0,0,0,103,6085,228,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2881,1066,'vt8_Mustasaari_Lintuvuori',false,null,0,0,0,0,106,6086,394,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2882,1067,'vt8_Vaasa_Böle',false,null,0,0,0,0,106,6087,395,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2883,1240,'OLD_vt20_Rusko',true,'2016-11-08 00:11:00',0,0,0,0,107,6088,428,'DSL_4',null,'Kuusamo',305,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2884,1242,'OLD_vt4_Kempele_mo',true,'2016-11-08 00:11:00',0,0,0,0,107,6089,430,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2885,1460,'kt79_Ylikylä',false,null,0,0,0,0,108,6090,497,'DSL_4',null,'Maalahti',475,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2886,906,'OLD_kt58_Keuruu2',true,'2016-11-08 00:11:00',0,0,0,0,105,6091,326,'DSL_4',null,'Keuruu',249,'Mänttä',506);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2887,926,'OLD_vt4_Toivakka',true,'2016-11-08 00:11:00',0,0,0,0,105,6092,331,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2888,306,'vt13_Nuijamaa_Uusi',false,null,0,0,0,0,103,6093,601,'DSL_4',null,null,1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2889,936,'vt4_Vehniä',false,null,0,0,0,0,105,6094,702,'DSL_6',null,'Äänekoski',992,'Jyväskylä',179);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2890,1068,'vt19_Ilmajoki_Rengonkylä',false,null,0,0,0,0,106,6095,722,'DSL_6',null,'Seinäjoki',743,'Jalasjärvi',164);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2891,3,'kt51_Kivenlahti',false,null,0,0,0,0,100,6096,762,'DSL_4',null,'Kirkkonummi',257,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2892,467,'OLD_vt12_Tre_Santalahti',true,'2016-11-08 00:11:00',0,0,0,0,102,6097,782,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2893,633,'vt5_Mikkeli',false,null,0,0,0,0,104,6098,822,'DSL_6',null,'Kuopio',297,'Lahti',398);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2894,1464,'vt4_Oijustie',false,null,0,0,0,0,108,6099,843,'DSL_4',null,'Rovaniemi',698,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2895,1467,'kt78_Pöykkölä',false,null,0,0,0,0,108,6100,902,'DSL_6',null,'Rovaniemi',698,'Ranua',683);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2896,5,'vt3_Klaukkalantie',false,null,0,0,0,0,100,6101,581,'DSL_4',null,'Hämeenlinna',109,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2897,190,'vt7_Porvoo_Ernestas',false,null,0,0,0,0,100,6102,642,'DSL_4',null,'Porvoo',638,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2898,1069,'vt19_Seinäjoki_Kertunlaakso',false,null,0,0,0,0,106,6103,802,'DSL_6',null,'Seinäjoki',743,'Jalasjärvi',164);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2899,1465,'vt4_Vapaudentie',false,null,0,0,0,0,108,6104,842,'DSL_4',null,'Rovaniemi',698,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2900,1461,'kt81_Vaarala',false,null,0,0,0,0,108,6105,498,'DSL_4',null,'Posio',614,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2901,1462,'kt79_Yli-Kittilä',false,null,0,0,0,0,108,6106,541,'DSL_4',null,'Muonio',498,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2902,99,'vt4_Järvenpää_Isokytö',false,null,0,0,0,0,100,6107,687,'DSL_4',null,'Lahti',398,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2903,1255,'vt8_Hurnasperä',false,null,0,0,0,0,107,6108,521,'DSL_6',null,'Oulu',564,'Kokkola',272);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2904,1463,'vt5_Sipovaara',false,null,0,0,0,0,108,6109,522,'DSL_4',null,'Kemijärvi',320,'Kuusamo',305);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2905,4,'vt3_Kivistö',false,null,0,0,0,0,100,6110,561,'DSL_4',null,'Hämeenlinna',109,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2821,579,'vt7_Siltakylä2',false,null,0,0,0,0,103,4366,247,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(2822,580,'vt7_Mokra',false,null,0,0,0,0,103,4367,248,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(102,205,'vt8_Raisio',false,null,98,98,93,93,101,3,96,'DSL_4',null,'Rauma',684,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(103,206,'vt8_Pyhäranta',false,null,91,92,79,79,101,4,97,'DSL_4',null,'Rauma',684,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(104,207,'vt8_Pori',false,null,79,80,78,77,101,5,98,'DSL_4',null,'Vaasa',905,'Pori',609);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(105,208,'vt9_Aura',false,null,102,99,97,96,101,6,99,'DSL_4',null,'Tampere',837,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(106,209,'vt10_Marttila',false,null,97,96,91,88,101,7,100,'DSL_4',null,'Hämeenlinna',109,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(107,211,'vt12_Lappi_tl',false,null,89,89,58,59,101,8,102,'DSL_4',null,'Lappi',406,'Rauma',684);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(108,221,'vt2_Nakkila',false,null,78,80,79,81,101,9,103,'DSL_4',null,'Pori',609,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(109,223,'vt8_Eurajoki',false,null,81,79,79,78,101,10,104,'DSL_4',null,'Pori',609,'Rauma',684);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(110,224,'vt11_Kullaa',false,null,100,97,68,67,101,11,105,'DSL_4',null,'Pori',609,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(111,225,'st180_Parainen',false,null,77,73,79,76,101,12,106,'DSL_4',null,'Nauvo',533,'Parainen',573);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(112,226,'vt23_Kankaanpää',false,null,90,92,73,76,101,13,107,'DSL_4',null,'Kankaanpää',214,'Pori',609);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(114,228,'OLD_st110_Kevola',true,'2017-03-22 00:03:00',96,94,91,87,101,15,109,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(115,229,'kt40_Tuulissuo',false,null,91,87,86,84,101,16,110,'DSL_4',null,'Helsinki',91,'Naantali',529);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(116,230,'vt10_Lieto',false,null,67,67,70,70,101,17,111,'DSL_4',null,'Hämeenlinna',109,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(117,231,'kt40_Vanto',false,null,74,77,78,80,101,18,112,'DSL_4',null,'Raisio',680,'Naantali',529);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(118,232,'vt2_Porin_lentokent',false,null,97,98,96,96,101,19,113,'DSL_4',null,'Pori',609,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(119,233,'kt40_Hauninen',false,null,96,94,92,92,101,20,114,'DSL_4',null,'Piikkiö',602,'Raisio',680);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(120,234,'kt40_Oriketo',false,null,100,95,93,92,101,21,115,'DSL_4',null,'Piikkiö',602,'Raisio',680);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(121,235,'vt1_Turku_Kupittaa',false,null,68,82,69,80,101,22,116,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(123,237,'vt1_Turku_Kurkela_länsi',false,null,93,88,0,0,101,24,118,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(125,239,'vt1_Kaarina',false,null,113,111,104,102,101,26,120,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(126,240,'vt1_Kaarina_Piikkiö',false,null,115,115,103,103,101,27,121,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(128,243,'vt1_Paimio_Valkoja',false,null,119,105,108,98,101,29,123,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(130,245,'vt1_Salo_Hajala',false,null,114,118,101,106,101,31,125,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(131,246,'OLD_vt1_Halikko',true,'2018-01-11 00:01:00',118,117,105,103,101,32,126,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(133,248,'vt1_Salo_Muurla',false,null,117,112,104,103,101,34,128,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(136,251,'kt40_Krookila',false,null,64,66,64,65,101,37,131,'DSL_4',null,'Piikkiö',602,'Naantali',529);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(138,253,'vt10_Satiaismäki',false,null,65,68,65,70,101,39,133,'DSL_4',null,'Hämeenlinna',109,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(139,501,'st170_Siltakylä',false,null,85,86,83,84,103,40,196,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(143,522,'vt6_Luumäki:kko',false,null,93,90,87,83,103,44,200,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(146,525,'vt7_Virolahti_Kattilainen',false,null,90,94,84,85,null,47,203,'DSL_4',null,null,1001,'Hamina',75);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(149,528,'kt62_Ruokolahti',false,null,87,88,82,87,103,50,206,'DSL_4',null,'Imatra',153,'Puumala',623);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(150,529,'vt6_Utti',false,null,80,79,81,82,103,51,207,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(153,532,'kt62_Imatra_raja2',false,null,42,32,40,30,103,53,210,'DSL_4',null,null,1002,'Imatra',153);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(155,534,'vt6_Muukko',false,null,90,95,86,91,103,55,212,'DSL_4',null,'Imatra',153,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(158,573,'vt7_Hamina_Summa',false,null,110,109,100,99,103,58,241,'DSL_4',null,'Hamina',75,'Karhula',1019);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(159,581,'vt7_Vaalimaa',false,null,41,32,40,31,103,59,249,'DSL_4',null,null,1002,'Hamina',75);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(160,582,'vt13_Nuijamaa',false,null,58,54,53,53,103,60,250,'DSL_4',null,null,1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(161,583,'vt7_Petäjäsuo',false,null,81,85,82,86,103,61,251,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(162,584,'st170_Pyhtää',false,null,86,87,80,83,103,62,252,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(165,587,'vt6_Somerharju',false,null,97,98,94,94,103,65,255,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(169,591,'vt15_Kiehuva',false,null,86,89,83,83,103,69,259,'DSL_4',null,'Kouvola',286,'Kotka',285);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(170,592,'vt6_Metso',false,null,98,97,93,93,103,70,260,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(171,593,'st387_Ylämaa',false,null,92,96,84,87,103,71,261,'DSL_4',null,null,1007,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(172,594,'vt6_Hevossuo',false,null,91,89,89,87,103,72,262,'DSL_4',null,'Kouvola',286,'Lapinjärvi',407);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(174,596,'vt6_Käyrälampi',false,null,86,90,83,86,103,74,264,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(175,602,'vt5_Toivola',false,null,86,88,86,87,104,75,269,'DSL_4',null,'Mikkeli',491,'Heinola',111);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(176,603,'vt5_Joroinen',false,null,82,78,81,82,104,76,270,'DSL_4',null,'Varkaus',915,'Mikkeli',491);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(177,604,'vt13_Ristiina',false,null,96,93,85,87,104,77,271,'DSL_4',null,'Lappeenranta',405,'Mikkeli',491);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(178,605,'vt14_Juva1',false,null,80,79,76,76,104,78,272,'DSL_4',null,'Savonlinna',740,'Mikkeli',491);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(179,606,'vt14_Savonlinna',false,null,67,72,69,68,104,79,273,'DSL_4',null,'Punkaharju',618,'Savonlinna',740);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(180,607,'kt72_Virtasalmi',false,null,98,101,70,68,104,80,274,'DSL_4',null,'Pieksämäki',593,'Mikkeli',491);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(181,622,'vt13_Kangasniemi',false,null,77,77,81,80,104,81,275,'DSL_4',null,'Jyväskylä',179,'Mikkeli',491);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(182,627,'vt5_Nuutilanmäki',false,null,87,93,79,83,104,82,278,'DSL_4',null,'Juva',178,'Mikkeli',491);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(186,203,'vt3_Hämeenkyrö',false,null,89,94,84,85,102,86,94,'DSL_4',null,'Hämeenkyrö',108,'Ylöjärvi',980);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(187,204,'vt3_Ikaalinen',false,null,93,92,85,85,102,87,95,'DSL_4',null,'Parkano',581,'Ikaalinen',143);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(188,210,'vt12_Vammala',false,null,91,94,86,88,102,88,101,'DSL_4',null,'Vammala',912,'Huittinen',102);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(190,402,'st130_Jutikkala',false,null,92,92,85,86,102,90,144,'DSL_4',null,'Tampere',837,'Hämeenlinna',109);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(191,403,'vt2_Humppila',false,null,84,87,85,84,100,91,145,'DSL_4',null,'Pori',609,'Forssa',61);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(193,405,'vt10_Hämeenlinna',false,null,87,85,86,86,100,93,147,'DSL_4',null,'Lammi',401,'Hämeenlinna',109);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(197,409,'kt66_Ruovesi',false,null,88,86,86,85,102,97,151,'DSL_4',null,'Virrat',936,'Ruovesi',702);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(199,422,'st190_Viiala',false,null,88,87,86,86,102,99,153,'DSL_4',null,'Tampere',837,'Urjala',887);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(201,424,'vt4_Renkomäki',false,null,119,114,105,99,100,101,155,'DSL_4',null,'Lahti',398,'Mäntsälä',505);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(203,426,'vt12_Hauho',false,null,92,89,83,83,100,103,157,'DSL_4',null,'Lammi',401,'Hauho',83);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(205,429,'vt3_Riihimäki_Herajoki',false,null,113,111,102,101,100,105,159,'DSL_4',null,'Riihimäki',694,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(207,431,'vt12_Kangasala_Suorama',false,null,84,82,83,80,102,107,161,'DSL_4',null,'Kangasala',211,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(212,437,'vt3_Hlinna_Mo3',false,null,108,107,102,100,100,111,166,'DSL_4',null,'Tampere',837,'Riihimäki',694);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(214,439,'vt12_Tre_Teiskontie',false,null,62,59,63,59,102,113,168,'DSL_4',null,'Kangasala',211,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(216,441,'st140_Hollola',false,null,80,78,79,79,100,115,170,'DSL_4',null,'Heinola',111,'Lahti',398);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(217,442,'vt4_Heinola_Lusi',false,null,99,94,92,88,100,116,171,'DSL_4',null,'Jyväskylä',179,'Heinola',111);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(220,445,'vt9_Kylmäkoski',false,null,85,85,86,86,102,119,174,'DSL_4',null,'Tampere',837,'Humppila',103);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(222,447,'vt12_Lahti_Joutjärvi',false,null,74,72,72,72,100,120,176,'DSL_4',null,null,532,'Lahti',398);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(225,450,'vt3_Myllypuro',false,null,77,79,76,77,102,123,179,'DSL_4',null,'Ylöjärvi',980,'Nokia',536);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(227,452,'vt12_Tre_Onkiniemi',false,null,56,61,60,61,102,125,181,'DSL_4',null,'Tampere',837,'Nokia',536);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(229,454,'OLD_vt12_Tre_Tampella',true,'2017-10-06 00:10:00',67,64,66,65,102,127,183,'DSL_4',null,'Kangasala',211,'Nokia',536);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(231,456,'kt65_Tre_Lielahti',false,null,64,64,60,63,102,129,185,'DSL_4',null,'Ylöjärvi',980,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(232,457,'vt12_Tre_Petsamo',false,null,69,71,68,70,102,130,186,'DSL_4',null,'Kangasala',211,'Nokia',536);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(236,461,'vt4_Vierumäki',false,null,0,0,98,101,100,134,190,'DSL_4',null,'Heinola',111,'Lahti',398);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(239,701,'vt9_Viinijärvi',false,null,96,96,87,87,104,136,284,'DSL_4',null,'Joensuu',167,'Outokumpu',309);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(240,704,'kt71_Kesälahti',false,null,89,92,82,84,104,137,285,'DSL_4',null,'Kitee',260,'Savonlinna',740);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(241,705,'kt73_Lieksa',false,null,77,77,74,75,104,138,286,'DSL_4',null,'Lieksa',422,'Joensuu',167);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(244,723,'vt23_Liperi',false,null,99,100,90,90,104,140,289,'DSL_4',null,'Joensuu',167,'Varkaus',915);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(245,724,'vt9_Niirala',false,null,36,35,38,32,104,141,290,'DSL_4',null,'Venäjä',1006,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(247,726,'vt9_Ylämylly',false,null,94,95,85,87,104,143,292,'DSL_4',null,'Joensuu',167,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(249,729,'vt9_Kaurila',false,null,73,69,76,74,104,145,294,'DSL_4',null,'Venäjä',1006,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(253,804,'vt9_Tuusniemi',false,null,83,82,82,81,104,148,302,'DSL_4',null,'Joensuu',167,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(255,806,'kt77_Pielavesi',false,null,93,96,83,87,104,150,304,'DSL_4',null,'Siilinjärvi',749,'Viitasaari',931);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(258,822,'kt88_Vieremä',false,null,91,96,85,85,104,152,307,'DSL_4',null,'Iisalmi',140,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(260,825,'vt5_Hiltulanlahti',false,null,105,102,101,98,104,154,309,'DSL_4',null,'Kuopio',297,'Varkaus',915);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(261,826,'vt5_Iisalmi',false,null,97,94,89,86,104,155,310,'DSL_4',null,'Kajaani',205,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(268,901,'vt4_Tikkakoski',false,null,89,88,88,86,105,161,321,'DSL_4',null,'Äänekoski',992,'Jyväskylä',179);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(270,903,'vt9_Lievestuore',false,null,87,84,75,72,105,163,323,'DSL_4',null,'Kuhmo',290,'Jyväskylä',179);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(271,904,'vt13_Saarijärvi',false,null,91,91,85,85,105,164,324,'DSL_4',null,'Äänekoski',992,'Kyyjärvi',312);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(274,921,'vt24_Kuhmoinen',false,null,91,94,88,88,105,167,327,'DSL_4',null,'Jämsä',182,'Lahti',398);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(280,928,'vt4_Joutsa',false,null,100,96,98,96,105,172,333,'DSL_4',null,'Jyväskylä',179,'Joutsa',172);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(282,930,'vt9_Jämsä',false,null,83,83,83,81,105,174,335,'DSL_4',null,'Jämsä',182,'Orivesi',562);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(285,933,'vt4_Palokka',false,null,99,99,94,96,105,177,338,'DSL_4',null,'Äänekoski',992,'Jyväskylä',179);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(288,1003,'vt8_Koivulahti',false,null,92,91,85,83,106,180,345,'DSL_4',null,'Kokkola',272,'Vaasa',905);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(289,1004,'vt18_Isokyrö',false,null,94,92,85,85,106,181,346,'DSL_4',null,'Ylistaro',975,'Laihia',399);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(291,1006,'kt67_Ilmajoki',false,null,92,92,85,84,106,183,348,'DSL_4',null,'Seinäjoki',743,'Kurikka',301);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(293,1021,'vt3_Koskue',false,null,93,96,86,87,106,185,350,'DSL_4',null,'Jalasjärvi',164,'Parkano',581);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(296,1024,'kt66_Alavus_Sampsalampi',false,null,87,84,85,85,106,187,353,'DSL_4',null,'Alavus',10,'Virrat',936);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(300,1029,'st724_Raippaluoto',false,null,79,79,78,78,106,191,357,'DSL_4',null,null,1009,'Vaasa',905);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(301,1030,'vt3_Vaasa_Mt',false,null,111,109,100,97,106,192,358,'DSL_4',null,'Vaasa',905,'Laihia',399);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(303,1032,'vt18_Nurmo_Munakka',false,null,88,91,83,86,106,194,360,'DSL_4',null,'Seinäjoki',743,'Ylistaro',975);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(306,1035,'kt63_Kauhava',false,null,97,98,87,88,106,197,363,'DSL_4',null,'Kortesjärvi',281,'Kauhava',233);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(307,1036,'kt68_Pietarsaari',false,null,83,82,82,81,106,198,364,'DSL_4',null,'Pietarsaari',598,'Evijärvi',52);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(310,1039,'kt68_Ähtäri',false,null,83,85,78,79,106,200,367,'DSL_4',null,'Ähtäri',989,'Virrat',936);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(311,1040,'vt28_Kannus',false,null,93,94,87,91,106,201,368,'DSL_4',null,'Kannus',217,'Kokkola',272);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(317,1104,'vt27_Alavieska',false,null,96,97,87,87,107,206,398,'DSL_4',null,'Ylivieska',977,'Kalajoki',208);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(319,1121,'vt4_Kärsämäki',false,null,100,99,98,96,107,208,400,'DSL_4',null,'Pihtipudas',601,'Pulkkila',617);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(320,1123,'kt86_Oulainen',false,null,97,99,87,87,107,209,402,'DSL_4',null,'Oulainen',563,'Ylivieska',977);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(323,1202,'vt4_Ii',false,null,73,76,82,77,107,211,405,'DSL_4',null,'Kemi',240,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(324,1203,'vt5_Kuusamo',false,null,93,93,85,84,107,212,406,'DSL_4',null,'Kemijärvi',320,'Kuusamo',305);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(326,1205,'kt86_Liminka',false,null,99,101,89,91,107,214,408,'DSL_4',null,null,1008,'Liminka',425);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(328,1222,'st847_Tupos',false,null,88,85,77,77,107,216,410,'DSL_4',null,'Liminka',425,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(329,1223,'vt20_Kiiminki',false,null,81,81,82,82,107,217,411,'DSL_4',null,'Kuusamo',305,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(333,1227,'vt22_Muhos',false,null,90,94,85,86,107,220,415,'DSL_4',null,'Kajaani',205,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(336,1230,'vt20_Rankkila',false,null,100,99,97,97,107,223,418,'DSL_4',null,'Kuusamo',305,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(339,1233,'kt88_Alpua',false,null,96,95,84,84,107,226,421,'DSL_4',null,'Raahe',678,'Pulkkila',617);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(343,1237,'vt4_Oulu',false,null,97,96,92,92,107,229,425,'DSL_4',null,'Kemi',240,'Kempele',244);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(344,1238,'vt4_Isko',false,null,105,101,103,98,107,230,426,'DSL_4',null,'Kemi',240,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(349,1243,'vt4_Luhasto',false,null,102,104,97,100,107,235,431,'DSL_4',null,'Kemi',240,'Kempele',244);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(351,1246,'vt4_Kuivasjärvi',false,null,104,101,102,99,107,237,433,'DSL_4',null,'Kemi',240,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(353,1248,'st815_Oulunsalo',false,null,63,64,63,63,107,239,435,'DSL_4',null,'Oulunsalo',567,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(355,1250,'vt4_Välkkylä',false,null,99,102,96,99,107,241,437,'DSL_4',null,'Kemi',240,'Jyväskylä',179);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(358,1303,'kt75_Kankivaara',false,null,95,95,86,85,107,244,444,'DSL_4',null,'Kuhmo',290,'Nurmes',541);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(360,1322,'vt5_Nuottijärvi',false,null,93,94,86,86,107,246,446,'DSL_4',null,'Kajaani',205,'Iisalmi',140);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(363,1325,'kt89_Vartius',false,null,85,86,84,84,107,248,449,'DSL_4',null,'Venäjä',1006,'Kajaani',205);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(365,1327,'kt78_Aska',false,null,95,94,87,86,107,250,451,'DSL_4',null,'Pudasjärvi',615,'Kajaani',205);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(367,1329,'vt22_Mieslahti',false,null,97,97,86,85,107,252,453,'DSL_4',null,'Kajaani',205,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(368,1401,'vt4_Tervola',false,null,85,87,84,85,108,253,454,'DSL_4',null,'Rovaniemi',698,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(372,1405,'kt82_Misi',false,null,95,98,77,78,108,256,458,'DSL_4',null,'Kemijärvi',320,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(374,1422,'kt79_Jääskö',false,null,91,98,87,93,108,258,460,'DSL_4',null,'Kittilä',261,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(377,1425,'kt78_Saukkokangas',false,null,97,96,92,93,108,260,463,'DSL_4',null,'Rovaniemi',698,'Ranua',683);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(378,1426,'vt5_Suomu',false,null,82,89,79,82,108,261,464,'DSL_4',null,'Kemijärvi',320,'Kuusamo',305);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(380,1429,'kt91_Rajajooseppi',false,null,39,32,32,26,108,263,466,'DSL_4',null,'Venäjä',1006,null,1014);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(382,1431,'vt29_Tornio',false,null,38,32,42,36,108,264,468,'DSL_4',null,'Ruotsi',1010,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(384,1433,'st937_Pello',false,null,35,34,37,36,108,266,470,'DSL_4',null,'Ruotsi',1010,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(387,1436,'st959_Karesuvanto',false,null,43,41,47,45,108,269,473,'DSL_4',null,'Ruotsi',1010,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(388,1437,'vt4_mp3_Jokisuu',false,null,80,79,81,77,108,270,474,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(390,1439,'vt29_Luukkaankangas',false,null,112,110,102,99,108,272,476,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(392,1441,'vt29_mp1b_Tornio',false,null,68,83,66,78,108,274,478,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(393,1442,'kt92_Näätämö',false,null,56,66,56,61,108,275,479,'DSL_4',null,'Norja',1020,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(396,1445,'kt92_Karigasniemi',false,null,57,55,60,63,108,277,482,'DSL_4',null,'Norja',1020,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(397,1446,'kt93_Kivilompolo',false,null,52,43,52,41,108,278,483,'DSL_4',null,'Norja',1020,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(400,101,'kt51_Hanasaari',false,null,88,87,85,84,100,281,3,'DSL_4',null,'Espoo',49,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(401,102,'kt51_Soukka',false,null,104,99,98,92,100,282,4,'DSL_4',null,'Kirkkonummi',257,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(402,103,'vt1_Huopalahti',false,null,101,101,95,97,100,283,5,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(403,104,'vt1_Palojärvi',false,null,110,115,100,103,100,284,6,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(405,106,'vt2_Huhmari',false,null,92,89,88,86,100,286,8,'DSL_4',null,'Pori',609,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(406,107,'vt3_Kaivoksela',false,null,86,82,84,82,100,287,9,'DSL_4',null,'Hämeenlinna',109,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(407,108,'vt3_Karhunkorpi',false,null,117,111,104,101,100,288,10,'DSL_4',null,'Hämeenlinna',109,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(408,109,'vt4_Jakomäki',false,null,104,101,98,96,100,289,11,'DSL_4',null,'Mäntsälä',505,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(410,111,'vt6_Liljendal',false,null,92,93,88,88,100,291,13,'DSL_4',null,'Kouvola',286,'Porvoo',638);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(413,115,'kt55_Saksala',false,null,86,87,81,81,100,294,16,'DSL_4',null,'Mäntsälä',505,'Porvoo',638);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(414,116,'st101_Leppävaara',false,null,76,76,72,74,100,295,17,'DSL_4',null,null,1003,null,1015);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(415,117,'vt1_Munkkiniemi',false,null,0,0,0,0,100,296,18,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(416,118,'OLD_st101_Keilaniemi',true,'2016-11-08 00:11:00',87,94,84,89,100,297,19,'DSL_4',null,null,1003,null,1015);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(419,122,'vt2_Karkkila',false,null,90,90,89,89,100,300,23,'DSL_4',null,'Pori',609,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(420,123,'st120_Pähkinärinne',false,null,74,76,71,73,100,301,24,'DSL_4',null,'Vihti',927,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(422,125,'kt50_Bemböle',false,null,69,81,73,80,100,303,26,'DSL_4',null,'Vantaa',92,'Kirkkonummi',257);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(423,126,'st101_Konala',false,null,84,85,81,81,100,304,27,'DSL_4',null,'Itäkeskus',2003,'Tapiola',2002);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(424,127,'kt45_Rusutjärvi',false,null,81,78,79,77,100,305,28,'DSL_4',null,'Hyvinkää',106,'Vantaa',92);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(425,128,'kt50_Voutila',false,null,80,79,77,78,100,306,29,'DSL_4',null,'Vantaa',92,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(426,129,'vt25_Selki',false,null,94,94,85,86,100,307,30,'DSL_4',null,'Vihti',927,'Hyvinkää',106);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(427,130,'vt25_Keravanjärvi',false,null,83,85,80,82,100,308,31,'DSL_4',null,'Hyvinkää',106,'Mäntsälä',505);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(428,131,'kt45_Tammisto',false,null,87,95,90,92,100,309,32,'DSL_4',null,'Tuusula',858,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(430,133,'vt25_Noppo',false,null,89,82,86,80,100,311,34,'DSL_4',null,'Hyvinkää',106,'Mäntsälä',505);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(431,134,'kt51_Inkoo',false,null,94,92,85,85,100,312,35,'DSL_4',null,'Karjaa',220,'Kirkkonummi',257);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(432,135,'st170_Ilola',false,null,75,76,75,75,100,313,36,'DSL_4',null,'Koskenkylä',1004,'Porvoo',638);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(433,136,'st140_Arola',false,null,85,86,82,84,100,314,37,'DSL_4',null,'Mäntsälä',505,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(434,137,'vt3_Keimola',false,null,113,109,100,98,100,315,38,'DSL_4',null,'Hämeenlinna',109,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(435,138,'st120_Odilampi',false,null,66,67,66,67,100,316,39,'DSL_4',null,'Vihti',927,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(436,139,'vt1_Nupuri',false,null,102,109,97,98,100,317,40,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(437,140,'kt50_Järvenperä',false,null,103,98,94,93,100,318,41,'DSL_4',null,'Vantaa',92,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(438,141,'vt7_Box',false,null,113,113,101,101,100,319,42,'DSL_4',null,'Porvoo',638,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(439,142,'vt4_Levanto',false,null,116,117,104,104,100,320,43,'DSL_4',null,'Lahti',398,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(440,143,'kt51_Sundsberg',false,null,84,83,82,81,100,321,44,'DSL_4',null,'Kirkkonummi',257,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(441,144,'vt1_Friisinmäki',false,null,104,98,98,95,100,322,45,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(442,145,'st101_Kannelmäki',false,null,85,87,82,85,100,323,46,'DSL_4',null,'Itäkeskus',2003,'Tapiola',2002);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(443,146,'st101_Länsi-Pakila',false,null,84,88,82,85,100,324,47,'DSL_6',null,null,1003,null,1015);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(444,147,'st101_Pakila',false,null,84,88,80,84,100,325,48,'DSL_4',null,'Itäkeskus',1003,'Tapiola',1015);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(445,148,'st101_Pukinmäki',false,null,85,85,83,83,100,326,49,'DSL_4',null,'Itäkeskus',1003,'Tapiola',1015);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(446,149,'st101_Malmi',false,null,80,83,78,79,100,327,50,'DSL_4',null,null,1003,null,1015);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(447,150,'kt50_Vantaanportti',false,null,85,84,80,81,100,328,51,'DSL_4',null,'Vantaa',92,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(448,151,'st120_Pitäjänmäki',false,null,70,69,65,66,100,329,52,'DSL_4',null,'Vantaa',92,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(449,152,'vt3_Pirkkola',false,null,86,87,83,84,100,330,53,'DSL_4',null,'Tampere',837,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(450,153,'kt45_Oulunkylä',false,null,87,86,82,84,100,331,54,'DSL_4',null,'Tuusula',858,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(451,154,'vt4_Viikinmäki',false,null,97,95,91,91,100,332,55,'DSL_4',null,'Lahti',398,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(452,155,'st170_Kulosaari',false,null,80,81,78,78,100,333,56,'DSL_4',null,null,1003,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(453,156,'kt51_Jorvas',false,null,85,80,83,77,100,334,57,'DSL_4',null,'Hanko',78,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(455,158,'st170_Hagaböle',false,null,81,82,80,79,100,336,59,'DSL_4',null,'Loviisa',434,'Porvoo',638);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(456,159,'kt50_Petikko',false,null,0,0,0,0,100,337,60,'DSL_4',null,'Vantaa',92,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(457,160,'kt50_Heidehof',false,null,84,86,80,83,100,338,61,'DSL_4',null,'Vantaa',92,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(459,162,'st102_Olarinluoma',false,null,90,86,86,85,100,340,63,'DSL_4',null,'Matinkylä',556,'Länsiväylä',1013);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(460,163,'st102_Kokinkylä',false,null,89,89,87,87,100,341,64,'DSL_4',null,'Turunväylä',1012,'Länsiväylä',1013);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(461,164,'vt3_Kivihaka',false,null,80,67,75,67,100,342,65,'DSL_4',null,'Hämeenlinna',109,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(462,165,'vt3_Hakuninmaa',false,null,66,76,81,77,100,343,66,'DSL_4',null,'Hämeenlinna',109,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(464,167,'vt1_Espoo_Sepänkylä',false,null,99,96,92,90,100,345,68,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(468,179,'vt7_Fazerila',false,null,0,0,0,0,100,349,75,'DSL_4',null,'Porvoo',638,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(469,182,'kt50_Hakunila',false,null,0,0,0,0,100,350,76,'DSL_4',null,'Porvoo',638,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(470,183,'kt50_Järvenperä2',false,null,0,0,0,0,100,351,77,'DSL_4',null,'Vantaa',92,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(471,184,'vt7_Porvoo_Uusikrouvi',false,null,0,0,0,0,100,352,78,'DSL_4',null,null,1004,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(472,185,'vt7_Porvoo_Lohijärvi',false,null,0,0,0,0,100,353,79,'DSL_6',null,'Kotka',285,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(473,186,'vt7_Loviisa_Gammelbyviken',false,null,0,0,0,0,100,354,80,'DSL_4',null,'Loviisa',434,null,1004);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(474,187,'vt7_Loviisa_Kärrbacka',false,null,0,0,0,0,100,355,81,'DSL_4',null,'Loviisa',434,null,1004);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(475,188,'vt7_Loviisa',false,null,0,0,0,0,100,356,82,'DSL_4',null,'Kotka',285,null,1004);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(476,189,'vt7_Loviisa_Itä',false,null,0,0,0,0,100,357,83,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(478,191,'vt1_Kolmiranta',false,null,0,0,0,0,100,359,84,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(480,193,'vt1_Hevoskallio',false,null,0,0,0,0,100,361,86,'DSL_4',null,'Lohja',444,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(481,194,'vt1_Lehmijärvi',false,null,0,0,0,0,100,362,87,'DSL_4',null,'Lohja',444,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(483,998,'vt4_Mäntsälä_E',false,null,122,118,103,104,100,364,341,'DSL_4',null,'Lahti',398,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(484,999,'OLD_vt4_Mäntsälä_P',true,'2017-09-19 00:09:00',118,117,104,104,100,365,342,'DSL_4',null,'Lahti',398,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(209,433,'st130_Pispantie',false,null,83,78,83,84,102,366,163,'DSL_4',null,'Tampere',837,'Hämeenlinna',109);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(221,446,'vt12_Lahti_Kärpäsen',false,null,74,74,70,71,100,367,175,'DSL_4',null,'Lahti',398,'Hollola',98);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(151,530,'vt15_Juurikorpi',false,null,92,91,84,83,103,368,208,'DSL_4',null,'Kouvola',286,'Kotka',285);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(237,623,'vt4_Hartola',false,null,91,95,87,89,100,369,276,'DSL_4',null,'Jyväskylä',179,'Heinola',111);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(250,801,'vt5_Leppävirta',false,null,95,93,87,86,104,371,299,'DSL_4',null,'Kuopio',297,'Varkaus',915);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(256,807,'vt27_Kiuruvesi',false,null,95,98,82,88,104,372,305,'DSL_4',null,'Iisalmi',140,'Haapajärvi',69);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(267,832,'vt5_Levänen',false,null,103,101,100,98,104,373,316,'DSL_4',null,'Kuopio',297,'Leppävirta',420);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(277,925,'vt4_Viitasaari',false,null,97,93,88,85,105,374,330,'DSL_4',null,'Pihtipudas',601,'Viitasaari',931);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(309,1038,'kt63_Kaustinen',false,null,86,87,81,81,106,376,366,'DSL_4',null,'Toholampi',849,'Kaustinen',236);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(315,1101,'vt4_Pyhäjärvi',false,null,100,99,99,95,107,377,396,'DSL_4',null,'Kärsämäki',317,'Pihtipudas',601);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(321,1124,'vt8_Rahvo',false,null,93,94,86,86,107,378,403,'DSL_4',null,'Oulu',564,'Kokkola',272);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(331,1225,'kt78_Pudasjärvi',false,null,102,98,92,87,107,379,413,'DSL_4',null,'Ranua',683,'Pudasjärvi',615);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(340,1234,'vt5_Toranki',false,null,81,82,77,78,107,380,422,'DSL_4',null,'Kuusamo',305,'Kajaani',205);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(362,1324,'vt22_Törmäkylä',false,null,101,100,95,97,107,381,448,'DSL_4',null,'Kajaani',205,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(369,1402,'vt4_Ivalo',false,null,91,91,86,86,108,382,455,'DSL_4',null,'Inari',148,'Sodankylä',758);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(375,1423,'kt81_Peräposio',false,null,98,90,85,78,108,383,461,'DSL_4',null,'Posio',614,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(381,1430,'kt82_Salla_Kelloselkä',false,null,34,34,33,30,108,384,467,'DSL_4',null,'Venäjä',1006,'Salla',732);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(394,1443,'st970_Nuorgam',false,null,53,55,52,52,108,385,480,'DSL_4',null,'Norja',1020,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(906,556,'vt6_Selkäharju',false,null,0,0,0,0,103,1627,230,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(908,558,'vt6_Mattila',false,null,0,0,0,0,103,1629,232,'DSL_4',null,'Lappeenranta',405,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(909,559,'vt6_Myllymäki',false,null,0,0,0,0,103,1630,233,'DSL_4',null,'Imatra',153,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(910,560,'vt6_Karhuvuori',false,null,0,0,0,0,103,1631,234,'DSL_4',null,'Imatra',153,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(911,561,'vt6_Hartikkala',false,null,0,0,0,0,103,1632,235,'DSL_4',null,'Imatra',153,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(912,562,'vt6_Mälkiä',false,null,0,0,0,0,103,1633,236,'DSL_4',null,'Imatra',153,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(931,598,'vt7_Vaalimaa_rask',false,null,0,0,0,0,103,1652,266,'DSL_4',null,null,1002,'Hamina',75);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1013,1254,'vt20_Rusko2',false,null,0,0,0,0,107,1734,441,'DSL_4',null,'Kuusamo',305,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1020,1452,'vt4_Marostenmäki',false,null,0,0,0,0,108,1741,489,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1021,1453,'vt4_Siikalahti',false,null,0,0,0,0,108,1742,490,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1022,1454,'vt4_Peurasaari',false,null,0,0,0,0,108,1743,491,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1023,1455,'vt4_Karjalahti',false,null,0,0,0,0,108,1744,492,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1024,1456,'vt4_Kivikko',false,null,0,0,0,0,108,1745,493,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1025,1457,'vt4_Ristikangas',false,null,0,0,0,0,108,1746,494,'DSL_4',null,'Tornio',851,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1036,196,'Niinisaarentie',false,null,0,0,0,0,null,1754,89,'DSL_4',null,'Porvoo',638,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1037,197,'st100_Kivihaan_tunneli',false,null,0,0,0,0,100,1755,90,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(418,121,'yt11888_Liljendal2',false,null,80,79,72,75,100,299,22,'DSL_4',null,'Kouvola',286,'Porvoo',638);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(127,242,'vt1_Paimio_Paimionjoki',false,null,114,114,103,103,101,28,122,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(142,521,'vt6_Elimäki',false,null,97,99,94,96,103,43,199,'DSL_4',null,'Kouvola',286,'Lapinjärvi',407);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(156,571,'OLD_vt7_Otsola',true,'2016-11-08 00:11:00',99,99,94,96,103,56,239,'DSL_4',null,'Hamina',75,'Karhula',1019);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(168,590,'vt12_Tillola',false,null,94,95,82,85,103,68,258,'DSL_4',null,'Kouvola',286,'Lahti',398);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(184,630,'vt5_Kuortti',false,null,98,98,89,88,104,84,281,'DSL_4',null,'Mikkeli',491,'Heinola',111);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(196,408,'kt54_Hausjärvi',false,null,92,94,84,86,100,96,150,'DSL_4',null,'Hollola',98,'Riihimäki',694);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(211,436,'yt13782_Nokia_Rajasalmi',false,null,59,57,62,63,102,110,165,'DSL_4',null,'Nokia',536,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(224,449,'vt3_Tre_Sarankulma',false,null,78,76,77,75,102,122,178,'DSL_4',null,'Nokia',536,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(238,628,'vt4_Murhamäki',false,null,102,113,104,99,100,135,279,'DSL_4',null,'Mikkeli',491,'Heinola',111);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(248,727,'vt6_Kontiolahti2',false,null,93,93,83,83,104,144,293,'DSL_4',null,'Nurmes',541,'Joensuu',167);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(262,827,'vt9_Jännevirta',false,null,94,96,86,87,104,156,311,'DSL_4',null,'Joensuu',167,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(276,923,'vt4_Jyväskylä',false,null,86,88,86,87,105,169,329,'DSL_4',null,'Jyväskylä',179,null,1017);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(287,1002,'vt3_Hälsingby',false,null,83,84,82,83,106,179,344,'DSL_4',null,'Vaasa',905,'Laihia',399);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(299,1028,'kt68_Evijärvi',false,null,97,96,90,86,106,190,356,'DSL_4',null,'Pietarsaari',598,'Alajärvi',5);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(313,1042,'kt67_Kauhajoki_P',false,null,94,93,87,86,106,203,370,'DSL_4',null,'Kurikka',301,'Kauhajoki',232);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(330,1224,'vt20_Pintamo',false,null,99,98,87,91,107,218,412,'DSL_4',null,'Kuusamo',305,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(357,1302,'vt5_Ristijärvi',false,null,99,97,84,86,107,243,443,'DSL_4',null,'Hyrynsalmi',105,null,1018);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(370,1403,'vt5_Kemijärvi',false,null,95,94,84,83,108,254,456,'DSL_4',null,'Pelkosenniemi',583,'Kemijärvi',320);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(385,1434,'st943_Kolari',false,null,43,38,44,39,108,267,471,'DSL_4',null,'Ruotsi',1010,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(412,114,'vt25_Virkkala',false,null,93,91,89,86,100,293,15,'DSL_4',null,'Hanko',78,'Lohja',444);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(100,201,'st110_Muurla_Salo',false,null,92,89,88,86,101,1,92,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(113,227,'vt1_Kaarina_Kirismäki',false,null,116,112,105,100,101,14,108,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(122,236,'OLD_vt1_Huhkola',true,'2018-01-11 00:01:00',90,90,90,90,101,23,117,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(129,244,'vt1_Paimio_Vista',false,null,115,118,104,105,101,30,124,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(135,250,'vt8_Masku',false,null,82,80,0,0,101,36,130,'DSL_4',null,'Rauma',684,'Turku',853);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(140,502,'vt12_Iitti',false,null,90,94,81,84,103,41,197,'DSL_4',null,'Kouvola',286,'Lahti',398);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(148,527,'vt15_Valkeala',false,null,94,92,86,83,103,49,205,'DSL_4',null,'Mikkeli',491,'Kouvola',286);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(157,572,'vt7_Salminlahti',false,null,108,110,99,101,103,57,240,'DSL_4',null,'Hamina',75,'Karhula',1019);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(164,586,'vt15_Kotkansaari',false,null,71,71,67,69,103,64,254,'DSL_4',null,'Kouvola',286,'Kotka',285);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(173,595,'vt6_Puhjo',false,null,88,89,82,84,103,73,263,'DSL_4',null,'Lappeenranta',405,'Lapinjärvi',407);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(185,631,'vt5_Kuvansi_uusi',false,null,89,94,89,88,104,85,282,'DSL_4',null,'Varkaus',915,'Joroinen',171);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(195,407,'vt12_Hollola',false,null,91,95,82,86,100,95,149,'DSL_4',null,'Hollola',98,'Lammi',401);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(210,435,'vt9_Tre_Karkuvuori',false,null,93,93,97,94,102,109,164,'DSL_4',null,'Orivesi',562,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(226,451,'vt9_Tre_Aitovuori',false,null,85,88,86,86,102,124,180,'DSL_4',null,'Orivesi',562,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(233,458,'vt12_Tre_Ruotula',false,null,69,73,68,71,102,131,187,'DSL_4',null,'Kangasala',211,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(275,922,'vt9_Muurame',false,null,95,99,91,93,105,168,328,'DSL_4',null,'Jyväskylä',179,'Jämsä',182);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(284,932,'vt4_Hännilänsalmi',false,null,57,61,58,62,105,176,337,'DSL_4',null,'Viitasaari',931,'Jyväskylä',179);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(297,1026,'vt8_Kovjoki',false,null,95,96,88,87,106,188,354,'DSL_4',null,'Kokkola',272,'Vaasa',905);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(335,1229,'vt4_Kuivaniemi',false,null,95,93,89,87,107,222,417,'DSL_4',null,'Kemi',240,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(350,1244,'vt4_Laanila',false,null,102,99,101,96,107,236,432,'DSL_4',null,'Kemi',240,'Kempele',244);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(361,1323,'vt6_Korholanmäki',false,null,100,97,88,85,107,247,447,'DSL_4',null,'Kajaani',205,'Sotkamo',765);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(399,1,'vt7_Rita',false,null,111,112,95,99,100,280,1,'DSL_4',null,null,1004,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(404,105,'st110_Hiidenvesi',false,null,81,82,83,82,100,285,7,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(411,112,'vt7_Treksilä',false,null,113,111,100,99,100,292,14,'DSL_4',null,'Porvoo',638,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(421,124,'vt25_Dragsvik',false,null,83,87,83,86,100,302,25,'DSL_4',null,'Hanko',78,'Lohja',444);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(507,168,'kt50_Askisto',false,null,80,100,80,80,100,393,69,'DSL_4',null,'Vantaa',92,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(508,169,'kt50_Pakkala',false,null,80,80,80,80,100,394,70,'DSL_4',null,'Vantaa',92,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(515,195,'st103_Satamatie',false,null,0,0,0,0,100,401,88,'DSL_4',null,'Porvoo',638,'Espoo',49);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(526,254,'st180_Kuusisto',false,null,80,80,80,80,101,412,134,'DSL_4',null,'Kuusisto',993,'Kaarina',202);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(527,255,'st180_Simonby',false,null,80,80,80,80,101,413,135,'DSL_4',null,'Nauvo',533,'Parainen',573);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(545,535,'kt62_Imatra_raja1',false,null,30,60,30,60,103,431,213,'DSL_4',null,null,1002,'Imatra',153);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(546,540,'vt7_Virojoki',false,null,80,60,80,60,null,432,214,'DSL_4',null,null,1001,'Hamina',75);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(547,541,'vt7_Mestarmäki',false,null,80,80,80,80,null,433,215,'DSL_4',null,'Vaalimaa',1000,'Hamina',75);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(549,550,'vt7_Vaahterikonsuo',false,null,80,60,80,60,null,435,224,'DSL_4',null,null,1001,'Hamina',75);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(550,551,'vt7_LAM030706',false,null,80,60,80,60,103,436,225,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(552,555,'vt170_Tallinmäki',false,null,80,60,80,60,103,438,229,'DSL_4',null,null,1001,'Hamina',75);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(571,597,'vt13_Nuijamaa_rask',false,null,60,60,60,60,103,457,265,'DSL_4',null,null,1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(607,462,'vt3_Nokia_Rajasalmi',false,null,100,100,100,100,102,493,191,'DSL_4',null,'Nokia',536,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(608,463,'vt3_Pirkkala_Sankila',false,null,100,100,100,100,102,494,192,'DSL_4',null,'Nokia',536,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(619,730,'vt9_Noljakka',false,null,0,0,0,0,104,505,295,'DSL_4',null,'Joensuu',167,'Kuopio',297);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(620,731,'vt6_Reijola',false,null,100,100,80,80,104,506,296,'DSL_4',null,'Joensuu',167,'Kitee',260);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(632,833,'kt75_Pajukoski',false,null,80,80,80,80,104,518,317,'DSL_4',null,'Nilsiä',534,'Siilinjärvi',749);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(663,934,'st637_Tiituspohja',false,null,80,80,80,80,105,549,339,'DSL_4',null,'Laukaa',410,'Jyväskylä',179);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(675,1048,'vt13_Kokkola_Ribacken',false,null,100,100,80,80,106,561,376,'DSL_4',null,'Kyyjärvi',312,'Kokkola',272);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(676,1049,'vt19_Lapua_Voitilanjärvi',false,null,100,100,80,80,106,562,377,'DSL_4',null,'Uusikaarlepyy',893,'Lapua',408);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(677,1050,'vt8_Kristiinankaupunki',false,null,100,100,80,80,106,563,378,'DSL_4',null,'Vaasa',905,'Pori',609);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(678,1051,'vt3_Laihia_Perälä',false,null,80,80,80,80,106,564,379,'DSL_4',null,'Vaasa',905,'Kurikka',301);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(679,1052,'vt8_Himanka',false,null,100,100,80,80,107,565,380,'DSL_4',null,'Oulu',564,'Kokkola',272);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(680,1053,'vt18_Laihia_Vedenoja',false,null,100,100,80,80,106,566,381,'DSL_4',null,'Ylistaro',975,'Laihia',399);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(681,1054,'kt44_Kauhajoki',false,null,80,80,80,80,106,567,382,'DSL_4',null,'Kurikka',301,'Honkajoki',99);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(682,1055,'kt68_Lehtimäki',false,null,80,80,80,80,106,568,383,'DSL_4',null,'Alajärvi',5,'Ähtäri',989);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(683,1056,'vt18_Alavus_Kuivaskylä',false,null,80,80,80,80,106,569,384,'DSL_4',null,'Alavus',10,'Peräseinäjoki',589);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(706,1450,'vt4_Revontuli',false,null,70,70,70,70,108,592,487,'DSL_4',null,'Sodankylä',758,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(761,1602,'vt1_Kruusila',false,null,120,120,100,100,101,1482,501,'E_18',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(762,1603,'vt1_Syvälampi',false,null,120,120,100,100,101,1483,502,'E_18',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(764,1605,'vt1_Hauklampi',false,null,120,120,100,100,100,1485,504,'E_18',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(766,1607,'vt1_Karnainen',false,null,120,120,100,100,100,1487,506,'E_18',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(409,110,'vt4_Mäntsälä',false,null,118,118,105,104,100,290,12,'DSL_4',null,'Lahti',398,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1192,464,'yt3495_Rautaharkko',false,null,0,0,0,0,102,2453,193,'DSL_4',null,'Tampere',837,'Hämeenlinna',109);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1323,1044,'kt66_Kuortane',false,null,0,0,0,0,106,2584,372,'DSL_4',null,'Lapua',408,'Alavus',10);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1324,1045,'vt8_Mustasaari_Riimal',false,null,0,0,0,0,106,2585,373,'DSL_4',null,'Vaasa',905,'Pori',609);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1325,1046,'kt67_Teuva',false,null,0,0,0,0,106,2586,374,'DSL_4',null,'Teuva',846,'Kaskinen',231);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1326,1047,'vt3_Kurikka_Tuiskula',false,null,0,0,0,0,106,2587,375,'DSL_4',null,'Vaasa',905,'Tampere',837);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1372,1251,'vt4_Oulu_Lintula',false,null,0,0,0,0,107,2633,438,'DSL_4',null,'Kemi',240,'Jyväskylä',179);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1373,1252,'st866_Kuusamo_T',false,null,0,0,0,0,107,2634,439,'DSL_4',null,'Venäjä',1006,'Kuusamo',305);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1483,175,'vt1_Lommila',false,null,0,0,0,0,100,2385,71,'DSL_4',null,'Turku',853,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1590,465,'vt3_Hämeenlinna_E',false,null,0,0,0,0,100,3427,194,'DSL_4',null,'Tampere',837,'Riihimäki',694);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1591,466,'vt3_Hämeenlinna_P',false,null,0,0,0,0,100,3428,195,'DSL_4',null,'Tampere',837,'Riihimäki',694);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1611,542,'vt7_Turkkisuo',false,null,0,0,0,0,null,3448,216,'DSL_4',null,'Venäjä',1006,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1612,547,'vt13_Nuijamaa_MP5',false,null,0,0,0,0,103,3449,221,'DSL_4',null,null,1002,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1623,563,'vt6_Joutseno',false,null,0,0,0,0,103,3460,237,'DSL_4',null,'Imatra',153,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1624,564,'vt6_Vesivalo',false,null,0,0,0,0,103,3461,238,'DSL_4',null,'Imatra',153,'Lappeenranta',405);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1628,574,'vt7_Kyminlinna',false,null,0,0,0,0,103,3465,242,'DSL_4',null,'Karhula',1019,'Kotka',285);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1629,575,'vt7_Pyhtää2',false,null,0,0,0,0,103,3466,243,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1630,576,'vt7_Karhula_Länsi',false,null,0,0,0,0,103,3467,244,'DSL_4',null,'Karhula',1019,'Kotka',285);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1631,577,'vt7_Otsola_Länsi',false,null,0,0,0,0,103,3468,245,'DSL_4',null,'Hamina',75,'Karhula',1019);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1648,599,'vt7_Leppäsaari',false,null,0,0,0,0,103,3485,267,'DSL_4',null,'Kotka',285,'Loviisa',434);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1662,632,'vt14_Hevonpäänniemi',false,null,0,0,0,0,104,3499,283,'DSL_4',null,'Savonlinna',740,'Juva',178);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1675,733,'vt6_Joensuu2',false,null,0,0,0,0,104,3512,298,'DSL_4',null,'Kajaani',205,'Kitee',260);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1753,1057,'vt8_Vaasa_Yhdystie',false,null,0,0,0,0,106,3590,385,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1754,1058,'vt16_Ylistaro',false,null,0,0,0,0,106,3591,386,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1755,1059,'vt19_Nurmo',false,null,0,0,0,0,106,3592,387,'DSL_4',null,'Lapua',408,'Seinäjoki',743);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1756,1060,'kt66_Alavus_Pohjoinen',false,null,0,0,0,0,106,3593,388,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1757,1061,'vt18_Seinäjöki_Pajuneva',false,null,0,0,0,0,106,3594,389,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1758,1062,'vt18_Töysä_Jokikylä',false,null,0,0,0,0,106,3595,390,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1759,1063,'vt8_Kaitsor_Vöyri-Maksamaa',false,null,0,0,0,0,106,3596,391,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1760,1064,'vt13_Perho_Möttönen',false,null,0,0,0,0,106,3597,392,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1761,1065,'vt18_Ähtäri_Inha',false,null,0,0,0,0,106,3598,393,'DSL_4',null,null,null,null,null);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1804,1253,'st816_Hailuoto',false,null,0,0,0,0,107,3641,440,'DSL_4',null,'Hailuoto',104,'Kempele',244);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1848,1447,'vt21_Kilpisjärvi_T',false,null,0,0,0,0,108,3685,484,'DSL_4',null,'Norja',1020,'Muonio',498);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1849,1448,'vt4_Torvinen',false,null,0,0,0,0,108,3686,485,'DSL_4',null,'Sodankylä',758,'Rovaniemi',698);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1850,1449,'vt4_Yläpostojoki',false,null,0,0,0,0,108,3687,486,'DSL_4',null,'Inari',148,'Sodankylä',758);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1852,1451,'vt4_Ala-Korkalo',false,null,0,0,0,0,108,3689,488,'DSL_4',null,'Rovaniemi',698,'Kemi',240);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1859,1458,'vt21_Tornio',false,null,0,0,0,0,108,3696,495,'DSL_4',null,'Kilpisjärvi',1021,'Tornio',851);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1860,1459,'vt21_Ylitornio',false,null,0,0,0,0,108,3697,496,'DSL_4',null,'Kilpisjärvi',1021,'Tornio',851);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(1861,1499,'vt29_mp1a_Tornio',false,null,0,0,0,0,108,3698,499,'DSL_4',null,'Norja',1020,'Suomi',1011);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3080,7,'vt4_Vantaa_Honkanummi',false,null,0,0,0,0,100,6843,1042,'DSL_6',null,'Lahti',398,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3081,8,'vt4_Kerava_Jokivarsi',false,null,0,0,0,0,100,6844,1062,'DSL_4',null,'Lahti',398,'Helsinki',91);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3100,1256,'vt22_Oulu_Kaukovainio',false,null,0,0,0,0,107,7203,1082,'DSL_6',null,'Kajaani',205,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3101,1257,'vt22_Oulu_Iinatti',false,null,0,0,0,0,107,7204,1102,'DSL_6',null,'Kajaani',205,'Oulu',564);
insert into lam_station(id, natural_id, name, obsolete, obsolete_date, summer_free_flow_speed_1, summer_free_flow_speed_2, winter_free_flow_speed_1, winter_free_flow_speed_2, road_district_id, road_station_id, lotju_id, lam_station_type, calculator_device_type, direction_1_municipality, direction_1_municipality_code, direction_2_municipality,
 direction_2_municipality_code)
values(3120,1468,'vt21_Muonio_Pahtonen',false,null,0,0,0,0,108,7288,1122,'DSL_6',null,'Muonio',498,'Ylitornio',976);

alter sequence seq_lam_station restart with 3121;
