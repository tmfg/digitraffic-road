package fi.livi.digitraffic.tie.service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.DataUpdatedRepository;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.DataType;

@Service
public class DataStatusService {
    private static final Logger log = LoggerFactory.getLogger(DataStatusService.class);

    private final DataUpdatedRepository dataUpdatedRepository;

    @Autowired
    public DataStatusService(final DataUpdatedRepository dataUpdatedRepository) {
        this.dataUpdatedRepository = dataUpdatedRepository;
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType) {
        dataUpdatedRepository.upsertDataUpdated(dataType);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final String subtype) {
        dataUpdatedRepository.upsertDataUpdated(dataType, subtype);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final Instant updated) {
        dataUpdatedRepository.upsertDataUpdated(dataType, updated);
    }

    @Transactional(readOnly = true)
    public ZonedDateTime findDataUpdatedTime(final DataType dataType) {
        return DateHelper.toZonedDateTimeAtUtc(dataUpdatedRepository.findUpdatedTime(dataType));
    }

    @Transactional(readOnly = true)
    public Instant findDataUpdatedInstant(final DataType dataType) {
        return dataUpdatedRepository.findUpdatedTime(dataType);
    }

    @Transactional(readOnly = true)
    public Instant findDataUpdatedTime(final DataType dataType, final List<String> subtypes) {
        return dataUpdatedRepository.findUpdatedTime(dataType, subtypes);
    }

    @Transactional(readOnly = true)
    public Instant getTransactionStartTime() {
        return dataUpdatedRepository.getTransactionStartTime();
    }
}
