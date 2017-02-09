package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaLaskennallinenAnturiVO;

@Service
public class LotjuWeatherStationMetadataService extends AbstractLotjuMetadataService {

    private static final Logger log = LoggerFactory.getLogger(LotjuWeatherStationMetadataService.class);
    private final LotjuWeatherStationMetadataClient lotjuWeatherStationClient;

    @Autowired
    public LotjuWeatherStationMetadataService(final LotjuWeatherStationMetadataClient lotjuWeatherStationClient) {
        super(lotjuWeatherStationClient.isEnabled());
        this.lotjuWeatherStationClient = lotjuWeatherStationClient;
    }

    public List<TiesaaAsemaVO> getTiesaaAsemmas() {
        return lotjuWeatherStationClient.getTiesaaAsemmas();
    }

    public Map<Long, List<TiesaaLaskennallinenAnturiVO>> getTiesaaLaskennallinenAnturisMappedByAsemaLotjuId(final Set<Long> tiesaaAsemaLotjuIds) {
        log.info("Fetching TiesaaLaskennallinenAnturis for {} TiesaaAsemas", tiesaaAsemaLotjuIds.size());

        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> tiesaaAnturisMappedByRwsLotjuId = new HashMap<>();

        StopWatch start = StopWatch.createStarted();
        ExecutorService executor = Executors.newFixedThreadPool(1 );
        CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        tiesaaAsemaLotjuIds.forEach(id -> {
            completionService.submit(new LaskennallisetAnturitFetcher(id, tiesaaAnturisMappedByRwsLotjuId));
        });

        final AtomicInteger countAnturis = new AtomicInteger();
        tiesaaAsemaLotjuIds.forEach(c -> {
            try {
                Future<Integer> f = completionService.take();
                countAnturis.addAndGet(f.get());
            } catch (InterruptedException e) {
                log.error("Error while fetching esiasentos", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                log.error("Error while fetching esiasentos", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        log.info("Fetched {} Anturits, took {} ms", countAnturis, start.getTime());
        return tiesaaAnturisMappedByRwsLotjuId;
    }

    public List<TiesaaLaskennallinenAnturiVO> getAllTiesaaLaskennallinenAnturis() {
        return lotjuWeatherStationClient.getAllTiesaaLaskennallinenAnturis();
    }

    private class LaskennallisetAnturitFetcher implements Callable<Integer> {

        private final Long tiesaaAsemaLotjuId;
        private final Map<Long, List<TiesaaLaskennallinenAnturiVO>> tiesaaAnturisMappedByRwsLotjuId;

        public LaskennallisetAnturitFetcher(Long tiesaaAsemaLotjuId, final Map<Long, List<TiesaaLaskennallinenAnturiVO>> tiesaaAnturisMappedByRwsLotjuId) {
            this.tiesaaAsemaLotjuId = tiesaaAsemaLotjuId;
            this.tiesaaAnturisMappedByRwsLotjuId = tiesaaAnturisMappedByRwsLotjuId;
        }

        @Override
        public Integer call() throws Exception {
            final List<TiesaaLaskennallinenAnturiVO> anturis = lotjuWeatherStationClient.getTiesaaAsemanLaskennallisetAnturit(tiesaaAsemaLotjuId);
            tiesaaAnturisMappedByRwsLotjuId.put(tiesaaAsemaLotjuId, anturis);
            return anturis.size();
        }
    }

}
