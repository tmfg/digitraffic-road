drop index camera_preset_publishable_i; -- unused

-- rewrite as partial index
drop index camera_preset_id_ui;
create index camera_preset_id_key on camera_preset(id) where obsolete_date is null;

-- should run reindex periodically
reindex table locking_table;
reindex table camera_preset;
reindex table data_updated;
reindex table forecast_section_weather;
reindex table datex2;
reindex table datex2_situation;
reindex table datex2_situation_record;
reindex table road_station;

-- slow ones
reindex table sensor_value;
reindex table work_machine_tracking;
