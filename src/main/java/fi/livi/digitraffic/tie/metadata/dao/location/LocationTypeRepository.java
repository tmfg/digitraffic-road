package fi.livi.digitraffic.tie.metadata.dao.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.location.LocationType;

@Repository
public interface LocationTypeRepository extends JpaRepository<LocationType, String> {
}
