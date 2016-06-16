package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.LamMeasurementRepository;
import fi.livi.digitraffic.tie.data.dto.lam.LamMeasurementDto;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;

@Service
public class LamDataServiceImpl implements LamDataService {
    private final LamMeasurementRepository lamMeasurementRepository;

    @Autowired
    public LamDataServiceImpl(final LamMeasurementRepository lamMeasurementRepository) {
        this.lamMeasurementRepository = lamMeasurementRepository;
    }

    // TODO onlyUpdateInfo: do direct query to get update info
    @Override
    @Transactional(readOnly = true)
    public LamRootDataObjectDto listAllLamDataFromNonObsoleteStations(boolean onlyUpdateInfo) {
        List<LamMeasurementDto> all = lamMeasurementRepository.listAllLamDataFromNonObsoleteStations();
        LocalDateTime updated = all.size() > 0 ? all.get(0).getMeasured() : null;

        return new LamRootDataObjectDto(
                onlyUpdateInfo ? null : all,
                updated);
    }
}
