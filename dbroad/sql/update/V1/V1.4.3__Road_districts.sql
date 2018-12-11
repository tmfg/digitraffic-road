--select 'insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(' || id || ',''' || name ||
--''',' || obsolete || ',' || obsolete_date || ',' || natural_id || ',' || speed_limit_season || ');' from sujuvuus.road_district;

insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(999,'Määrittelemätön',true,'08.02.2017',
999,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(100,'Uusimaa',false,null,1,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(101,'Varsinais-Suomi',false,null,2,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(102,'Pirkanmaa',false,null,4,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(103,'Kaakkois-Suomi',false,null,3,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(104,'Pohjois-Savo',false,null,8,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(105,'Keski-Suomi',false,null,9,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(106,'Etelä-Pohjanmaa',false,null,10,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(107,'Pohjois-Pohjanmaa',false,null,12,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(108,'Lappi',false,null,14,2);
insert into road_district(id, name, obsolete, obsolete_date, natural_id, speed_limit_season) values(120,'Muurla-Lohja',true,'08.02.2017',
16,2);

alter sequence seq_road_district restart with 1000;