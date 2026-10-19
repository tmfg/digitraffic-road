package fi.livi.digitraffic.tie.service.data;

import fi.livi.digitraffic.tie.dao.data.DataIncomingRepository;
import fi.livi.digitraffic.tie.model.data.DataIncoming;

import fi.livi.digitraffic.tie.model.data.IncomingDataTypes;

import org.apache.commons.lang3.StringUtils;
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
                        data.markProcessed();
                    } else {
                        log.error("method=handleNewData invalid type: {} dataId={} messageId={}",
                                data.getType(), data.getDataId(), data.getMessageId());
                        data.markFailed("Invalid data type " + data.getType());
                    }
                } catch (final Exception e) {
                    // Some exceptions carry no message, so fall back to the type to avoid an empty reason.
                    final String reason = StringUtils.defaultIfBlank(e.getMessage(), e.toString());

                    log.error("method=handleNewData failed dataId={} messageId={} reason={}",
                            data.getDataId(), data.getMessageId(), reason, e);
                    data.markFailed(reason);
                }
            });
        } finally {
            log.info("method=handleNewData updatedCount={} tookMs={}",
                    unhandled.size(),
                    stopWatch.getTime());
        }

    }
}
