package fi.livi.digitraffic.tie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import fi.livi.digitraffic.tie.service.lam.LamStationUpdater;

@SpringBootApplication
public class MetadataApplication {
    public static void main(String[] args) {
        final ConfigurableApplicationContext app = SpringApplication.run(MetadataApplication.class, args);

        final LamStationUpdater updater = app.getBean(LamStationUpdater.class);
        updater.updateLamStations();
    }
}
