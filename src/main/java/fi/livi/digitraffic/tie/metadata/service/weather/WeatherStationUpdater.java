package fi.livi.digitraffic.tie.metadata.service.weather;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.UpdateStatus;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;

@Service
public class WeatherStationUpdater  {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationUpdater.class);

    private final RoadStationService roadStationService;
    private final WeatherStationService weatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService;

    @Autowired
    public WeatherStationUpdater(final RoadStationService roadStationService,
                                 final WeatherStationService weatherStationService,
                                 final StaticDataStatusService staticDataStatusService,
                                 final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService) {
        this.roadStationService = roadStationService;
        this.weatherStationService = weatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuWeatherStationMetadataService = lotjuWeatherStationMetadataService;
    }

    /**
     * Updates Weather Stations
     */
    @PerformanceMonitor(maxWarnExcecutionTime = 60000, maxErroExcecutionTime = 90000)
    public boolean updateWeatherStations() {
        log.info("Update WeatherStations start");

        if (!lotjuWeatherStationMetadataService.isEnabled()) {
            log.warn("Not updating WeatherStations metadata because LotjuWeatherStationService not enabled");
            return false;
        }

        final List<TiesaaAsemaVO> tiesaaAsemas = lotjuWeatherStationMetadataService.getTiesaaAsemmas();

        final boolean updateStaticDataStatus = updateWeatherStationsMetadata(tiesaaAsemas);
        updateRoasWeatherStationStaticDataStatus(updateStaticDataStatus);

        log.info("Update WeatherStations end");
        return updateStaticDataStatus;
    }

    private void updateRoasWeatherStationStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.ROAD_WEATHER, updateStaticDataStatus);
    }

    private boolean updateWeatherStationsMetadata(final List<TiesaaAsemaVO> tiesaaAsemas) {

        weatherStationService.fixNullLotjuIds(tiesaaAsemas);

        int updated = 0;
        int inserted = 0;

        final List<TiesaaAsemaVO> toUpdate =
            tiesaaAsemas.stream().filter(tsa -> validate(tsa)).collect(Collectors.toList());

        final Collection invalid = CollectionUtils.subtract(tiesaaAsemas, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ReflectionToStringBuilder.toString(i)));

        List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(TiesaaAsemaVO::getId).collect(Collectors.toList());
        int obsoleted = roadStationService.obsoleteRoadStationsExcludingLotjuIds(RoadStationType.WEATHER_STATION, notToObsoleteLotjuIds);

        for (TiesaaAsemaVO tsa : toUpdate) {
            UpdateStatus result = weatherStationService.updateOrInsertWeatherStation(tsa);
            if (result == UpdateStatus.INSERTED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }

        log.info("Obsoleted {} WeatherStations", obsoleted);
        log.info("Updated {} WeatherStations", updated);
        log.info("Inserted {} WeatherStations", inserted);
        if (!invalid.isEmpty()) {
            log.warn("Invalid WeatherStations from lotju {}", invalid.size());
        }
        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private boolean validate(final TiesaaAsemaVO tsa) {
        final boolean valid = tsa.getVanhaId() != null;
        if (!valid && !isPermanentlyDeletedKeruunTila(tsa.getKeruunTila())) {
            log.error("{} is invalid: has null vanhaId", ToStringHelper.toString(tsa));
        }
        return valid;
    }
}
