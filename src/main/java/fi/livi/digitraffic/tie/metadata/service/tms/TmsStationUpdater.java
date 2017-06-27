package fi.livi.digitraffic.tie.metadata.service.tms;

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

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.UpdateStatus;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;

@Service
public class TmsStationUpdater {

    private static final Logger log = LoggerFactory.getLogger(TmsStationUpdater.class);

    private final RoadStationService roadStationService;
    private final TmsStationService tmsStationService;
    private final DataStatusService dataStatusService;
    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;

    @Autowired
    public TmsStationUpdater(final RoadStationService roadStationService,
                             final TmsStationService tmsStationService,
                             final DataStatusService dataStatusService,
                             final LotjuTmsStationMetadataService lotjuTmsStationMetadataService) {
        this.roadStationService = roadStationService;
        this.tmsStationService = tmsStationService;
        this.dataStatusService = dataStatusService;
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
    }

    public boolean updateTmsStations() {
        log.info("Update tms Stations start");

        final List<LamAsemaVO> asemas = lotjuTmsStationMetadataService.getLamAsemas();

        final boolean updatedTmsStations = updateTmsStationsMetadata(asemas);
        updateStaticDataStatus(updatedTmsStations);
        log.info("UpdateTmsStations end");
        return updatedTmsStations;
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        dataStatusService.updateStaticDataStatus(DataStatusService.StaticStatusType.TMS, updateStaticDataStatus);
    }

    private boolean updateTmsStationsMetadata(final List<LamAsemaVO> lamAsemas) {

        final int fixed = tmsStationService.fixNullLotjuIds(lamAsemas);

        int updated = 0;
        int inserted = 0;

        final List<LamAsemaVO> toUpdate =
            lamAsemas.stream().filter(lam -> validate(lam)).collect(Collectors.toList());

        final List<Long> notToObsoleteLotjuIds = toUpdate.stream().map(LamAsemaVO::getId).collect(Collectors.toList());
        final int obsoleted = roadStationService.obsoleteRoadStationsExcludingLotjuIds(RoadStationType.TMS_STATION, notToObsoleteLotjuIds);
        log.info("Not to obsolete lotju ids {}", notToObsoleteLotjuIds);

        final Collection invalid = CollectionUtils.subtract(lamAsemas, toUpdate);
        invalid.forEach(i -> log.warn("Found invalid {}", ReflectionToStringBuilder.toString(i)));

        for (LamAsemaVO tsa : toUpdate) {
            UpdateStatus result = tmsStationService.updateOrInsertTmsStation(tsa);
            if (result == UpdateStatus.UPDATED) {
                updated++;
            } else if (result == UpdateStatus.INSERTED) {
                inserted++;
            }
        }


        log.info("Obsoleted {} TmsStations", obsoleted);
        log.info("Updated {} TmsStations", updated);
        log.info("Inserted {} TmsStations", inserted);
        if (!invalid.isEmpty()) {
            log.warn("Invalid WeatherStations from lotju {}", invalid.size());
        }

        return obsoleted > 0 || inserted > 0 || updated > 0 || fixed > 0;
    }

    private boolean validate(final LamAsemaVO lamAsema) {
        final boolean valid = lamAsema.getVanhaId() != null;
        if(!valid && !isPermanentlyDeletedKeruunTila(lamAsema.getKeruunTila())) {
            log.error("{} is invalid: has null vanhaId", ToStringHelper.toString(lamAsema));
        }
        return valid;
    }
}
