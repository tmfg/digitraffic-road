package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.metadata.model.TmsSensorConstant;

public interface TmsSensorConstantRepository extends JpaRepository<TmsSensorConstant, Long> {

    List<TmsSensorConstant> findByObsoleteDateIsNull();
}