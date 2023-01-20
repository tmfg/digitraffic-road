ALTER TABLE maintenance_tracking
  ALTER COLUMN domain SET NOT NULL;

drop index if exists maintenance_tracking_created_i;
drop index if exists maintenance_tracking_created_domain_i;
drop index if exists maintenance_tracking_end_created_domain_i;
drop index if exists maintenance_tracking_domain_end_time_i;
drop index if exists maintenance_tracking_domain_created_i;

drop index if exists maintenance_tracking_task_tracking_id_task_i;
drop index if exists maintenance_tracking_end_time_domain_created_i;
drop index if exists maintenance_tracking_created_domain_end_time_i;
drop index if exists maintenance_tracking_domain_end_time_created_i;
drop index if exists maintenance_tracking_domain_contract_domain_contract_source_i;

create index if not exists maintenance_tracking_task_tracking_id_task_i on maintenance_tracking_task USING btree (maintenance_tracking_id, task);
create index if not exists maintenance_tracking_end_time_domain_created_i on maintenance_tracking USING btree (end_time, domain, created);
create index if not exists maintenance_tracking_created_domain_end_time_i on maintenance_tracking USING btree (created, domain, end_time);
create index if not exists maintenance_tracking_domain_end_time_created_i on maintenance_tracking USING btree (domain, end_time, created);
create index if not exists maintenance_tracking_domain_contract_domain_contract_source_i on maintenance_tracking_domain_contract USING btree (domain, contract, source);

-- DPO-2064 Turhien indeksien poistoa
drop index if exists datex2_situation_record_validy_search_i;
drop index if exists datex2_situation_type_i;
drop index if exists road_section_road_natural_i;
drop index if exists rs_sensor_publishable_i;
drop index if exists region_geometry_code_i;
drop index if exists counting_site_domain_added_i;