package fi.livi.digitraffic.tie.data.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;

public class DeviceRepositoryTest extends AbstractJpaTest {
    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    public void notFound() {
        final Optional<Device> d = deviceRepository.findById("not_found");

        assertFalse(d.isPresent());
    }

    @Test
    public void found() {
        final Optional<Device> d = deviceRepository.findById("id1");

        assertTrue(d.isPresent());
    }
}
