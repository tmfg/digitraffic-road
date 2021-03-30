ALTER TABLE camera_preset
DISABLE TRIGGER trg_camera_preset_delete;

delete from camera_preset
where id in (143862,143868,1363910,1363912,1363921,1363931,143618,143803,1363922,1363925,143843,143846,143847,143882);

ALTER TABLE camera_preset
ENABLE TRIGGER trg_camera_preset_delete;