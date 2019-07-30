-- reindex after every db update
reindex table locking_table;
reindex table camera_preset;
reindex table data_updated;
reindex table forecast_section_weather;
reindex table datex2;
reindex table datex2_situation;
reindex table datex2_situation_record;
reindex table road_station;

-- reindex qrtz_tables
reindex table qrtz_fired_triggers;
reindex table qrtz_simple_triggers;
reindex table qrtz_triggers;
