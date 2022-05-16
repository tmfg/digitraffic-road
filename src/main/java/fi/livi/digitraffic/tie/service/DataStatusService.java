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
        log.debug("method=updateDataUpdated dataType={}", dataType);
        dataUpdatedRepository.upsertDataUpdated(dataType);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final String extension) {
        log.debug("method=updateDataUpdated dataType={}, extension={}", dataType, extension);
        dataUpdatedRepository.upsertDataUpdated(dataType, extension);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final Instant updated) {
        log.debug("method=updateDataUpdated dataType={}, updatedTime={}", dataType, updated);
        dataUpdatedRepository.upsertDataUpdated(dataType, DataUpdatedRepository.UNSET_EXTENSION, updated);
    }

    @Transactional(readOnly = true)
    public ZonedDateTime findDataUpdatedTime(final DataType dataType) {
        return DateHelper.toZonedDateTimeAtUtc(dataUpdatedRepository.findUpdatedTime(dataType));
    }

    @Transactional(readOnly = true)
    public ZonedDateTime findDataUpdatedTime(final DataType...dataTypes) {
        return DateHelper.toZonedDateTimeAtUtc(dataUpdatedRepository.findUpdatedTime(dataTypes));
    }

    @Transactional(readOnly = true)
    public Instant findDataUpdatedTime(final DataType dataType, final List<String> extensions) {
        return dataUpdatedRepository.findUpdatedTime(dataType, extensions);
    }

    @Transactional(readOnly = true)
    public Instant getTransactionStartTime() {
        return dataUpdatedRepository.getTransactionStartTime();
    }
}
