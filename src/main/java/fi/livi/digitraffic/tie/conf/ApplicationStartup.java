package fi.livi.digitraffic.tie.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.service.BuildVersionResolver;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);

    @Value("${app.type}")
    private String appType;

    @Autowired
    private BuildVersionResolver buildVersionResolver;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent applicationReadyEvent) {
        log.info("startedApp=RoadApplication appType={} version: {}", appType, buildVersionResolver.getAppFullVersion());
    }

    @EventListener
    public void onShutdown(final ContextStoppedEvent event) {
        log.info("stoppedApp=RoadApplication appType={} version: {}", appType, buildVersionResolver.getAppFullVersion());
    }
}
