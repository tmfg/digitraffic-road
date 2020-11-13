CREATE OR REPLACE VIEW maintenance_tracking_view AS
    SELECT id
         , sending_system
         , sending_time
         , last_point
         -- Simplifies the geometry with preserve collapsed flag
         , ST_Simplify(line_string, 0.00005, true) line_string
         , work_machine_id
         , start_time
         , end_time
         , direction
         , finished
         , created
         , modified
FROM maintenance_tracking;