package fi.livi.digitraffic.tie.metadata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.conf.RoadCacheConfiguration;
import fi.livi.digitraffic.tie.metadata.dao.FlywayRepository;
import fi.livi.digitraffic.tie.metadata.dto.FlywayVersion;

@Service
public class FlywayService {

    private final FlywayRepository flywayRepository;

    @Autowired
    public FlywayService(final FlywayRepository flywayRepository) {
        this.flywayRepository = flywayRepository;
    }

    @Transactional
    @Cacheable(RoadCacheConfiguration.FLYWAY_VERSION_CACHE)
    public FlywayVersion getLatestVersion() {
        return flywayRepository.findLatest();
    }
}
