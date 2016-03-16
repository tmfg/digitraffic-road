package fi.livi.digitraffic.tie.dao;

import java.util.List;

import fi.livi.digitraffic.tie.model.CameraPreset;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CameraPresetRepository extends JpaRepository<CameraPreset, Long> {

    @EntityGraph("camera")
    @Override
    List<CameraPreset> findAll();
}
