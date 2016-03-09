package fi.livi.digitraffic.tie.service;

import com.jcabi.manifests.Manifests;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class BuildVersionServiceImpl implements BuildVersionService{

    private static final Logger LOG = Logger.getLogger(BuildVersionServiceImpl.class);

    @Override
    public String getAppVersion() {
        if (Manifests.exists("MetadataApplication-Version")) {
            return Manifests.read("MetadataApplication-Version");
        }
        return "DEV-BUILD";
    }

    @Override
    public String getAppBuildRevision() {
        if (Manifests.exists("MetadataApplication-Build")) {
            return Manifests.read("MetadataApplication-Build");
        }
        return "X";
    }

    @Override
    public String getAppFullVersion() {
        return getAppVersion() + "-" + getAppBuildRevision();
    }
}
