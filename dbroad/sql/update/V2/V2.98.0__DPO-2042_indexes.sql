-- Duplicate indexes
drop index if exists data_source_info_id_i;
drop index if exists counting_site_counter_domain_fki;
drop index if exists maintenance_tracking_domain_task_mapping_domain_original_id_i;
drop index if exists maintenance_tracking_observation_data_tracking_data_fkey_i;
drop index if exists maintenance_tracking_task_tracking_id_task_i;