package fi.livi.digitraffic.tie.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.model.RoadDistrict;

public interface RoadDistrictRepository extends JpaRepository<RoadDistrict, Long> {
    RoadDistrict findByNaturalId(final int tienumero);
}
