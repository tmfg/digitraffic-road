package fi.livi.digitraffic.tie.dao.trafficmessage.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationVersion;

@Repository
public interface LocationVersionRepository extends JpaRepository<LocationVersion, String> {
    @Query(value = """
        select version, created, modified
        from location_version
        order by created desc
        limit 1""", nativeQuery = true)
    LocationVersion findLatestVersion();
}
