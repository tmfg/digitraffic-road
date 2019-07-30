-- rewrite as partial index
alter table forecast_section drop constraint forecast_section_unique; -- unique constraint
drop index forecast_section_unique; -- unique constraint
drop index forecast_section_ui;

create index forecast_section_natural_version_id_key on forecast_section(natural_id, version, id) where obsolete_date is null;
alter table forecast_section add constraint forecast_section_unique unique using index forecast_section_natural_version_id_key; -- recreate unique constraint

-- duplicate
drop index forecast_section_version;

-- rewrite as partial indexes
drop index work_machine_tracking_type_i;
drop index work_machine_tracking_handled_created_i;

create index work_machine_tracking_created_idx on work_machine_tracking (created) where handled is null;
create index work_machine_tracking_type2_idx on work_machine_tracking(id) where type is null;
