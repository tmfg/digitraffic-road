package fi.livi.digitraffic.tie.conf;

import java.util.Map;

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
        // Add dirty and tags to actuator
        map.put("dirty", getProperties().get("dirty"));
        map.put("tags", getProperties().get("tags"));
        builder.withDetail("git", map);
    }
}