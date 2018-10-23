package fi.livi.digitraffic.tie.metadata.service.weather;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.AbstractRoadStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.UpdateStatus;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaLaskennallinenAnturiVO;

@Service
public class WeatherStationSensorUpdater extends AbstractRoadStationSensorUpdater {
    private static final Logger log = LoggerFactory.getLogger(WeatherStationSensorUpdater.class);

    private final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService;

    @Autowired
    public WeatherStationSensorUpdater(final RoadStationSensorService roadStationSensorService,
                                       final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService) {
        super(roadStationSensorService);
        this.lotjuWeatherStationMetadataService = lotjuWeatherStationMetadataService;
    }

    /**
     * Updates all available weather road station sensors
     */
    public boolean updateRoadStationSensors() {
        log.info("Update weather RoadStationSensors start");

        // Update available RoadStationSensors types to db
        final List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis =
                lotjuWeatherStationMetadataService.getAllTiesaaLaskennallinenAnturis();

        boolean fixedLotjuIds = roadStationSensorService.fixWeatherStationSensorsWithoutLotjuId(
            allTiesaaLaskennallinenAnturis.stream().filter(WeatherStationSensorUpdater::validate).collect(Collectors.toList()));

        boolean updated = updateAllRoadStationSensors(allTiesaaLaskennallinenAnturis);
        log.info("Update weather RoadStationSensors end");
        return fixedLotjuIds || updated;
    }

    private boolean updateAllRoadStationSensors(final List<TiesaaLaskennallinenAnturiVO> allTiesaaLaskennallinenAnturis) {

        int updated = 0;
        int inserted = 0;

        final List<TiesaaLaskennallinenAnturiVO> toUpdate =
            allTiesaaLaskennallinenAnturis.stream().filter(WeatherStationSensorUpdater::validate).collect(Collectors.toList());

        final Collection invalid = CollectionUtils.subtract(allTiesaaLaskennallinenAnturis, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ReflectionToStringBuilder.toString(i)));

        List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(TiesaaLaskennallinenAnturiVO::getId).collect(Collectors.toList());
        int obsoleted = roadStationSensorService.obsoleteSensorsExcludingLotjuIds(RoadStationType.TMS_STATION, notToObsoleteLotjuIds);

        for (TiesaaLaskennallinenAnturiVO anturi : toUpdate) {
            final UpdateStatus result = roadStationSensorService.updateOrInsert(anturi);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }

        log.info("roadStationSensorsObsoleted={}", obsoleted);
        log.info("roadStationSensorsUpdated={}", updated);
        log.info("roadStationSensorsInserted={}", inserted);
        if (!invalid.isEmpty()) {
            log.warn("invalidWeatherSensorsFromLotjuCount={}", invalid.size());
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private static boolean validate(final TiesaaLaskennallinenAnturiVO anturi) {
        return anturi.getId() != null && anturi.getVanhaId() != null;
    }
}
