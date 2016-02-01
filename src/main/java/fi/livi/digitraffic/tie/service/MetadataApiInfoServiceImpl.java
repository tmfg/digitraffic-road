package fi.livi.digitraffic.tie.service;

import fi.livi.digitraffic.tie.documentation.MetadataApiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.service.ApiInfo;

@Service
public class MetadataApiInfoServiceImpl implements MetadataApiInfoService {

    @Autowired
    MessageService messageService;

    @Autowired
    BuildVersionService buildVersionService;

    @Override
    public ApiInfo getApiInfo() {
        return new MetadataApiInfo(messageService, buildVersionService);
    }
}
