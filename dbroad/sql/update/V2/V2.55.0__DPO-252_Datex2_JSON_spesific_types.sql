ALTER TABLE datex2 ADD COLUMN IF NOT EXISTS detailed_message_type TEXT;
CREATE INDEX IF NOT EXISTS datex2_detailed_type_import_i ON datex2(detailed_message_type, import_date);

-- WEIGHT_RESTRICTION(Datex2MessageType.WEIGHT_RESTRICTION, "Painorajoitus");
-- UNKNOWN(Datex2MessageType.TRAFFIC_INCIDENT, "Datex2DetailedMessageType.UNKNOWN.random");
-- TRAFFIC_ANNOUNCEMENT(Datex2MessageType.TRAFFIC_INCIDENT, "Liikennetiedote"),
-- PRELIMINARY_ANNOUNCEMENT(Datex2MessageType.TRAFFIC_INCIDENT, "Ensitiedote"),
-- EXEMPTED_TRANSPORT(Datex2MessageType.TRAFFIC_INCIDENT, "Erikoiskuljetus"),
-- UNCONFIRMED_OBSERVATION(Datex2MessageType.TRAFFIC_INCIDENT, "Vahvistamaton havainto"),
-- ROADWORK(Datex2MessageType.ROADWORK, "Tietyö"),

UPDATE datex2
SET detailed_message_type =
        case
            when message LIKE ('%Tietyö%') then 'ROADWORK'
            when message LIKE ('%Painorajoitus%') then 'WEIGHT_RESTRICTION'
            when message LIKE ('%Liikennetiedote%') then 'TRAFFIC_ANNOUNCEMENT'
            when message LIKE ('%Ensitiedote%') then 'PRELIMINARY_ANNOUNCEMENT'
            when message LIKE ('%Erikoiskuljetus%') then 'EXEMPTED_TRANSPORT'
            when message LIKE ('%Vahvistamaton havainto%') then 'UNCONFIRMED_OBSERVATION'
            else 'UNKNOWN'
            end;

ALTER TABLE datex2 ALTER COLUMN detailed_message_type SET NOT NULL;

ALTER TABLE datex2
    ADD CONSTRAINT datex2_detailed_type_check CHECK (detailed_message_type IN
        ('TRAFFIC_ANNOUNCEMENT', 'PRELIMINARY_ANNOUNCEMENT', 'EXEMPTED_TRANSPORT', 'UNCONFIRMED_OBSERVATION', 'ROADWORK', 'WEIGHT_RESTRICTION', 'UNKNOWN')
    );
