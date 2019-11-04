package fi.livi.digitraffic.tie.data.dao;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    Stream<Device> streamAll();
}
