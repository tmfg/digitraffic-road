package fi.livi.digitraffic.tie.metadata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.FlywayRepository;
import fi.livi.digitraffic.tie.metadata.dto.FlywayVersion;

@Service
public class FlywayService {

    private final FlywayRepository flywayRepository;

    private FlywayVersion flywayVersion;

    @Autowired
    public FlywayService(final FlywayRepository flywayRepository) {
        this.flywayRepository = flywayRepository;
    }

    @Transactional
    public FlywayVersion getLatestVersion() {
        if (flywayVersion == null) {
            flywayVersion = flywayRepository.findLatest();
        }
        return flywayVersion;
    }
}
