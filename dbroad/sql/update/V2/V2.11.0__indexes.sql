-- rewrite as partial index
drop index forecast_section_unique;
drop index forecast_section_ui;

create index forecast_section_natural_version_id_key on forecast_section(natural_id, version, id) where obsolete_date is null;

-- duplicate
drop index forecast_section_version;


-- rewrite as partial indexes
drop index work_machine_tracking_type_i;
drop index work_machine_tracking_handled_created_i;

create index work_machine_tracking_created_idx on work_machine_tracking (created) where handled is null;
create index work_machine_tracking_type2_idx on work_machine_tracking(id) where type is null;
