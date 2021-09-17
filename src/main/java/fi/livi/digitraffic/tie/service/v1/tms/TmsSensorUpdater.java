package fi.livi.digitraffic.tie.service.v1.tms;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.AbstractRoadStationSensorUpdater;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.UpdateStatus;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class TmsSensorUpdater extends AbstractRoadStationSensorUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsSensorUpdater.class);

    private DataStatusService dataStatusService;
    private final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper;

    @Autowired
    public TmsSensorUpdater(final RoadStationSensorService roadStationSensorService,
                            final DataStatusService dataStatusService,
                            final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper) {
        super(roadStationSensorService);
        this.dataStatusService = dataStatusService;
        this.lotjuTmsStationMetadataClientWrapper = lotjuTmsStationMetadataClientWrapper;
    }

    /**
     * Updates all available tms sensors
     */
    public boolean updateTmsSensors() {
        log.info("method=updateTmsSensors start");

        // Update available RoadStationSensors types to db
        final List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis =
                lotjuTmsStationMetadataClientWrapper.getAllLamLaskennallinenAnturis();

        boolean updated = updateAllRoadStationSensors(allLamLaskennallinenAnturis);
        log.info("Update TMS RoadStationSensors end");
        return updated;
    }

    public boolean updateTmsSensor(final long lotjuId,
                                   final MetadataUpdatedMessageDto.UpdateType updateType) {
        log.info("method=updateTmsSensor lotjuId={}", lotjuId);

        if (updateType.isDelete()) {
            if (roadStationSensorService.obsoleteSensor(lotjuId, RoadStationType.TMS_STATION)) {
                dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA);
                return true;
            }
        } else {
            final LamLaskennallinenAnturiVO anturi = lotjuTmsStationMetadataClientWrapper.getLamLaskennallinenAnturi(lotjuId);
            if (anturi == null) {
                log.warn("method=updateTmsSensor Weather sensor with lotjuId={} not found", lotjuId);
            } else if ( roadStationSensorService.updateOrInsert(anturi).isUpdateOrInsert() ) {
                dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA);
                return true;
            }
        }
        return false;
    }

    private boolean updateAllRoadStationSensors(final List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis) {

        int updated = 0;
        int inserted = 0;

        final List<LamLaskennallinenAnturiVO> toUpdate =
            allLamLaskennallinenAnturis.stream().filter(TmsSensorUpdater::validate).collect(Collectors.toList());

        final List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(LamLaskennallinenAnturiVO::getId).collect(Collectors.toList());
        final int obsoleted = roadStationSensorService.obsoleteSensorsExcludingLotjuIds(RoadStationType.TMS_STATION, notToObsoleteLotjuIds);

        final Collection<LamLaskennallinenAnturiVO> invalid = CollectionUtils.subtract(allLamLaskennallinenAnturis, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ToStringHelper.toStringFull(i)));

        for (LamLaskennallinenAnturiVO anturi : toUpdate) {
            UpdateStatus result = roadStationSensorService.updateOrInsert(anturi);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }

        log.info("method=updateAllRoadStationSensors roadStationSensors obsoletedCount={} roadStationType={}", obsoleted, RoadStationType.TMS_STATION);
        log.info("method=updateAllRoadStationSensors roadStationSensors updatedCount={} roadStationType={}", updated, RoadStationType.TMS_STATION);
        log.info("method=updateAllRoadStationSensors roadStationSensors insertedCount={} roadStationType={}", inserted, RoadStationType.TMS_STATION);

        if (!invalid.isEmpty()) {
            log.warn("method=updateAllRoadStationSensors roadStationSensors invalidCount={} roadStationType={}", invalid.size(), RoadStationType.TMS_STATION);
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private static boolean validate(LamLaskennallinenAnturiVO anturi) {
        return anturi.getId() != null && anturi.getVanhaId() != null;
    }
}
