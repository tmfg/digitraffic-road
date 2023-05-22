package fi.livi.digitraffic.tie.dao.variablesign.v1;

import java.time.Instant;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.variablesign.Device;

@Repository
public interface DeviceRepositoryV1 extends JpaRepository<Device, String> {
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Device> findAllByDeletedDateIsNull();

    @Query(value =
       "select max(modified)\n" +
       "from device", nativeQuery = true)
    Instant getLastUpdated();
}
