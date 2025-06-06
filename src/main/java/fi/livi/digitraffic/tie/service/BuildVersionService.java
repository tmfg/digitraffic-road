package fi.livi.digitraffic.tie.service;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;

@Service
public class BuildVersionService {

    private static final Logger log = LoggerFactory.getLogger(BuildVersionService.class);

    private static final String GIT_PROPERTIES  = "git.properties";
    private static final String GIT_COMMIT_ID_HASH = "git.commit.id.abbrev";
    private static final String GIT_BUILD_TIME  = "git.build.time";
    private static final String GIT_TAGS  = "git.tags";
    private static final String GIT_BRANCH  = "git.branch";

    private String getAppBuildRevision() {
        final String tag = readProperty(GIT_COMMIT_ID_HASH);
        if (StringUtils.isNotBlank(tag)) {
            return tag;
        }
        return "?";
    }

    private String getAppBuildTime() {
        final String time = readProperty(GIT_BUILD_TIME);
        if (StringUtils.isNotBlank(time)) {
            return time;
        }
        return "?";
    }

    private String getAppTags() {
        final String tags = readProperty(GIT_TAGS);
        if (StringUtils.isNotBlank(tags) && !StringUtils.contains(tags, "{")) {
            return tags;
        }
        return "DEV";
    }

    private String getAppBranch() {
        final String branch = readProperty(GIT_BRANCH);
        if (StringUtils.isNotBlank(branch)) {
            return branch;
        }
        return "?";
    }

    @NotTransactionalServiceMethod
    public String getAppFullVersion() {
        return String.format("Branch: %s, tag: %s #%s @ %s", getAppBranch(), getAppTags(), getAppBuildRevision(), getAppBuildTime());
    }

    private String readProperty(final String property) {
        final Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(GIT_PROPERTIES));
            final String prop = (String) properties.get(property);
            if (StringUtils.isNotBlank(prop)) {
                return prop;
            }
            return null;
        } catch (final IOException ioe) {
            log.error("Failed to load git properties from file: " + GIT_PROPERTIES, ioe);
            return null;
        }
    }
}
