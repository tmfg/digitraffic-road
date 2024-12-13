package fi.livi.digitraffic.tie.service.tms.v1;

import static fi.livi.digitraffic.common.util.TimeUtil.getGreatest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.converter.tms.v1.TmsStationToFeatureConverterV1;
import fi.livi.digitraffic.tie.dao.tms.TmsFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.dao.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsFreeFlowSpeedDto;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.tms.TmsStationService;

@ConditionalOnWebApplication
@Service
public class TmsStationMetadataWebServiceV1 {
    private final TmsStationRepository tmsStationRepository;
    private final DataStatusService dataStatusService;
    private final TmsStationToFeatureConverterV1 tmsStationToFeatureConverterV1;
    private final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository;
    private final TmsStationService tmsStationService;

    @Autowired
    public TmsStationMetadataWebServiceV1(final TmsStationRepository tmsStationRepository,
                                          final DataStatusService dataStatusService,
                                          final TmsStationToFeatureConverterV1 tmsStationToFeatureConverterV1,
                                          final TmsFreeFlowSpeedRepository tmsFreeFlowSpeedRepository,
                                          final TmsStationService tmsStationService) {
        this.tmsStationRepository = tmsStationRepository;
        this.dataStatusService = dataStatusService;
        this.tmsStationToFeatureConverterV1 = tmsStationToFeatureConverterV1;
        this.tmsFreeFlowSpeedRepository = tmsFreeFlowSpeedRepository;
        this.tmsStationService = tmsStationService;
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
        final TmsStation station = getPublishableStationById(id);
        final TmsFreeFlowSpeedDto ffs = tmsFreeFlowSpeedRepository.getTmsFreeFlowSpeedsByRoadStationNaturalId(station.getRoadStationNaturalId());

        return tmsStationToFeatureConverterV1.convertToDetailedFeature(station, ffs != null ? ffs.getFreeFlowSpeed1OrNull() : null, ffs != null ? ffs.getFreeFlowSpeed2OrNull() : null);
    }

    @Transactional(readOnly = true)
    public TmsStation getPublishableStationById(final Long id) {
        return tmsStationService.findPublishableTmsStationByRoadStationNaturalId(id);
    }

    @Transactional(readOnly = true)
    public List<TmsStation> findPublishableStations(final RoadStationState roadStationState) {
        return switch (roadStationState) {
            case ACTIVE -> tmsStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
            case REMOVED -> tmsStationRepository.findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId
                    (CollectionStatus.REMOVED_PERMANENTLY);
            case ALL -> tmsStationRepository.findByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();
        };
    }

    @Transactional(readOnly = true)
    public Instant getMetadataLastUpdated() {
        final Instant sensorsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_METADATA);
        final Instant stationsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_METADATA);
        return getGreatest(sensorsUpdated, stationsUpdated);
    }

    private Instant getMetadataLastChecked() {
        final Instant sensorsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_METADATA_CHECK);
        final Instant stationsUpdated = dataStatusService.findDataUpdatedInstant(DataType.TMS_STATION_METADATA_CHECK);
        return getGreatest(sensorsUpdated, stationsUpdated);
    }
}

