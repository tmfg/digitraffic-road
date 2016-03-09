package fi.livi.digitraffic.tie.service;

import fi.livi.digitraffic.tie.dao.RoadDistrictRepository;
import fi.livi.digitraffic.tie.model.RoadDistrict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadDistrictServiceImpl implements RoadDistrictService {

    private RoadDistrictRepository roadDistrictRepository;

    @Autowired
    public RoadDistrictServiceImpl(final RoadDistrictRepository roadDistrictRepository) {
        this.roadDistrictRepository = roadDistrictRepository;
    }

    @Transactional
    @Override
    public RoadDistrict findByNaturalId(int roadDistrictNumber) {
        return roadDistrictRepository.findByNaturalId(roadDistrictNumber);
    }

    @Transactional
    @Override
    public RoadDistrict findByRoadSectionAndRoadNaturalId(int roadSectionNaturalId, int roadNaturalId) {
        return roadDistrictRepository.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId);
    }

}
