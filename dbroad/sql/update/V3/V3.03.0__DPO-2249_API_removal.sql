-- Not used in new APIs
alter table forecast_section drop column road_id;
alter table forecast_section drop column start_road_section_id;
alter table forecast_section drop column end_road_section_id;

drop table road_section;
drop table road;