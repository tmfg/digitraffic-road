package fi.livi.digitraffic.tie.service.weather;

import static fi.livi.digitraffic.tie.model.roadstation.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.common.service.locking.CachedLockingService;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.AbstractVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.RoadStationUpdateService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.lotju.LotjuWeatherStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class WeatherStationUpdater  {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationUpdater.class);

    private final RoadStationUpdateService roadStationUpdateService;
    private final WeatherStationService weatherStationService;
    private final LotjuWeatherStationMetadataClientWrapper lotjuWeatherStationMetadataClientWrapper;
    private final DataStatusService dataStatusService;
    private final RoadStationService roadStationService;
    private final RoadStationSensorService roadStationSensorService;
    private final CachedLockingService cachedLockingService;

    @Autowired
    public WeatherStationUpdater(final RoadStationUpdateService roadStationUpdateService,
                                 final WeatherStationService weatherStationService,
                                 final LotjuWeatherStationMetadataClientWrapper lotjuWeatherStationMetadataClientWrapper,
                                 final LockingService lockingService,
                                 final DataStatusService dataStatusService,
                                 final RoadStationService roadStationService,
                                 final RoadStationSensorService roadStationSensorService) {
        this.roadStationUpdateService = roadStationUpdateService;
        this.weatherStationService = weatherStationService;
        this.lotjuWeatherStationMetadataClientWrapper = lotjuWeatherStationMetadataClientWrapper;
        this.dataStatusService = dataStatusService;
        this.roadStationService = roadStationService;
        this.roadStationSensorService = roadStationSensorService;
        this.cachedLockingService = lockingService.createCachedLockingService(this.getClass().getSimpleName());
    }

    public boolean updateWeatherStationAndSensors(final long lotjuId,
                                                  final MetadataUpdatedMessageDto.UpdateType updateType) {
        log.info("method=updateWeatherStationAndSensors start lotjuId={} type={}", lotjuId, updateType);
        if (updateType.isDelete()) {
            if (weatherStationService.updateStationToObsoleteWithLotjuId(lotjuId)) {
                dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA);
                return true;
            }
        } else {
            final TiesaaAsemaVO tiesaaAsema = lotjuWeatherStationMetadataClientWrapper.getTiesaaAsema(lotjuId);
            if (tiesaaAsema == null) {
                log.warn("method=updateWeatherStationAndSensors Weather station with lotjuId={} not found", lotjuId);
            } else if (updateWeatherStationAndSensors(tiesaaAsema)) {
                dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA);
                return true;
            }
        }
        return false;
    }

    /**
     * @param tiesaaAsema Weather station to update
     * @return true if data was updated
     */
    private boolean updateWeatherStationAndSensors(final TiesaaAsemaVO tiesaaAsema) {
        // Try to get lock for 10s and then gives up
        if (!cachedLockingService.lock(10000)) {
            log.error("method=updateWeatherStationAndSensors did not get the lock {}",
                    cachedLockingService.getLockInfoForLogging());
            return false;
        }
        try {
            log.debug("method=updateWeatherStationAndSensors got the lock");
            if (!validate(tiesaaAsema)) {
                return false;
            }
            final UpdateStatus updateStatus = weatherStationService.updateOrInsertWeatherStation(tiesaaAsema);
            final RoadStation weatherStation =
                roadStationService.findByTypeAndLotjuId(RoadStationType.WEATHER_STATION, tiesaaAsema.getId()).orElseThrow();
            final List<TiesaaLaskennallinenAnturiVO> anturit =
                lotjuWeatherStationMetadataClientWrapper.getTiesaaAsemanLaskennallisetAnturit(tiesaaAsema.getId());
            final List<Long> sensorslotjuIds = anturit.stream().map(AbstractVO::getId).collect(Collectors.toList());
            final Pair<Integer, Integer> result =
                roadStationSensorService.updateSensorsOfRoadStation(weatherStation.getId(),
                                                                    RoadStationType.WEATHER_STATION,
                                                                    sensorslotjuIds);

            return updateStatus.isUpdateOrInsert() || result.getLeft() > 0 || result.getRight() > 0;
        } finally {
            cachedLockingService.deactivate();
        }
    }

    /**
     * Updates Weather Stations
     */
    @PerformanceMonitor(maxWarnExcecutionTime = 60000, maxErrorExcecutionTime = 90000)
    public boolean updateWeatherStations() {
        final List<TiesaaAsemaVO> tiesaaAsemas = lotjuWeatherStationMetadataClientWrapper.getTiesaaAsemas();
        return updateWeatherStationsMetadata(tiesaaAsemas);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 30000) // Normally takes around 20s to fetch data and few seconds to update
    public int updateWeatherStationsStatuses() {
        final List<TiesaaAsemaVO> allTiesaaAsemas = lotjuWeatherStationMetadataClientWrapper.getTiesaaAsemas();

        int updated = 0;
        for (final TiesaaAsemaVO from : allTiesaaAsemas) {
            try {
                if (roadStationUpdateService.updateRoadStation(from)) {
                    updated++;
                }
            } catch (final Exception e) {
                log.error("method=updateWeatherStationsStatuses : Updating roadstation nimiFi=\"{}\" lotjuId={} naturalId={} keruunTila={} failed", from.getNimiFi(), from.getId(), from.getVanhaId(), from.getKeruunTila());
                throw e;
            }
        }
        return updated;
    }

    private boolean updateWeatherStationsMetadata(final List<TiesaaAsemaVO> tiesaaAsemas) {

        int updated = 0;
        int inserted = 0;

        final List<TiesaaAsemaVO> toUpdate =
            tiesaaAsemas.stream().filter(this::validate).collect(Collectors.toList());

        final Collection<?> invalid = CollectionUtils.subtract(tiesaaAsemas, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ToStringHelper.toStringFull(i)));

        final List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(TiesaaAsemaVO::getId).collect(Collectors.toList());
        final int obsoleted = roadStationUpdateService.obsoleteRoadStationsExcludingLotjuIds(RoadStationType.WEATHER_STATION, notToObsoleteLotjuIds);

        for (final TiesaaAsemaVO tsa : toUpdate) {
            final UpdateStatus result = weatherStationService.updateOrInsertWeatherStation(tsa);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }

        log.info("weatherStationsObsoletedCount={} WeatherStations", obsoleted);
        log.info("weatherStationsUpdatedCount={} WeatherStations", updated);
        log.info("weatherStationsInserted={} WeatherStations", inserted);
        if (!invalid.isEmpty()) {
            log.warn("invalidWeatherStationsFromLotjuCount={}", invalid.size());
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
