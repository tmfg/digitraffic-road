package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.metadata.model.TmsSensorConstant;

public interface TmsSensorConstantRepository extends JpaRepository<TmsSensorConstant, Long> {

    @EntityGraph(attributePaths = { "roadStation" })
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="4000"))
    List<TmsSensorConstant> findByObsoleteDateIsNull();
}