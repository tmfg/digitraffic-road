package fi.livi.digitraffic.tie.service.tms;

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
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.AbstractVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
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
import fi.livi.digitraffic.tie.service.lotju.LotjuTmsStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class TmsStationUpdater {

    private static final Logger log = LoggerFactory.getLogger(TmsStationUpdater.class);

    private final RoadStationUpdateService roadStationUpdateService;
    private final CachedLockingService cachedLockingService;
    private final DataStatusService dataStatusService;
    private final RoadStationService roadStationService;
    private final TmsStationService tmsStationService;
    private final RoadStationSensorService roadStationSensorService;
    private final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper;

    @Autowired
    public TmsStationUpdater(final RoadStationUpdateService roadStationUpdateService,
                             final RoadStationService roadStationService,
                             final TmsStationService tmsStationService,
                             final RoadStationSensorService roadStationSensorService,
                             final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper,
                             final LockingService lockingService,
                             final DataStatusService dataStatusService) {
        this.roadStationUpdateService = roadStationUpdateService;
        this.roadStationService = roadStationService;
        this.tmsStationService = tmsStationService;
        this.roadStationSensorService = roadStationSensorService;
        this.lotjuTmsStationMetadataClientWrapper = lotjuTmsStationMetadataClientWrapper;
        this.cachedLockingService = lockingService.createCachedLockingService(this.getClass().getSimpleName());
        this.dataStatusService = dataStatusService;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 60000)
    public boolean updateTmsStations() {
        final List<LamAsemaVO> asemas = lotjuTmsStationMetadataClientWrapper.getLamAsemas();
        if (updateTmsStationsMetadata(asemas)) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
            return true;
        }
        return false;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000) // Fetching data takes normally around 10s and updating few seconds
    public int updateTmsStationsStatuses() {
        final List<LamAsemaVO> allLams = lotjuTmsStationMetadataClientWrapper.getLamAsemas();

        int updated = 0;
        for(final LamAsemaVO from : allLams) {
            try {
                if (roadStationUpdateService.updateRoadStation(from)) {
                    updated++;
                }
            } catch (final Exception e) {
                log.error("method=updateTmsStationsStatuses : Updating roadstation nimiFi=\"{}\" lotjuId={} naturalId={} keruunTila={} failed", from.getNimiFi(), from.getId(), from.getVanhaId(), from.getKeruunTila());
                throw e;
            }
        }
        if (updated > 0) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
        }
        return updated;
    }

    private boolean updateTmsStationsMetadata(final List<LamAsemaVO> lamAsemas) {

        int updated = 0;
        int inserted = 0;

        final List<LamAsemaVO> toUpdate =
            lamAsemas.stream().filter(this::validate).collect(Collectors.toList());

        final List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(LamAsemaVO::getId).collect(Collectors.toList());
        final int obsoleted = roadStationUpdateService.obsoleteRoadStationsExcludingLotjuIds(RoadStationType.TMS_STATION, notToObsoleteLotjuIds);
        log.info("Not to obsolete lotju ids {}", notToObsoleteLotjuIds);

        final Collection<?> invalid = CollectionUtils.subtract(lamAsemas, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ToStringHelper.toStringFull(i)));

        for (final LamAsemaVO tsa : toUpdate) {
            final UpdateStatus result = tmsStationService.updateOrInsertTmsStation(tsa);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }


        log.info("method=updateTmsStationsMetadata obsoleteCount={} updateCount={} insertCount={}", obsoleted, updated, inserted);
        if (!invalid.isEmpty()) {
            log.warn("method=updateTmsStationsMetadata Invalid TmsStations from lotju invalidCount={}", invalid.size());
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private boolean validate(final LamAsemaVO lamAsema) {
        final boolean valid = lamAsema.getVanhaId() != null;
        if(!valid && !isPermanentlyDeletedKeruunTila(lamAsema.getKeruunTila())) {
            log.error("{} is invalid: has null vanhaId", ToStringHelper.toString(lamAsema));
        }
        return valid;
    }

    public boolean updateTmsStationAndSensors(final long tmsStationLotjuId,
                                              final MetadataUpdatedMessageDto.UpdateType updateType) {
        log.info("method=updateTmsStationAndSensors start lotjuId={} type={}", tmsStationLotjuId, updateType);
        if ( updateType.isDelete() ) {
            if (tmsStationService.updateStationToObsoleteWithLotjuId(tmsStationLotjuId)) {
                dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
                return true;
            }
        } else {
            final LamAsemaVO lamAsema = lotjuTmsStationMetadataClientWrapper.getLamAsema(tmsStationLotjuId);
            if ( lamAsema == null ) {
                log.warn("method=updateTmsStation TMS stationg with lotjuId={} not found", tmsStationLotjuId);
            } else if ( updateTmsStationAndSensors(lamAsema) ) {
                dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
                return true;
            }
        }
        return false;
    }

    /**
     * @param lamAsema TMS station to update
     * @return true if data was updated
     */
    private boolean updateTmsStationAndSensors(final LamAsemaVO lamAsema) {
        // Try to get lock for 10s and then gives up
        if (!cachedLockingService.lock(10000)) {
            log.error("method=updateTmsStationAndSensors did not get the lock {}", cachedLockingService.getLockInfoForLogging());
            return false;
        }
        try {
            log.debug("method=updateTmsStationAndSensors got the lock {}", cachedLockingService.getLockInfoForLogging());
            if (!validate(lamAsema)) {
                return false;
            }
            final UpdateStatus updateStatus = tmsStationService.updateOrInsertTmsStation(lamAsema);
            final RoadStation tmsStation =
                roadStationService.findByTypeAndLotjuId(RoadStationType.TMS_STATION, lamAsema.getId());
            final List<LamLaskennallinenAnturiVO> anturit =
                lotjuTmsStationMetadataClientWrapper.getLamAsemanLaskennallisetAnturit(lamAsema.getId());
            final List<Long> sensorslotjuIds = anturit.stream().map(AbstractVO::getId).collect(Collectors.toList());
            final Pair<Integer, Integer> result =
                roadStationSensorService.updateSensorsOfRoadStation(tmsStation.getId(),
                                                                    RoadStationType.TMS_STATION,
                                                                    sensorslotjuIds);

            return updateStatus.isUpdateOrInsert() || result.getLeft() > 0 || result.getRight() > 0;
        } finally {
            cachedLockingService.deactivate();
        }
    }

}
