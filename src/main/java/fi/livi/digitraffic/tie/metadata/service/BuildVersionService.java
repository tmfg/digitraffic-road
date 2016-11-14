package fi.livi.digitraffic.tie.metadata.service;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jcabi.manifests.Manifests;

@Service
public class BuildVersionService {

    private static final Logger log = LoggerFactory.getLogger(BuildVersionService.class);

    private static final String GIT_PROPERTIES  = "git.properties";
    private static final String GIT_TAGS_PROPERTY  = "git.tags";

    public String getAppVersion() {
        if (Manifests.exists("MetadataApplication-Version")) {
            return "1.0"; //Manifests.read("MetadataApplication-Version");
        }
        return "DEV-BUILD";
    }

    public String getAppBuildRevision() {
        final Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(GIT_PROPERTIES));
            return "" + properties.get(GIT_TAGS_PROPERTY);
        } catch (final IOException e) {
            log.error("Failed to load git properties from file: " + GIT_PROPERTIES, e);
            return "?";
        }
    }

    public String getAppFullVersion() {
        return getAppVersion() + "#" + getAppBuildRevision();
    }
}
