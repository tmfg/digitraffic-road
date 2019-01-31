package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.metadata.model.TmsSensorConstantValue;

public interface TmsSensorConstantValueRepository extends JpaRepository<TmsSensorConstantValue, Long> {

    List<TmsSensorConstantValue> findByObsoleteDateIsNull();
}