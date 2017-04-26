package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.Link;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Link> findByObsoleteDateIsNullOrderByNaturalId();
}
