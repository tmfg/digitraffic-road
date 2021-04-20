--mark all situation records as ended if one is ended and it is wont exist in old query result
WITH to_deactivate as (
    select situation_id
    from (
             WITH LATEST_SITUATION AS (
                 SELECT DISTINCT ON (s.situation_id) s.situation_id
                                                   , s.id
                                                   , s.datex2_id
                 FROM datex2_situation s
                          INNER JOIN datex2 d2 ON d2.id = s.datex2_id
                 WHERE d2.situation_type IN ('TRAFFIC_ANNOUNCEMENT','EXEMPTED_TRANSPORT','WEIGHT_RESTRICTION','ROAD_WORK')
                 ORDER BY s.situation_id DESC, s.id DESC
             )
             SELECT latest_situation.situation_id
             FROM datex2 d
                      INNER JOIN latest_situation ON latest_situation.datex2_id = d.id
             WHERE exists(
                           SELECT null
                           FROM datex2_situation_record situation_record
                           WHERE situation_record.datex2_situation_id = latest_situation.id
                             AND situation_record.life_cycle_management_canceled IS NOT TRUE
                             AND (
                                       situation_record.validy_status = 'ACTIVE'
                                   OR (situation_record.validy_status = 'DEFINED_BY_VALIDITY_TIME_SPEC'
                                   AND (situation_record.overall_end_time IS NULL
                                       OR situation_record.overall_end_time > current_timestamp - 0 * interval '1 hour'
                                           )
                                           ) OR (situation_record.validy_status = 'SUSPENDED'
                                   AND situation_record.version_time > current_timestamp - 0 * interval '1 hour'
                                           )
                               )
                       )
         ) as uusi
    where not exists(
            select null
            from (
                     SELECT s.situation_id
                     FROM datex2 d
                              INNER JOIN datex2_situation s ON s.datex2_id = d.id
                     WHERE d.id IN (
                         SELECT datex2_id
                         FROM (
                                  SELECT ROW_NUMBER() OVER (PARTITION BY situation.SITUATION_ID
                                      ORDER BY record.version_time DESC NULLS LAST,
                                          record.overall_end_time DESC NULLS FIRST,
                                          record.id DESC) AS rnum
                                       , d.publication_time
                                       , d.id             AS datex2_id
                                       , record.validy_status
                                       , record.overall_end_time
                                  FROM DATEX2 d
                                           INNER JOIN datex2_situation situation ON situation.datex2_id = d.id
                                           INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id
                                  WHERE d.situation_type in ('TRAFFIC_ANNOUNCEMENT','EXEMPTED_TRANSPORT','WEIGHT_RESTRICTION','ROAD_WORK')
                              ) disorder
                         WHERE rnum = 1
                           AND (disorder.overall_end_time IS NULL OR
                                disorder.overall_end_time > current_timestamp - 0 * interval '1 hour')
                           AND (
                                     disorder.validy_status <> 'SUSPENDED'
                                 OR (disorder.validy_status = 'SUSPENDED' AND disorder.overall_end_time IS NOT null)
                             )
                     )
                 ) as vanha
            where uusi.situation_id = vanha.situation_id
        )
)
update datex2_situation_record tgt
set validy_status = 'DEFINED_BY_VALIDITY_TIME_SPEC',
    overall_end_time = (select overall_end_time
                        from datex2_situation_record src
                        where src.datex2_situation_id = tgt.datex2_situation_id
                          AND src.validy_status = 'DEFINED_BY_VALIDITY_TIME_SPEC'
                          AND tgt.validy_status <> 'ACTIVE')
where tgt.validy_status = 'ACTIVE'
  AND exists (
        select null from datex2_situation s
        where tgt.datex2_situation_id = s.id
          AND s.situation_id in (select situation_id from to_deactivate)
    )
RETURNING id as datex2_situation__id;

-- update all situation records to canceled where cancel true exists
update DATEX2_SITUATION_RECORD tgt
set life_cycle_management_canceled = true
where exists(
    select null
    from datex2
    inner join datex2_situation d2s on datex2.id = d2s.datex2_id
    where message like ('%<cancel>true</cancel>%')
      and tgt.datex2_situation_id = d2s.id
);