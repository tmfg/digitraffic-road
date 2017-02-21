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

import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;

@Service
public class LotjuTmsStationMetadataService extends AbstractLotjuMetadataService {

    private static final Logger log = LoggerFactory.getLogger(LotjuTmsStationMetadataService.class);
    private final LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    @Autowired
    public LotjuTmsStationMetadataService(final LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient) {
        super(lotjuTmsStationMetadataClient.isEnabled());
        this.lotjuTmsStationMetadataClient = lotjuTmsStationMetadataClient;
    }

    public List<LamAsemaVO> getLamAsemas() {
        return lotjuTmsStationMetadataClient.getLamAsemas();
    }

    private List<LamLaskennallinenAnturiVO> getTiesaaLaskennallinenAnturis(final Long lamAsemaLotjuId) {
        return lotjuTmsStationMetadataClient.getTiesaaLaskennallinenAnturis(lamAsemaLotjuId);
    }

    public List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        return lotjuTmsStationMetadataClient.getAllLamLaskennallinenAnturis();
    }

    public Map<Long, List<LamLaskennallinenAnturiVO>> getTiesaaLaskennallinenAnturisMappedByAsemaLotjuId(Set<Long> tmsLotjuIds) {
        final Map<Long, List<LamLaskennallinenAnturiVO>> lamAnturisMappedByTmsLotjuId = new HashMap<>();
        final AtomicInteger countAnturis = new AtomicInteger();

        StopWatch start = StopWatch.createStarted();

        ExecutorService executor = Executors.newFixedThreadPool(3);
        CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(new LaskennallinenanturiFetcher(tmsLotjuId, lamAnturisMappedByTmsLotjuId));
        }

        tmsLotjuIds.forEach(id -> {
            try {
                Future<Integer> f = completionService.take();
                countAnturis.addAndGet(f.get());
                log.info("Got {} anturis", f.get());
            } catch (InterruptedException e) {
                log.error("Error while fetching LamLaskennallinenAnturis", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                log.error("Error while fetching LamLaskennallinenAnturis", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        log.info("Fetched {} LamLaskennallinenAnturis for {} LAMAsemas, took {} ms",
                countAnturis.get(), lamAnturisMappedByTmsLotjuId.size(), start.getTime());
        return lamAnturisMappedByTmsLotjuId;
    }

    private class LaskennallinenanturiFetcher implements Callable<Integer> {

        private Long tmsLotjuId;
        private Map<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturisMappedByTmsLotjuId;

        public LaskennallinenanturiFetcher(Long tmsLotjuId, final Map<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturisMappedByTmsLotjuId) {
            this.tmsLotjuId = tmsLotjuId;
            this.currentLamAnturisMappedByTmsLotjuId = currentLamAnturisMappedByTmsLotjuId;
        }

        @Override
        public Integer call() throws Exception {
            final List<LamLaskennallinenAnturiVO> anturis = lotjuTmsStationMetadataClient.getTiesaaLaskennallinenAnturis(tmsLotjuId);
            currentLamAnturisMappedByTmsLotjuId.put(tmsLotjuId, anturis);
            return anturis.size();
        }
    }
}
