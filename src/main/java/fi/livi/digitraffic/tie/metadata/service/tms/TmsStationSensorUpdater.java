package fi.livi.digitraffic.tie.metadata.service.tms;

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
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;

@Service
public class TmsStationSensorUpdater extends AbstractRoadStationSensorUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorUpdater.class);

    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;

    @Autowired
    public TmsStationSensorUpdater(final RoadStationSensorService roadStationSensorService,
                                   final LotjuTmsStationMetadataService lotjuTmsStationMetadataService) {
        super(roadStationSensorService);
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
    }

    /**
     * Updates all available tms road station sensors
     */
    public boolean updateRoadStationSensors() {
        log.info("Update TMS RoadStationSensors start");

        if (!lotjuTmsStationMetadataService.isEnabled()) {
            log.warn("Not updating TMS stations sensors because LotjuTmsStationMetadataService not enabled");
            return false;
        }

        // Update available RoadStationSensors types to db
        List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis =
                lotjuTmsStationMetadataService.getAllLamLaskennallinenAnturis();

        boolean fixedLotjuIds = roadStationSensorService.fixTmsStationSensorsWithoutLotjuId(
            allLamLaskennallinenAnturis.stream().filter(TmsStationSensorUpdater::validate).collect(Collectors.toList()));

        boolean updated = updateAllRoadStationSensors(allLamLaskennallinenAnturis);
        log.info("Update TMS RoadStationSensors end");
        return fixedLotjuIds || updated;
    }

    private boolean updateAllRoadStationSensors(final List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis) {

        int updated = 0;
        int inserted = 0;

        final List<LamLaskennallinenAnturiVO> toUpdate =
            allLamLaskennallinenAnturis.stream().filter(TmsStationSensorUpdater::validate).collect(Collectors.toList());

        List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(LamLaskennallinenAnturiVO::getId).collect(Collectors.toList());
        int obsoleted = roadStationSensorService.obsoleteSensorsExcludingLotjuIds(RoadStationType.TMS_STATION, notToObsoleteLotjuIds);

        final Collection invalid = CollectionUtils.subtract(allLamLaskennallinenAnturis, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ReflectionToStringBuilder.toString(i)));

        for (LamLaskennallinenAnturiVO anturi : toUpdate) {
            UpdateStatus result = roadStationSensorService.updateOrInsert(anturi);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }

        log.info("Obsoleted {} RoadStationSensors", obsoleted);
        log.info("Updated {} RoadStationSensors", updated);
        log.info("Inserted {} RoadStationSensors", inserted);
        if (!invalid.isEmpty()) {
            log.warn("Invalid TMS sensors from lotju {}", invalid.size());
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private static boolean validate(LamLaskennallinenAnturiVO anturi) {
        return anturi.getId() != null && anturi.getVanhaId() != null;
    }
}
