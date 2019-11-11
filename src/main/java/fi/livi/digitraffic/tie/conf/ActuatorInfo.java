package fi.livi.digitraffic.tie.conf;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.dto.FlywayVersion;
import fi.livi.digitraffic.tie.metadata.service.FlywayService;

@Component
public class ActuatorInfo implements InfoContributor {

    private static final Logger log = LoggerFactory.getLogger(ActuatorInfo.class);
    private final FlywayService flywayService;

    @Autowired
    public ActuatorInfo(final FlywayService flywayService) {
        this.flywayService = flywayService;
    }

    @Override
    public void contribute(final Builder builder) {
        final Map<String, String> db = new HashMap<>();
        try {
            final FlywayVersion first = flywayService.getLatestVersion();
            System.out.println(first.getInstalledRank());
            System.out.println(first.getVersion());
            System.out.println(first.getChecksum());
            System.out.println(first.getDescription());
            System.out.println(first.getExecutionTime());
            System.out.println(first.getInstalledBy());
            System.out.println(first.getInstalledOn());
            System.out.println(first.getScript());
            System.out.println(first.getSuccess());
            System.out.println(first.getType());

            db.put("version", first.getVersion());
            db.put("installedOn", first.getInstalledOn().toString());
            db.put("success", first.getSuccess().toString());
        } catch (Exception e) {
            log.error("Could not get db version info", e);
            db.put("error", "Could not get db version info");
        }
        builder.withDetail("db", db);
    }
}