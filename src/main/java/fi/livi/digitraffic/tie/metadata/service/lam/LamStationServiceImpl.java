package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.LamStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.LamStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class LamStationServiceImpl implements LamStationService {
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

        final MetadataUpdated updated = staticDataStatusService.findMetadataUptadedByMetadataType(MetadataType.LAM_STATION);

        return LamStationMetadata2FeatureConverter.convert(
                onlyUpdateInfo == false ?
                    lamStationRepository.findByRoadStationObsoleteFalseAndRoadStationIsPublicTrue() :
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
    public Map<Long, LamStation> findAllLamStationsMappedByByNaturalId() {
        final List<LamStation> allStations = lamStationRepository.findAll();
        final Map<Long, LamStation> stationMap = new HashMap<>();

        for(final LamStation lam : allStations) {
            stationMap.put(lam.getNaturalId(), lam);
        }

        return stationMap;
    }
}
