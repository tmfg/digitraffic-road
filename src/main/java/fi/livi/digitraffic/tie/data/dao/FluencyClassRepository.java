package fi.livi.digitraffic.tie.data.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.FluencyClass;

@Repository
public interface FluencyClassRepository extends JpaRepository<FluencyClass, Long> {

    @Cacheable("FluencyClass")
    @Query(value =
           "SELECT *\n"
         + "FROM FLUENCY_CLASS\n"
         + "ORDER BY LOWER_LIMIT DESC",
           nativeQuery = true)
    List<FluencyClass> findAllOrderByLowerLimitDesc();

    @Query(value = "SELECT upper_limit FROM FLUENCY_CLASS WHERE code = :code", nativeQuery = true)
    BigDecimal getFluencyClassUpperLimit(@Param("code") final int code);
}
