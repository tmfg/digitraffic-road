package fi.livi.digitraffic.tie.metadata.dao.location;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.location.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    @Override
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Location> findAll();
}
