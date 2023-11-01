package fi.livi.digitraffic.tie.dao.trafficmessage.location;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationTypeDtoV1;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationType;

@Repository
public interface LocationTypeRepository extends JpaRepository<LocationType, String> {
    List<LocationTypeDtoV1> findAllByIdVersion(final String version);

    List<LocationTypeDtoV1> findAllByIdVersionOrderByIdTypeCode(final String version);
}
