package fi.livi.digitraffic.tie.service.v1.tms;

import static fi.livi.digitraffic.tie.helper.DateHelper.getGreatestAtUtc;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.converter.TmsStationMetadata2Datex2Converter;
import fi.livi.digitraffic.tie.dao.v1.tms.TmsStationDatex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
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

        switch(roadStationState) {
        case ACTIVE:
            return tmsStationDatex2Repository.findDistinctByRoadStationPublishableIsTrueOrderByNaturalId();
        case REMOVED:
            return tmsStationDatex2Repository
                .findDistinctByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByNaturalId(CollectionStatus.REMOVED_PERMANENTLY);
        case ALL:
            return tmsStationDatex2Repository.findDistinctByRoadStationIsPublicIsTrueOrderByNaturalId();
        default:
            throw new IllegalArgumentException();
        }
    }

    private ZonedDateTime getMetadataLastUpdated() {
        final ZonedDateTime sensorsUpdated = dataStatusService.findDataUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA);
        final ZonedDateTime stationsUpdated = dataStatusService.findDataUpdatedTime(DataType.TMS_STATION_METADATA);
        return getGreatestAtUtc(sensorsUpdated, stationsUpdated);
    }

}
