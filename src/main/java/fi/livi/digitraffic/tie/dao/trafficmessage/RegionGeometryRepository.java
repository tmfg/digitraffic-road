package fi.livi.digitraffic.tie.dao.trafficmessage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;

public interface RegionGeometryRepository extends JpaRepository<RegionGeometry, Long> {

    @Query(value =
           "select git_commit_id\n" +
           "from region_geometry\n" +
           "order by id desc\n" +
           "limit 1", nativeQuery = true)
    String getLatestCommitId();

    List<RegionGeometry> findAllByOrderByIdAsc();
}