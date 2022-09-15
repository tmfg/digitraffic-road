package fi.livi.digitraffic.tie.service.tms.v1;

import static fi.livi.digitraffic.tie.helper.DateHelper.getNewest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.converter.tms.v1.TmsStationToFeatureConverterV1;
import fi.livi.digitraffic.tie.dao.v1.TmsFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.dao.v1.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsFreeFlowSpeedDto;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;


@ConditionalOnWebApplication
@Service
public class TmsStationMetadataWebServiceV1 {
    private final TmsStationRepository tmsStationRepository;
    private final DataStatusService dataStatusService;
    private final TmsStationToFeatureConverterV1 tmsStationToFeatureConverterV1;
    private final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository;

    @Autowired
    public TmsStationMetadataWebServiceV1(final TmsStationRepository tmsStationRepository,
                                          final DataStatusService dataStatusService,
                                          final TmsStationToFeatureConverterV1 tmsStationToFeatureConverterV1,
                                          final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository) {
        this.tmsStationRepository = tmsStationRepository;
        this.dataStatusService = dataStatusService;
        this.tmsStationToFeatureConverterV1 = tmsStationToFeatureConverterV1;
        this.tmsFreeFlowSpeedRepository = tmsFreeFlowSpeedRepository;
    }

    @Transactional(readOnly = true)
    public TmsStationFeatureCollectionSimpleV1 findAllPublishableTmsStationsAsSimpleFeatureCollection(final boolean onlyUpdateInfo, final RoadStationState roadStationState) {
        final List<TmsStation> stations = onlyUpdateInfo ? Collections.emptyList() : findPublishableStations(roadStationState);

        return tmsStationToFeatureConverterV1.convertToSimpleFeatureCollection(
            stations,
            getMetadataLastUpdated());
    }

    @Transactional(readOnly = true)
    public TmsStationFeatureDetailedV1 getTmsStationById(final Long id) {
        final TmsStation station = tmsStationRepository.findByRoadStationIsPublicIsTrueAndRoadStation_NaturalId(id);
        if(station == null) {
            throw new ObjectNotFoundException(TmsStation.class, id);
        }

        final TmsFreeFlowSpeedDto ffs = tmsFreeFlowSpeedRepository.getTmsFreeFlowSpeedsByRoadStationNaturalId(station.getRoadStationNaturalId());

        return tmsStationToFeatureConverterV1.convertToDetailedFeature(station, ffs != null ? ffs.getFreeFlowSpeed1OrNull() : null, ffs != null ? ffs.getFreeFlowSpeed2OrNull() : null);
    }

    private List<TmsStation> findPublishableStations(final RoadStationState roadStationState) {
        switch(roadStationState) {
        case ACTIVE:
            return tmsStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
        case REMOVED:
            return tmsStationRepository.findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId
                (CollectionStatus.REMOVED_PERMANENTLY);
        case ALL:
            return tmsStationRepository.findByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();
        default:
            throw new IllegalArgumentException();
        }
    }

    private Instant getMetadataLastUpdated() {
        final Instant sensorsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_METADATA);
        final Instant stationsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_METADATA);
        return getNewest(sensorsUpdated, stationsUpdated);
    }

    private Instant getMetadataLastChecked() {
        final Instant sensorsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_METADATA_CHECK);
        final Instant stationsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_METADATA_CHECK);
        return getNewest(sensorsUpdated, stationsUpdated);
    }
}

