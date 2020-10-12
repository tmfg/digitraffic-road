UPDATE datex2
SET detailed_message_type =
        case
            when message SIMILAR TO ('%(Liikennetiedote.|Liikennetiedote |Tilanne ohi.)%') then 'TRAFFIC_ANNOUNCEMENT' -- OK 'Liikennetiedote.|Liikennetiedote onnettomuudesta.'
            when message LIKE ('%Ensitiedote %') then 'PRELIMINARY_ANNOUNCEMENT' -- OK 'Ensitiedote liikenneonnettomuudesta.'
            when message LIKE ('%Erikoiskuljetus.%') then 'EXEMPTED_TRANSPORT'
            when message LIKE ('%Vahvistamaton havainto.%') then 'UNCONFIRMED_OBSERVATION'-- OK
            when message LIKE ('%Painorajoitus.%') then 'WEIGHT_RESTRICTION' -- OK
            when message SIMILAR TO ('%(Tietyö.|Tietyövaihe.)%') then 'ROADWORK' -- Tietyö.|Tietyövaihe.
            else 'UNKNOWN'
            end;
