package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.metadata.dao.TmsSensorConstantRepository;
import fi.livi.digitraffic.tie.metadata.dao.TmsSensorConstantValueRepository;
import fi.livi.digitraffic.tie.metadata.model.TmsSensorConstant;
import fi.livi.digitraffic.tie.metadata.model.TmsSensorConstantValue;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioArvoVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioVO;

@Service
public class TmsStationSensorConstantService {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorConstantService.class);
    private final TmsSensorConstantDao tmsSensorConstantDao;
    private final TmsSensorConstantRepository tmsSensorConstantRepository;
    private final TmsSensorConstantValueRepository tmsSensorConstantValueRepository;

    @Autowired
    public TmsStationSensorConstantService(final TmsSensorConstantDao tmsSensorConstantDao,
                                           final TmsSensorConstantRepository tmsSensorConstantRepository,
                                           final TmsSensorConstantValueRepository tmsSensorConstantValueRepository) {

        this.tmsSensorConstantDao = tmsSensorConstantDao;
        this.tmsSensorConstantRepository = tmsSensorConstantRepository;
        this.tmsSensorConstantValueRepository = tmsSensorConstantValueRepository;
    }

    @Transactional
    public boolean updateSensorConstants(List<LamAnturiVakioVO> allLamAnturiVakios) {
        List<Long> ids = allLamAnturiVakios.stream().map(LamAnturiVakioVO::getId).collect(Collectors.toList());
        final int obsoleted = tmsSensorConstantDao.obsoleteSensorConstants(ids);
        final int upsert = tmsSensorConstantDao.updateSensorConstants(allLamAnturiVakios);
        log.info("updateSensorConstants upsert={}, obsoleted={}", upsert, obsoleted);
        return obsoleted > 0 || upsert > 0;
    }

    @Transactional
    public boolean updateSensorConstantValues(List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos) {
        final List<Long> ids = allLamAnturiVakioArvos.stream().map(LamAnturiVakioArvoVO::getId).collect(Collectors.toList());
        final int obsoleted = tmsSensorConstantDao.obsoleteSensorConstantValues(ids);
        final int upsert = tmsSensorConstantDao.updateSensorConstantValues(allLamAnturiVakioArvos);
        log.info("updateSensorConstantValues upsert={}, obsoleted={}", upsert, obsoleted);
        return obsoleted > 0 || upsert > 0;
    }

    @Transactional
    public int updateFreeFlowSpeedsOfTmsStations() {
        return tmsSensorConstantDao.updateFreeFlowSpeedsOfTmsStations();
    }

    @Transactional(readOnly = true)
    public List<TmsSensorConstant> findAllTmsStationsSensorConstants() {
        return tmsSensorConstantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TmsSensorConstant> findAllPublishableTmsStationsSensorConstants() {
        return tmsSensorConstantRepository.findByObsoleteDateIsNull();
    }

    @Transactional(readOnly = true)
    public List<TmsSensorConstantValue> findAllTmsStationsSensorConstantValues() {
        return tmsSensorConstantValueRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TmsSensorConstantValue> findAllPublishableTmsStationsSensorConstantValues() {
        return tmsSensorConstantValueRepository.findByObsoleteDateIsNull();
    }
}
