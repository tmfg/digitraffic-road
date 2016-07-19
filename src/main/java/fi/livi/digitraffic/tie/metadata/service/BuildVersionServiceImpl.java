package fi.livi.digitraffic.tie.metadata.service;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.jcabi.manifests.Manifests;

@Service
public class BuildVersionServiceImpl implements BuildVersionService{

    private static final Logger log = Logger.getLogger(BuildVersionServiceImpl.class);

    private static final String GIT_PROPERTIES  = "git.properties";
    private static final String GIT_REVISION_PROPERTY  = "git.commit.id.abbrev";

    @Override
    public String getAppVersion() {
        if (Manifests.exists("MetadataApplication-Version")) {
            return Manifests.read("MetadataApplication-Version");
        }
        return "DEV-BUILD";
    }

    @Override
    public String getAppBuildRevision() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
            return "" + properties.get(GIT_REVISION_PROPERTY);
        } catch (IOException e) {
            log.error("Failed to load git properties from file: " + GIT_PROPERTIES, e);
            return "?";
        }
    }

    @Override
    public String getAppFullVersion() {
        return getAppVersion() + "#" + getAppBuildRevision();
    }
}
