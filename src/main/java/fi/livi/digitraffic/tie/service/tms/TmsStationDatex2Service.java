package fi.livi.digitraffic.tie.service.tms;

import static fi.livi.digitraffic.common.util.TimeUtil.getGreatest;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.converter.tms.TmsStationMetadata2Datex2Converter;
import fi.livi.digitraffic.tie.dao.tms.TmsStationDatex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;

@ConditionalOnWebApplication
@Service
public class TmsStationDatex2Service {
    private final TmsStationDatex2Repository tmsStationDatex2Repository;
    private final TmsStationMetadata2Datex2Converter tmsStationMetadata2Datex2Converter;
    private final DataStatusService dataStatusService;

    public TmsStationDatex2Service(final TmsStationDatex2Repository tmsStationDatex2Repository,
                                   final TmsStationMetadata2Datex2Converter tmsStationMetadata2Datex2Converter,
                                   final DataStatusService dataStatusService) {
        this.tmsStationDatex2Repository = tmsStationDatex2Repository;
        this.tmsStationMetadata2Datex2Converter = tmsStationMetadata2Datex2Converter;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findAllPublishableTmsStationsAsDatex2(final RoadStationState roadStationState) {
        final List<TmsStation> stations = findStations(roadStationState);
        return tmsStationMetadata2Datex2Converter.convert(stations, getMetadataLastUpdated());
    }

    @Transactional(readOnly = true)
    public List<TmsStation> findAllPublishableTmsStations() {
        return tmsStationDatex2Repository.findDistinctByRoadStationPublishableIsTrueOrderByNaturalId();
    }

    private List<TmsStation> findStations(final RoadStationState roadStationState) {

        return switch (roadStationState) {
            case ACTIVE -> tmsStationDatex2Repository.findDistinctByRoadStationPublishableIsTrueOrderByNaturalId();
            case REMOVED -> tmsStationDatex2Repository.findDistinctByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByNaturalId(CollectionStatus.REMOVED_PERMANENTLY);
            case ALL -> tmsStationDatex2Repository.findDistinctByRoadStationIsPublicIsTrueOrderByNaturalId();
        };
    }

    private Instant getMetadataLastUpdated() {
        final Instant sensorsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_METADATA);
        final Instant stationsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_METADATA);
        return getGreatest(sensorsUpdated, stationsUpdated);
    }

}
