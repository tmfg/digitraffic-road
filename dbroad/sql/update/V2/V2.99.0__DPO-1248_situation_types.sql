--- Not used indexes ---
DROP INDEX IF EXISTS device_data_device_fk; -- This is not related to datex2
DROP INDEX IF EXISTS datex2_type_import_i;
DROP INDEX IF EXISTS datex2_situation_type_import_i;
DROP INDEX IF EXISTS datex2_detailed_type_import_i;

---- Situation type ----
ALTER TABLE datex2 ADD COLUMN IF NOT EXISTS situation_type TEXT;
CREATE INDEX IF NOT EXISTS datex2_situation_type_i ON datex2(situation_type);
CREATE INDEX IF NOT EXISTS datex2_is_json_message_not_null_i ON datex2 (id) WHERE json_message IS NOT NULL;

ALTER TABLE datex2 ADD CONSTRAINT datex2_situation_type_check
    CHECK (situation_type IN ('TRAFFIC_ANNOUNCEMENT', 'EXEMPTED_TRANSPORT', 'WEIGHT_RESTRICTION', 'ROAD_WORK'));

UPDATE datex2
SET situation_type =
    case
        when message_type = 'TRAFFIC_INCIDENT' then 'TRAFFIC_ANNOUNCEMENT'
        when message_type = 'WEIGHT_RESTRICTION' then 'WEIGHT_RESTRICTION'
        when message_type = 'ROADWORK' then 'ROAD_WORK'
        when detailed_message_type = 'EXEMPTED_TRANSPORT' then 'SPECIAL_TRANSPORT'
        else 'TRAFFIC_ANNOUNCEMENT'
    end;

ALTER TABLE datex2 ALTER COLUMN situation_type SET NOT NULL;

---- Traffic Announcement type ----
ALTER TABLE datex2 ADD COLUMN IF NOT EXISTS traffic_announcement_type TEXT;
ALTER TABLE datex2 ADD CONSTRAINT datex2_traffic_announcement_type_check
    CHECK (CASE WHEN situation_type = 'TRAFFIC_ANNOUNCEMENT' THEN
               traffic_announcement_type IN ('GENERAL', 'PRELIMINARY_ACCIDENT_REPORT', 'ACCIDENT_REPORT', 'UNCONFIRMED_OBSERVATION', 'ENDED', 'RETRACTED')
           ELSE
               traffic_announcement_type IS NULL
           END);

UPDATE datex2
SET traffic_announcement_type =
    case
        when message like ('%Tilanne ohi.%') then 'ENDED'
        when message like ('%peruttu.%') then 'RETRACTED'
        when message like ('%Liikennetiedote onnettomuudesta%') then 'ACCIDENT_REPORT'
        when detailed_message_type = 'PRELIMINARY_ANNOUNCEMENT' then 'PRELIMINARY_ACCIDENT_REPORT'
        when detailed_message_type = 'UNCONFIRMED_OBSERVATION' then 'UNCONFIRMED_OBSERVATION'
        else 'GENERAL'
    end
WHERE situation_type = 'TRAFFIC_ANNOUNCEMENT';

---- Cleanup ----
alter table datex2 drop column if exists detailed_message_type;


