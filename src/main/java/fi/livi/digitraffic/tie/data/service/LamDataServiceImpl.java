package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LamMeasurementRepository;
import fi.livi.digitraffic.tie.data.dto.lam.LamMeasurementDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationService;

@Service
public class LamDataServiceImpl implements LamDataService {
    private static final Logger log = LoggerFactory.getLogger(LamDataServiceImpl.class);

    private final LamMeasurementRepository lamMeasurementRepository;
    private final LamStationService lamStationService;

    @Autowired
    public LamDataServiceImpl(final LamMeasurementRepository lamMeasurementRepository,
                              final LamStationService lamStationService) {
        this.lamMeasurementRepository = lamMeasurementRepository;
        this.lamStationService = lamStationService;
    }

    @Override
    @Transactional(readOnly = true)
    public LamRootDataObjectDto listPublicLamData(final boolean onlyUpdateInfo) {
        final LocalDateTime updated = lamMeasurementRepository.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            return new LamRootDataObjectDto(updated);
        } else {
            final List<LamMeasurementDto> all = lamMeasurementRepository.listAllLamDataFromNonObsoleteStations();

            return new LamRootDataObjectDto(all, updated);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LamRootDataObjectDto listPublicLamData(long id) {
        final LocalDateTime updated = lamMeasurementRepository.getLatestMeasurementTime();
        final LamMeasurementDto dto = lamMeasurementRepository.getLamDataFromStation(id);

        return new LamRootDataObjectDto(Arrays.asList(dto), updated);
    }

    @Override
    public void updateLamData(Lam data) {
        log.info("Update road weather with station lotjuId: " + data.getAsemaId());
        long lamStationLotjuId = data.getAsemaId();
        LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTimeAtZone(data.getAika(), ZoneId.systemDefault());

        LamStation rws =
                lamStationService.findByLotjuId(lamStationLotjuId);

        if (rws == null) {
            log.warn("LamStation not found for " + ToStringHelpper.toString(data));
            return;
        }
        // TODO sensorvalue updates
    }
}
