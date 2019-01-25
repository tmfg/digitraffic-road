package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.metadata.dao.TmsSensorConstantValueRepository;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioArvoVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioVO;

@Service
public class TmsStationSensorConstantService {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorConstantService.class);
    private final TmsSensorConstantDao tmsSensorConstantDao;
    private final TmsSensorConstantValueRepository tmsSensorConstantValueRepository;

    @Autowired
    public TmsStationSensorConstantService(final TmsSensorConstantDao tmsSensorConstantDao,
                                           final TmsSensorConstantValueRepository tmsSensorConstantValueRepository) {

        this.tmsSensorConstantDao = tmsSensorConstantDao;
        this.tmsSensorConstantValueRepository = tmsSensorConstantValueRepository;
    }

    @Transactional
    public boolean updateSensorConstants(List<LamAnturiVakioVO> allLamAnturiVakios) {
        List<Long> ids = allLamAnturiVakios.stream().map(LamAnturiVakioVO::getId).collect(Collectors.toList());
        final int obsoleted = tmsSensorConstantDao.obsoleteSensorConstants(ids);
        final int upsert = tmsSensorConstantDao.updateSensorConstants(allLamAnturiVakios);
        log.info("updateSensorConstants obsoleted={}, upsert={}", obsoleted, upsert);
        return obsoleted > 0 || upsert > 0;
    }

    @Transactional
    public int updateSensorConstantValues(List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos) {
        return tmsSensorConstantDao.updateSensorConstantValues(allLamAnturiVakioArvos);
    }

    @Transactional
    public int updateFreeFlowSpeedsOfTmsStations() {
        return tmsSensorConstantDao.updateFreeFlowSpeedsOfTmsStations();
    }
}
