package fi.livi.digitraffic.tie.dao.v1.location;

import java.util.stream.Stream;

import jakarta.persistence.QueryHint;

import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationDtoV1;
import fi.livi.digitraffic.tie.model.v1.location.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    LocationDtoV1 findByVersionAndLocationCode(final String version, final int locationCode);

    @QueryHints(@QueryHint(name= AvailableHints.HINT_FETCH_SIZE, value="7000"))
    Stream<LocationDtoV1> findAllByVersion(final String version);
}
