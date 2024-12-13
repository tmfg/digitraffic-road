package fi.livi.digitraffic.tie.service.tms;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.conf.RoadCacheConfiguration;
import fi.livi.digitraffic.tie.dao.tms.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.dao.tms.TmsSensorConstantValueDtoV1Repository;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDtoV1;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;

@Service
public class TmsStationSensorConstantService {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorConstantService.class);
    private final TmsSensorConstantDao tmsSensorConstantDao;
    private final TmsSensorConstantValueDtoV1Repository tmsSensorConstantValueDtoRepository;

    @Autowired
    public TmsStationSensorConstantService(final TmsSensorConstantDao tmsSensorConstantDao,
                                           final TmsSensorConstantValueDtoV1Repository tmsSensorConstantValueDtoRepository) {

        this.tmsSensorConstantDao = tmsSensorConstantDao;
		this.tmsSensorConstantValueDtoRepository = tmsSensorConstantValueDtoRepository;
    }

    @CacheEvict(cacheNames = RoadCacheConfiguration.CACHE_FREE_FLOW_SPEEDS, key = "#rsNaturalId")
    @Transactional
    public boolean updateSensorConstant(final LamAnturiVakioVO anturiVakio,
                                        @SuppressWarnings("unused") final Long rsNaturalId) {
        return tmsSensorConstantDao.updateSensorConstants(Collections.singletonList(anturiVakio)) > 0;
    }

    @CacheEvict(cacheNames = RoadCacheConfiguration.CACHE_FREE_FLOW_SPEEDS, allEntries = true)
    @Transactional
    public boolean updateSensorConstants(final List<LamAnturiVakioVO> allLamAnturiVakios) {
        final List<Long> ids = allLamAnturiVakios.stream().map(LamAnturiVakioVO::getId).collect(Collectors.toList());
        final int obsoleted = tmsSensorConstantDao.obsoleteSensorConstantsExcludingIds(ids);
        final int upsert = tmsSensorConstantDao.updateSensorConstants(allLamAnturiVakios);
        log.info("method=updateSensorConstants upsertCount={}, obsoleteCount={}", upsert, obsoleted);
        return obsoleted > 0 || upsert > 0;
    }

    @CacheEvict(cacheNames = RoadCacheConfiguration.CACHE_FREE_FLOW_SPEEDS, key = "#rsNaturalId")
    @Transactional
    public boolean updateSingleStationsSensorConstants(final List<LamAnturiVakioVO> sigleStationsLamAnturiVakios,
                                                       @SuppressWarnings("unused") final Long rsNaturalId) {
        final long roadStationLotjuId = sigleStationsLamAnturiVakios.stream().findFirst().map(LamAnturiVakioVO::getAsemaId).orElseThrow();
        final int obsoleted = tmsSensorConstantDao.obsoleteSensorConstantsWithRoadStationLotjuId(roadStationLotjuId);
        final int upsert = tmsSensorConstantDao.updateSensorConstants(sigleStationsLamAnturiVakios);
        log.info("method=updateSingleStationsSensorConstants upsertCount={}, obsoleteCount={}", upsert, obsoleted);
        return obsoleted > 0 || upsert > 0;
    }

    @CacheEvict(cacheNames = RoadCacheConfiguration.CACHE_FREE_FLOW_SPEEDS, key = "#rsNaturalId")
    @Transactional
    public boolean obsoleteSensorConstantWithLotjuId(final long lotjuId,
                                                     @SuppressWarnings("unused") final Long rsNaturalId) {
        return tmsSensorConstantDao.obsoleteSensorConstantWithLotjuId(lotjuId) > 0;
    }

    @CacheEvict(cacheNames = RoadCacheConfiguration.CACHE_FREE_FLOW_SPEEDS, allEntries = true)
    @Transactional
    public boolean updateSingleSensorConstantValues(final List<LamAnturiVakioArvoVO> lamAnturiVakioArvos,
                                                    @SuppressWarnings("unused") final Long rsNaturalId) {
        lamAnturiVakioArvos.forEach(v -> tmsSensorConstantDao.updateSensorConstantValueToObsoleteWithSensorConstantValueLotjuId(v.getId()));
        final int upsert = tmsSensorConstantDao.updateSensorConstantValues(lamAnturiVakioArvos);
        log.info("method=updateSingleSensorConstantValues upsert={}", upsert);
        return upsert > 0;
    }

    @CacheEvict(cacheNames = RoadCacheConfiguration.CACHE_FREE_FLOW_SPEEDS, allEntries = true)
    @Transactional
    public boolean updateSensorConstantValues(final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos) {
        final List<Long> ids = allLamAnturiVakioArvos.stream().map(LamAnturiVakioArvoVO::getId).collect(Collectors.toList());
        final int obsoleted = tmsSensorConstantDao.updateSensorSensorConstantValuesToObsoleteExcludingIds(ids);
        final int upsert = tmsSensorConstantDao.updateSensorConstantValues(allLamAnturiVakioArvos);
        log.info("method=updateSensorConstantValues upsert={}, obsoleteCount={}", upsert, obsoleted);
        return obsoleted > 0 || upsert > 0;
    }

    @CacheEvict(cacheNames = RoadCacheConfiguration.CACHE_FREE_FLOW_SPEEDS, key = "#rsNaturalId")
    @Transactional
    public boolean updateStationSensorConstantValues(final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos,
                                                     @SuppressWarnings("unused") final Long rsNaturalId) {
        final int obsoleted = allLamAnturiVakioArvos
            .stream()
            .map(v -> tmsSensorConstantDao.updateSensorConstantValueToObsoleteWithSensorConstantValueLotjuId(v.getId()))
            .reduce(0, Integer::sum);
        final int upsert = tmsSensorConstantDao.updateSensorConstantValues(allLamAnturiVakioArvos);
        log.info("method=updateStationSensorConstantValues upsert={}, obsoleteCount={}", upsert, obsoleted-upsert);
        return obsoleted > 0 || upsert > 0;
    }

    @CacheEvict(cacheNames = RoadCacheConfiguration.CACHE_FREE_FLOW_SPEEDS, key = "#rsNaturalId")
    @Transactional
    public boolean updateSensorConstantValueToObsoleteWithSensorConstantValueLotjuId(final long sensorConstantValueLotjuId,
                                                                                     @SuppressWarnings("unused") final Long rsNaturalId) {
        return tmsSensorConstantDao.updateSensorConstantValueToObsoleteWithSensorConstantValueLotjuId(sensorConstantValueLotjuId) > 0;
    }

    @Transactional(readOnly = true)
    public Instant getLatestModified() {
        return tmsSensorConstantValueDtoRepository.getTmsSensorConstantsLastUpdated();
    }

    @Transactional(readOnly = true)
    public TmsSensorConstantValueDtoV1 getStationSensorConstantValue(final long stationLotjuId, final long sensorConstantValueLotjuId) {
        return tmsSensorConstantValueDtoRepository.getStationSensorConstantValue(stationLotjuId, sensorConstantValueLotjuId);
    }
}
