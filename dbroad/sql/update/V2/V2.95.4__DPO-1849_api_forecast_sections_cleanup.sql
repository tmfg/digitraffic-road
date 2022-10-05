drop table forecast_section_coordinate;
drop table forecast_section_coordinate_list;

-- Run this to db manually after damon is up an running
-- UPDATE QRTZ_TRIGGERS
-- SET next_fire_time = 1
-- WHERE JOB_NAME in ('forecastSectionV1MetadataUpdateJob','forecastSectionV2MetadataUpdateJob');