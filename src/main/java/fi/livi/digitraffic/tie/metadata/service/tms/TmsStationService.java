package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.TmsStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class TmsStationService {

    private static final Logger log = LoggerFactory.getLogger(TmsStationService.class);
    private final TmsStationRepository tmsStationRepository;
    private final StaticDataStatusService staticDataStatusService;
    private final TmsStationMetadata2FeatureConverter tmsStationMetadata2FeatureConverter;

    @Autowired
    public TmsStationService(final TmsStationRepository tmsStationRepository,
                             final StaticDataStatusService staticDataStatusService,
                             final TmsStationMetadata2FeatureConverter tmsStationMetadata2FeatureConverter) {
        this.tmsStationRepository = tmsStationRepository;
        this.staticDataStatusService = staticDataStatusService;
        this.tmsStationMetadata2FeatureConverter = tmsStationMetadata2FeatureConverter;
    }

    @Transactional(readOnly = true)
    public TmsStationFeatureCollection findAllNonObsoletePublicTmsStationsAsFeatureCollection(final boolean onlyUpdateInfo) {

        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.LAM_STATION);

        return tmsStationMetadata2FeatureConverter.convert(
                onlyUpdateInfo ?
                Collections.emptyList() :
                findAllNonObsoletePublicNonNullLotjuIdTmsStations(),
                updated != null ? updated.getUpdatedTime() : null);
    }

    @Transactional
    public List<TmsStation> findAllNonObsoletePublicNonNullLotjuIdTmsStations() {
        return tmsStationRepository.findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueAndLotjuIdIsNotNullOrderByRoadStation_NaturalId();
    }

    @Transactional
    public TmsStation save(final TmsStation tmsStation) {
        try {
            final TmsStation tms = tmsStationRepository.save(tmsStation);
            tmsStationRepository.flush();
            return tms;
        } catch (Exception e) {
            log.error("Could not save " + tmsStation);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsMappedByByTmsNaturalId() {
        final List<TmsStation> allStations = tmsStationRepository.findAll();
        final Map<Long, TmsStation> stationMap = new HashMap<>();

        for(final TmsStation tms : allStations) {
            stationMap.put(tms.getNaturalId(), tms);
        }

        return stationMap;
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsMappedByByLotjuId() {
        final List<TmsStation> allStations = tmsStationRepository.findAll();
        return allStations.stream().filter(tms -> tms.getLotjuId() != null).collect(Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsByMappedByLotjuId() {
        final Map<Long, TmsStation> map = new HashMap<>();
        final List<TmsStation> all = tmsStationRepository.findAll();
        for (final TmsStation tmsStation : all) {
            if (tmsStation.getLotjuId() != null) {
                map.put(tmsStation.getLotjuId(), tmsStation);
            } else {
                log.warn("Null lotjuId: " + tmsStation);
            }
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllNonObsoletePublicTmsStationsMappedByLotjuId() {
        final List<TmsStation> all = findAllNonObsoletePublicNonNullLotjuIdTmsStations();
        return all.stream().collect(Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public TmsStation findPublicNonObsoleteTmsStationByRoadStationNaturalId(long roadStationNaturalId) {
        TmsStation entity = tmsStationRepository.findByRoadStation_NaturalIdAndObsoleteDateIsNullAndLotjuIdIsNotNullAndRoadStationIsPublicTrue(roadStationNaturalId);
        if (entity == null) {
            throw new ObjectNotFoundException(TmsStation.class, roadStationNaturalId);
        }
        return entity;
    }

    @Transactional(readOnly = true)
    public boolean tmsStationExistsWithRoadStationNaturalId(long roadStationNaturalId) {
        return tmsStationRepository.tmsExistsWithRoadStationNaturalId(roadStationNaturalId);
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsWithoutLotjuIdMappedByTmsNaturalId() {
        List<TmsStation> all = tmsStationRepository.findByLotjuIdIsNull();
        return all.stream().collect(Collectors.toMap(TmsStation::getNaturalId, Function.identity()));
    }
}
