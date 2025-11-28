package fi.livi.digitraffic.tie.service.data;

import fi.livi.digitraffic.tie.dao.data.DataIncomingRepository;
import fi.livi.digitraffic.tie.model.data.DataIncoming;

import fi.livi.digitraffic.tie.model.data.IncomingDataTypes;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataUpdatingService {
    private final ImsUpdatingService imsUpdatingService;
    private final DataIncomingRepository dataIncomingRepository;

    private static final Logger log = LoggerFactory.getLogger(DataUpdatingService.class);

    public DataUpdatingService(final ImsUpdatingService imsUpdatingService, final DataIncomingRepository dataIncomingRepository) {
        this.imsUpdatingService = imsUpdatingService;
        this.dataIncomingRepository = dataIncomingRepository;
    }

    @Transactional
    public void insertData(final DataIncoming dataIncoming) {
        dataIncomingRepository.save(dataIncoming);
    }

    @Transactional()
    public void handleNewData() {
        final StopWatch stopWatch = StopWatch.createStarted();
        final var unhandled = dataIncomingRepository.findAllUnhandled();

        try {
            unhandled.forEach(data -> {
                try {
                    if (data.getType().equals(IncomingDataTypes.DataType.IMS)) {
                        imsUpdatingService.handleIms(data);
                        // handle datex2, with version
                        data.setProcessed();
                    } else {
                        log.error("method=handleNewData invalid type: {}", data.getType());
                        data.setFailed();
                    }
                } catch (final Exception e) {
                    log.error("method=handleNewData failed", e);
                    data.setFailed();
                }
            });
        } finally {
            log.info("method=handleNewData updatedCount={} tookMs={}",
                    unhandled.size(),
                    stopWatch.getTime());
        }

    }
}
