package fi.livi.digitraffic.tie.metadata.service;

import fi.livi.digitraffic.tie.metadata.model.RoadDistrict;

public interface RoadDistrictService {

    RoadDistrict findByNaturalId(int roadDistrictNumber);

    RoadDistrict findByRoadSectionAndRoadNaturalId(final int roadSectionNaturalId,
                                                   final int roadNaturalId);
}
