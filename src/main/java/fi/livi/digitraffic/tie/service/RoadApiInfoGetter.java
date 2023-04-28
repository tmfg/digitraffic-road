package fi.livi.digitraffic.tie.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.documentation.RoadApiInfo;

@Component
public class RoadApiInfoGetter {

    private final LocalizedMessageSource localizedMessageSource;

    private final BuildVersionService buildVersionResolver;

    @Autowired
    public RoadApiInfoGetter(final LocalizedMessageSource localizedMessageSource,
                             final BuildVersionService buildVersionResolver) {
        this.localizedMessageSource = localizedMessageSource;
        this.buildVersionResolver = buildVersionResolver;
    }

    public RoadApiInfo getApiInfo() {
        return new RoadApiInfo(localizedMessageSource, buildVersionResolver);
    }
}
