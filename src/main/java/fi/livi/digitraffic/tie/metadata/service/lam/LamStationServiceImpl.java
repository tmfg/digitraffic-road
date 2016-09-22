package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.metadata.converter.LamStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.LamStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class LamStationServiceImpl implements LamStationService {

    private static final Logger log = LoggerFactory.getLogger(LamStationServiceImpl.class);
    private final LamStationRepository lamStationRepository;
    private StaticDataStatusService staticDataStatusService;

    @Autowired
    public LamStationServiceImpl(final LamStationRepository lamStationRepository,
                                 final StaticDataStatusService staticDataStatusService) {
        this.lamStationRepository = lamStationRepository;
        this.staticDataStatusService = staticDataStatusService;
    }

    @Transactional(readOnly = true)
    @Override
    public LamStationFeatureCollection findAllNonObsoletePublicLamStationsAsFeatureCollection(final boolean onlyUpdateInfo) {

        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.LAM_STATION);

        return LamStationMetadata2FeatureConverter.convert(
                onlyUpdateInfo == false ?
                    lamStationRepository.findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueAndLotjuIdIsNotNullOrderByRoadStation_NaturalId() :
                    Collections.emptyList(),
                updated != null ? updated.getUpdated() : null);
    }

    @Transactional
    @Override
    public LamStation save(final LamStation lamStation) {
        final LamStation lam = lamStationRepository.save(lamStation);
        lamStationRepository.flush();
        return lam;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, LamStation> findAllLamStationsMappedByByLamNaturalId() {
        final List<LamStation> allStations = lamStationRepository.findAll();
        final Map<Long, LamStation> stationMap = new HashMap<>();

        for(final LamStation lam : allStations) {
            stationMap.put(lam.getNaturalId(), lam);
        }

        return stationMap;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, LamStation> findAllLamStationsMappedByByRoadStationNaturalId() {
        final List<LamStation> allStations = lamStationRepository.findAll();
        final Map<Long, LamStation> stationMap = new HashMap<>();

        for(final LamStation lam : allStations) {
            stationMap.put(lam.getRoadStationNaturalId(), lam);
        }

        return stationMap;
    }

    @Transactional(readOnly = true)
    @Override
    public LamStation findByLotjuId(long lamStationLotjuId) {
        return lamStationRepository.findByLotjuId(lamStationLotjuId);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, LamStation> findAllLamStationsByMappedByLotjuId() {
        final Map<Long, LamStation> map = new HashMap<>();
        final List<LamStation> all = lamStationRepository.findAll();
        for (final LamStation lamStation : all) {
            if (lamStation.getLotjuId() != null) {
                map.put(lamStation.getLotjuId(), lamStation);
            } else {
                log.warn("Null lotjuId: " + lamStation);
            }
        }
        return map;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, LamStation> findLamStationsMappedByLotjuId(List<Long> lamStationLotjuIds) {

        final List<LamStation> all = lamStationRepository.findByLotjuIdIn(lamStationLotjuIds);
        Map<Long, LamStation> result = all.stream().collect(Collectors.toMap(p -> p.getLotjuId(), p -> p));
        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public LamStation findByRoadStationNaturalId(long roadStationNaturalId) {
        LamStation entity = lamStationRepository.findByRoadStation_NaturalId(roadStationNaturalId);
        if (entity == null) {
            throw new ObjectNotFoundException(LamStation.class, roadStationNaturalId);
        }
        return entity;
    }

    @Override
    public boolean lamStationExistsWithRoadStationNaturalId(long roadStationNaturalId) {
        return lamStationRepository.lamExistsWithRoadStationNaturalId(roadStationNaturalId);
    }

    @Override
    public boolean lamStationExistsWithNaturalId(long lamNaturalId) {
        return lamStationRepository.lamExistsWithLamNaturalId(lamNaturalId);
    }
}
