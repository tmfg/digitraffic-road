package fi.livi.digitraffic.tie.service.v1.weather;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.AbstractRoadStationSensorUpdater;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuWeatherStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class WeatherStationSensorUpdater extends AbstractRoadStationSensorUpdater {
    private static final Logger log = LoggerFactory.getLogger(WeatherStationSensorUpdater.class);

    private final LotjuWeatherStationMetadataClientWrapper lotjuWeatherStationMetadataClientWrapper;

    @Autowired
    public WeatherStationSensorUpdater(final RoadStationSensorService roadStationSensorService,
                                       final LotjuWeatherStationMetadataClientWrapper lotjuWeatherStationMetadataClientWrapper) {
        super(roadStationSensorService);
        this.lotjuWeatherStationMetadataClientWrapper = lotjuWeatherStationMetadataClientWrapper;
    }

    /**
     * Updates all available weather road station sensors
     */
    public boolean updateRoadStationSensors() {
        log.info("method=updateRoadStationSensors Update weather RoadStationSensors start");

        // Update available RoadStationSensors types to db
        final List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis =
                lotjuWeatherStationMetadataClientWrapper.getAllTiesaaLaskennallinenAnturis();

        boolean updated = updateAllRoadStationSensors(allTiesaaLaskennallinenAnturis);
        log.info("method=updateRoadStationSensors Update weather RoadStationSensors end");
        return updated;
    }

    private boolean updateAllRoadStationSensors(final List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis) {

        int updated = 0;
        int inserted = 0;

        final List<TiesaaLaskennallinenAnturiVO> toUpdate =
            allTiesaaLaskennallinenAnturis.stream().filter(WeatherStationSensorUpdater::validate).collect(Collectors.toList());

        final Collection<TiesaaLaskennallinenAnturiVO> invalid = CollectionUtils.subtract(allTiesaaLaskennallinenAnturis, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ToStringHelper.toStringFull(i)));

        final List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(TiesaaLaskennallinenAnturiVO::getId).collect(Collectors.toList());
        final int obsoleted = roadStationSensorService.obsoleteSensorsExcludingLotjuIds(RoadStationType.WEATHER_STATION, notToObsoleteLotjuIds);

        for (TiesaaLaskennallinenAnturiVO anturi : toUpdate) {
            final UpdateStatus result = roadStationSensorService.updateOrInsert(anturi);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }

        log.info("method=updateAllRoadStationSensors roadStationSensors obsoletedCount={} roadStationType={}", obsoleted, RoadStationType.WEATHER_STATION);
        log.info("method=updateAllRoadStationSensors roadStationSensors updatedCount={} roadStationType={}", updated, RoadStationType.WEATHER_STATION);
        log.info("method=updateAllRoadStationSensors roadStationSensors insertedCount={} roadStationType={}", inserted, RoadStationType.WEATHER_STATION);

        if (!invalid.isEmpty()) {
            log.warn("method=updateAllRoadStationSensors roadStationSensors invalidCount={} roadStationType={}", invalid.size(), RoadStationType.WEATHER_STATION);
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private static boolean validate(final TiesaaLaskennallinenAnturiVO anturi) {
        return anturi.getId() != null && anturi.getVanhaId() != null;
    }
}
