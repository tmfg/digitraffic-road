package fi.livi.digitraffic.tie.metadata.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.Link;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE LINK SET obsolete = 1, obsolete_date = sysdate " +
                   "WHERE obsolete = 0 AND obsolete_date IS NULL",
           nativeQuery = true)
    void makeNonObsoleteLinksObsolete();

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Link> findByObsoleteDateOrderByNaturalId(final Date obsoleteDate);
}
