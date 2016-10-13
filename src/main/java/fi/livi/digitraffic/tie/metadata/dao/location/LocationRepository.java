package fi.livi.digitraffic.tie.metadata.dao.location;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.location.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {
    @Query("select l from Location l")
    Stream<Location> streamAll();
}
