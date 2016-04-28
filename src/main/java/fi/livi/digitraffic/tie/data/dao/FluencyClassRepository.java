package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.trafficfluency.FluencyClass;

@Repository
public interface FluencyClassRepository extends JpaRepository<FluencyClass, Long> {

    @Cacheable("FluencyClass")
    @Query(value =
           "SELECT *\n"
         + "FROM FLUENCY_CLASS\n"
         + "ORDER BY LOWER_LIMIT ASC",
           nativeQuery = true)
    List<FluencyClass> findAllOrderByLowerLimitAsc();
}
