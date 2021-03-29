package fi.livi.digitraffic.tie.service.v2.datex2;

import java.util.Date;

import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.test.util.ReflectionTestUtils;

import fi.livi.digitraffic.tie.scheduler.RegionGeometryUpdateJob;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3RegionGeometryUpdateService;


public class V3RegionGeometryTestHelper {

    private final RegionGeometryUpdateJob regionGeometryUpdateJob;
    private final JobExecutionContext jobExecutionContext;

    public V3RegionGeometryTestHelper(final RegionGeometryGitClient regionGeometryGitClient, final V3RegionGeometryUpdateService v3RegionGeometryUpdateService, final DataStatusService dataStatusService) {
        regionGeometryUpdateJob = new RegionGeometryUpdateJob();
        ReflectionTestUtils.setField(regionGeometryUpdateJob, "regionGeometryGitClient", regionGeometryGitClient);
        ReflectionTestUtils.setField(regionGeometryUpdateJob, "v3RegionGeometryUpdateService", v3RegionGeometryUpdateService);
        ReflectionTestUtils.setField(regionGeometryUpdateJob, "dataStatusService", dataStatusService);
        jobExecutionContext = createJobExcecutionContext();
    }

    public void runUpdateJob() {
        regionGeometryUpdateJob.execute(jobExecutionContext);
    }

    private JobExecutionContext createJobExcecutionContext() {
        return new JobExecutionContext() {
            @Override
            public Scheduler getScheduler() {
                return null;
            }

            @Override
            public Trigger getTrigger() {
                return null;
            }

            @Override
            public Calendar getCalendar() {
                return null;
            }

            @Override
            public boolean isRecovering() {
                return false;
            }

            @Override
            public TriggerKey getRecoveringTriggerKey() throws IllegalStateException {
                return null;
            }

            @Override
            public int getRefireCount() {
                return 0;
            }

            @Override
            public JobDataMap getMergedJobDataMap() {
                return null;
            }

            @Override
            public JobDetail getJobDetail() {
                return new JobDetail() {
                    @Override
                    public JobKey getKey() {
                        return new JobKey("regionUpdateJob");
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }

                    @Override
                    public Class<? extends Job> getJobClass() {
                        return null;
                    }

                    @Override
                    public JobDataMap getJobDataMap() {
                        return null;
                    }

                    @Override
                    public boolean isDurable() {
                        return false;
                    }

                    @Override
                    public boolean isPersistJobDataAfterExecution() {
                        return false;
                    }

                    @Override
                    public boolean isConcurrentExectionDisallowed() {
                        return false;
                    }

                    @Override
                    public boolean requestsRecovery() {
                        return false;
                    }

                    @Override
                    public Object clone() {
                        return null;
                    }

                    @Override
                    public JobBuilder getJobBuilder() {
                        return null;
                    }
                };
            }

            @Override
            public Job getJobInstance() {
                return null;
            }

            @Override
            public Date getFireTime() {
                return null;
            }

            @Override
            public Date getScheduledFireTime() {
                return null;
            }

            @Override
            public Date getPreviousFireTime() {
                return null;
            }

            @Override
            public Date getNextFireTime() {
                return null;
            }

            @Override
            public String getFireInstanceId() {
                return null;
            }

            @Override
            public Object getResult() {
                return null;
            }

            @Override
            public void setResult(final Object result) {

            }

            @Override
            public long getJobRunTime() {
                return 0;
            }

            @Override
            public void put(final Object key, final Object value) {

            }

            @Override
            public Object get(final Object key) {
                return null;
            }
        };
    }
}
