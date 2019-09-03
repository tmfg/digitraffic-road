package fi.livi.digitraffic.tie.metadata.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.traffic_signs.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
}
