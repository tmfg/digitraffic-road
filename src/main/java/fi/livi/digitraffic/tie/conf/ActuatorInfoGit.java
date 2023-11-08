package fi.livi.digitraffic.tie.conf;

import java.time.Instant;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.GitInfoContributor;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

@Component
public class ActuatorInfoGit extends GitInfoContributor {

    @Autowired
    public ActuatorInfoGit(final GitProperties properties) {
        super(properties);
    }

    @Override
    public void contribute(final Builder builder) {
        final Map<String, Object> map = generateContent();
        builder.withDetail(getDetailKey(), map);
    }

    @Override
    protected void postProcessContent(final Map<String, Object> content) {
        super.postProcessContent(content);
        // Add tags and buildTime. Default has branch, commit, commit.id, commit.time
        if ( StringUtils.isNotBlank(getProperties().get("tags")) ) {
            content.put("tags", getProperties().get("tags"));
        }
        if ( getProperties().getInstant("build.time") != null ) {
            final Instant buildTime = getProperties().getInstant("build.time");
            content.put("buildTime", buildTime);
        }
    }

    /**
     * @return the detail key where git info will be located
     */
    protected String getDetailKey() {
        return "git";
    }
}