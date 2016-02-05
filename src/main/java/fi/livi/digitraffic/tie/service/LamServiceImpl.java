package fi.livi.digitraffic.tie.service;

import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.LamStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.dao.LamStationRepository;

@Transactional(readOnly = true)
@Service
public class LamServiceImpl implements LamService {

    private final LamStationRepository lamStationRepository;

    @Autowired
    LamServiceImpl(LamStationRepository lamStationRepository) {
        this.lamStationRepository = lamStationRepository;
    }

    @Override
    public FeatureCollection findAllNonObsoleteLamStationsAsFeatureCollection() {
        return LamStationMetadata2FeatureConverter.convert(lamStationRepository.findAllNonObsolete());
    }
}
