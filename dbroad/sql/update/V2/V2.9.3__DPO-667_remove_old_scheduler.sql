DELETE FROM qrtz_triggers WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_job_details WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_simple_triggers WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_blob_triggers WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_calendars WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_cron_triggers WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_fired_triggers WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_paused_trigger_grps WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_scheduler_state WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_simprop_triggers WHERE sched_name = 'schedulerFactoryBean';

DELETE FROM qrtz_locks WHERE sched_name = 'schedulerFactoryBean';
