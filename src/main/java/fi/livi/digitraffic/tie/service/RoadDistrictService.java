package fi.livi.digitraffic.tie.service;

import fi.livi.digitraffic.tie.model.RoadDistrict;

public interface RoadDistrictService {

    RoadDistrict findByNaturalId(int roadDistrictNumber);

    RoadDistrict findByRoadSectionAndRoadNaturalId(final int roadSectionNaturalId,
                                                   final int roadNaturalId);
}
