drop index camera_preset_publishable_i; -- unused

-- rewrite as partial index
drop index camera_preset_id_ui;
create index camera_preset_id_key on camera_preset(id) where obsolete_date is null;

-- slow ones
reindex table sensor_value;
reindex table work_machine_tracking;
