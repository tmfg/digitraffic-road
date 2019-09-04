package fi.livi.digitraffic.tie.data.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.trafficsigns.DeviceData;

@Repository
public interface DeviceDataRepository extends JpaRepository<DeviceData, Long> {
}
