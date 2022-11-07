package fi.livi.digitraffic.tie.dao.v1.location;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationSubtypeDtoV1;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;

@Repository
public interface LocationSubtypeRepository extends JpaRepository<LocationSubtype, String> {
    List<LocationSubtypeDtoV1> findAllByIdVersion(final String version);

    List<LocationSubtypeDtoV1> findAllByIdVersionOrderByIdSubtypeCode(final String version);
}
