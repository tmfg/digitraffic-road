package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.util.Arrays;
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
}
