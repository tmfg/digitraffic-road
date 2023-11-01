package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.service.trafficmessage.location.LocationMetadataUpdater;

@DisallowConcurrentExecution
public class LocationMetadataUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @Override
    @Transactional
    public void doExecute(final JobExecutionContext context) throws Exception {
        locationMetadataUpdater.findAndUpdate();
    }
}
