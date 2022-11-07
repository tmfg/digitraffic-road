package fi.livi.digitraffic.tie.dao.v1.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.location.LocationVersion;

@Repository
public interface LocationVersionRepository extends JpaRepository<LocationVersion, String> {
    @Query(value =
        "select version, created, modified\n" +
        "from location_version\n" +
        "order by created desc\n" +
        "limit 1", nativeQuery = true)
    LocationVersion findLatestVersion();
}
