package fi.livi.digitraffic.tie.metadata.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.metadata.model.TmsSensorConstant;
import fi.livi.digitraffic.tie.metadata.model.TmsSensorConstantValue;

public interface TmsSensorConstantValueRepository extends JpaRepository<TmsSensorConstantValue, Long> {

}