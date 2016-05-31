package fi.livi.digitraffic.tie.metadata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.RoadDistrictRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadDistrict;

@Service
public class RoadDistrictServiceImpl implements RoadDistrictService {

    private final RoadDistrictRepository roadDistrictRepository;

    @Autowired
    public RoadDistrictServiceImpl(final RoadDistrictRepository roadDistrictRepository) {
        this.roadDistrictRepository = roadDistrictRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public RoadDistrict findByNaturalId(final int roadDistrictNumber) {
        return roadDistrictRepository.findByNaturalId(roadDistrictNumber);
    }

    @Transactional(readOnly = true)
    @Override
    public RoadDistrict findByRoadSectionAndRoadNaturalId(final int roadSectionNaturalId, final int roadNaturalId) {
        return roadDistrictRepository.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId);
    }

}
