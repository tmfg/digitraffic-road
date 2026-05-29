-- Drop the old duplicate index from V2.61.0 that was never removed by V3.31.0.
-- V3.31.0 already created the correct covering index device_data_device_id_effect_date_i
-- on (device_id, effect_date DESC) INCLUDE (id), making this unique index redundant.
-- Having both indexes causes extra write overhead and confuses the query planner.
DROP INDEX IF EXISTS device_data_effect_date_key;

