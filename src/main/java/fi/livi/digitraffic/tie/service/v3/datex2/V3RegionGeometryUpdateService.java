package fi.livi.digitraffic.tie.service.v3.datex2;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;

@ConditionalOnNotWebApplication
@Service
public class V3RegionGeometryUpdateService {
    private static final Logger log = LoggerFactory.getLogger(V3RegionGeometryUpdateService.class);

    private final RegionGeometryRepository regionGeometryRepository;

    @Autowired
    public V3RegionGeometryUpdateService(final RegionGeometryRepository regionGeometryRepository) {
        this.regionGeometryRepository = regionGeometryRepository;
    }

    @Transactional
    public String getLatestCommitId() {
        return regionGeometryRepository.getLatestCommitId();
    }

    @Transactional
    public void saveChanges(final List<RegionGeometry> changes) {
        final StopWatch start = StopWatch.createStarted();
        changes.forEach(regionGeometryRepository::save);
        log.info("method=saveChanges insertCount={} tookMs={}", changes.size(), start.getTime());
    }
}
