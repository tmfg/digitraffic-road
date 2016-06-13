package fi.livi.digitraffic.tie.metadata.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.RoadAddress;

@Repository
public interface RoadAddressRepository extends JpaRepository<RoadAddress, Long>{


}
