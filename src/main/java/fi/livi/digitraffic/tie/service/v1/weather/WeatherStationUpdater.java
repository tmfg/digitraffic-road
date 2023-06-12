package fi.livi.digitraffic.tie.service.v1.weather;

import static fi.livi.digitraffic.tie.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

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

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.AbstractVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.RoadStationUpdateService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuWeatherStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class WeatherStationUpdater  {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationUpdater.class);

    private final RoadStationUpdateService roadStationUpdateService;
    private final WeatherStationService weatherStationService;
    private final LotjuWeatherStationMetadataClientWrapper lotjuWeatherStationMetadataClientWrapper;
    private final ClusteredLocker.ClusteredLock lock;
    private DataStatusService dataStatusService;
    private RoadStationService roadStationService;
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    public WeatherStationUpdater(final RoadStationUpdateService roadStationUpdateService,
                                 final WeatherStationService weatherStationService,
                                 final LotjuWeatherStationMetadataClientWrapper lotjuWeatherStationMetadataClientWrapper,
                                 final ClusteredLocker clusteredLocker,
                                 final DataStatusService dataStatusService,
                                 final RoadStationService roadStationService,
                                 final RoadStationSensorService roadStationSensorService) {
        this.roadStationUpdateService = roadStationUpdateService;
        this.weatherStationService = weatherStationService;
        this.lotjuWeatherStationMetadataClientWrapper = lotjuWeatherStationMetadataClientWrapper;
        this.lock = clusteredLocker.createClusteredLock(this.getClass().getSimpleName(), 10000);
        this.dataStatusService = dataStatusService;
        this.roadStationService = roadStationService;
        this.roadStationSensorService = roadStationSensorService;
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
        lock.lock();
        try {
            log.debug("method=updateWeatherStationAndSensors got the lock");
            if (!validate(tiesaaAsema)) {
                return false;
            }
            final UpdateStatus updateStatus = weatherStationService.updateOrInsertWeatherStation(tiesaaAsema);
            final RoadStation weatherStation =
                roadStationService.findByTypeAndLotjuId(RoadStationType.WEATHER_STATION, tiesaaAsema.getId());
            final List<TiesaaLaskennallinenAnturiVO> anturit =
                lotjuWeatherStationMetadataClientWrapper.getTiesaaAsemanLaskennallisetAnturit(tiesaaAsema.getId());
            final List<Long> sensorslotjuIds = anturit.stream().map(AbstractVO::getId).collect(Collectors.toList());
            final Pair<Integer, Integer> result =
                roadStationSensorService.updateSensorsOfRoadStation(weatherStation.getId(),
                                                                    RoadStationType.WEATHER_STATION,
                                                                    sensorslotjuIds);

            return updateStatus.isUpdateOrInsert() || result.getLeft() > 0 || result.getRight() > 0;
        } finally {
            lock.unlock();
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

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateWeatherStationsStatuses() {
        final List<TiesaaAsemaVO> allTiesaaAsemas = lotjuWeatherStationMetadataClientWrapper.getTiesaaAsemas();

        int updated = 0;
        for (TiesaaAsemaVO from : allTiesaaAsemas) {
            try {
                if (roadStationUpdateService.updateRoadStation(from)) {
                    updated++;
                }
            } catch (Exception e) {
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

        List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(TiesaaAsemaVO::getId).collect(Collectors.toList());
        int obsoleted = roadStationUpdateService.obsoleteRoadStationsExcludingLotjuIds(RoadStationType.WEATHER_STATION, notToObsoleteLotjuIds);

        for (TiesaaAsemaVO tsa : toUpdate) {
            UpdateStatus result = weatherStationService.updateOrInsertWeatherStation(tsa);
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
