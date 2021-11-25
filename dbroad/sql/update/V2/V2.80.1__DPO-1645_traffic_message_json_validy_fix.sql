with fixed_data AS (
  select valid_d.id
       -- If result is ST_GeometryCollection extract it to LineStrings else use simplified value.
       -- Then replace original feature's geometry with the value
       , to_json(jsonb_set(valid_d.json_message::jsonb, '{geometry}',
                           ST_AsGeoJSON(case when ST_GeometryType(valid_d.valid_simple) = 'ST_GeometryCollection' then ST_CollectionExtract(valid_d.valid_simple, 2)
                                             else valid_d.valid_simple end)::jsonb)) as fixed_feture
  from (
         SELECT d.id
              -- Extract the geometry and fix it. Can produce ST_GeometryCollection and we don't want that that as that is not widely supported
              , ST_MakeValid(ST_Simplify(ST_GeomFromGeoJSON((json_message::json -> 'geometry')::text), 0.00005, true)) valid_simple
              , d.json_message
         FROM datex2 d
              -- only messages with json
         where d.original_json_message is null
           -- not fixed already
           AND d.json_message is not null
           -- > fix only non area locations as areas are fetched from area_location- table
           AND d.json_message::json -> 'properties' -> 'announcements' -> 0 -> 'locationDetails' -> 'areaLocation' is null
           AND ST_IsValid(ST_GeomFromGeoJSON((json_message::json -> 'geometry')::text)) = false
           -- skip messages having null geometry
           AND (json_message::json -> 'geometry')::text <> 'null'
       ) valid_d
)
UPDATE datex2 tgt
-- Make backup of original json_message
set original_json_message = json_message,
    -- update value to fixed feature
    json_message = fixed_data.fixed_feture
from fixed_data
where tgt.id = fixed_data.id;