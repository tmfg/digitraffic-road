package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.model.Link;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE LINK SET obsolete = 1, obsolete_date = sysdate " +
                   "WHERE obsolete = 0 AND obsolete_date IS NULL",
           nativeQuery = true)
    void makeNonObsoleteLinksObsolete();

    List<Link> findByOrderByNaturalId();
}
