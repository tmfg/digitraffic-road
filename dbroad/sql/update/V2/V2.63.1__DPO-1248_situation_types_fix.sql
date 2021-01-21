UPDATE datex2
SET situation_type = 'EXEMPTED_TRANSPORT',
    traffic_announcement_type = null
where situation_type = 'TRAFFIC_ANNOUNCEMENT'
  and traffic_announcement_type = 'GENERAL'
  and message LIKE ('%Erikoiskuljetus%');
