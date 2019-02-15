package fi.livi.digitraffic.tie.metadata.dao.location;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

import java.util.List;
import java.util.stream.Stream;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.dto.location.LocationJson;
import fi.livi.digitraffic.tie.metadata.model.location.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    LocationJson findByVersionAndLocationCode(final String version, final int locationCode);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="100"))
    Stream<LocationJson> findAllByVersion(final String version);
}
