CREATE INDEX camera_preset_publishable_i
ON camera_preset
USING BTREE ((CASE
    WHEN (obsolete_date IS NULL AND lotju_id IS NOT NULL AND camera_lotju_id IS NOT NULL AND public_internal = true AND public_external =
     true)
     THEN true
    ELSE false
END) ASC, camera_id ASC);

CREATE UNIQUE INDEX camera_preset_id_ui
ON camera_preset
USING BTREE (preset_id ASC, (CASE
    WHEN obsolete_date IS NULL THEN -1
    ELSE id
END) ASC);

CREATE INDEX road_station_publishable_i
ON road_station
USING BTREE ((CASE
    WHEN (obsolete_date IS NULL AND collection_status <> 'REMOVED_PERMANENTLY' AND is_public = true AND lotju_id IS NOT NULL) THEN true
    ELSE false
END) ASC, type ASC);
