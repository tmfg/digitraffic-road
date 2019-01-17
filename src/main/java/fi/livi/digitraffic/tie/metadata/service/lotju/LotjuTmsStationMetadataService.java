package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2017._05._02.LamLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.LamAsemaVO;

@Service
public class LotjuTmsStationMetadataService {

    private static final Logger log = LoggerFactory.getLogger(LotjuTmsStationMetadataService.class);
    private final LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    @Autowired
    public LotjuTmsStationMetadataService(final LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient) {
        this.lotjuTmsStationMetadataClient = lotjuTmsStationMetadataClient;
    }

    public List<LamAsemaVO> getLamAsemas() {
        return lotjuTmsStationMetadataClient.getLamAsemas();
    }

    public List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        return lotjuTmsStationMetadataClient.getAllLamLaskennallinenAnturis();
    }

//    TODO @PerformanceMonitor(maxErroExcecutionTime = , maxWarnExcecutionTime = )
    public Map<Long, List<LamLaskennallinenAnturiVO>> getLamLaskennallinenAnturisMappedByAsemaLotjuId(final Set<Long> tmsLotjuIds) {
        final ConcurrentMap<Long, List<LamLaskennallinenAnturiVO>> lamAnturisMappedByTmsLotjuId = new ConcurrentHashMap<>();

        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(new LaskennallinenanturiFetcher(tmsLotjuId, lamAnturisMappedByTmsLotjuId));
        }

        final AtomicInteger countAnturis = new AtomicInteger();
        // Tämä laskenta on välttämätön, jotta executor suorittaa loppuun jokaisen submitatun taskin.
        tmsLotjuIds.forEach(id -> {
            try {
                final Future<Integer> f = completionService.take();
                countAnturis.addAndGet(f.get());
                log.debug("Got {} anturis", f.get());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamLaskennallinenAnturis", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        log.info("lamFetchedCount={} LamLaskennallinenAnturis for lamStationCount={} LAMAsemas, tookMs={}",
                countAnturis.get(), lamAnturisMappedByTmsLotjuId.size(), start.getTime());
        return lamAnturisMappedByTmsLotjuId;
    }

    public Map<Long, List<LamAnturiVakioVO>> getLamAnturiVakiosMappedByAsemaLotjuId(final Set<Long> tmsLotjuIds) {
        final ConcurrentMap<Long, List<LamAnturiVakioVO>> lamAnturiVakiosMappedByTmsLotjuId = new ConcurrentHashMap<>();

        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(new AnturiVakioFetcher(tmsLotjuId, lamAnturiVakiosMappedByTmsLotjuId));
        }

        final AtomicInteger countAnturiVakios = new AtomicInteger();
        // Tämä laskenta on välttämätön, jotta executor suorittaa loppuun jokaisen submitatun taskin.
        tmsLotjuIds.forEach(id -> {
            try {
                final Future<Integer> f = completionService.take();
                countAnturiVakios.addAndGet(f.get());
                log.debug("Got {} AnturiVakios", f.get());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamAnturiVakios", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        log.info("lamFetchedCount={} LamAnturiVakios for lamStationCount={} LAMAsemas, tookMs={}",
            countAnturiVakios.get(), lamAnturiVakiosMappedByTmsLotjuId.size(), start.getTime());
        return lamAnturiVakiosMappedByTmsLotjuId;
    }


    private class LaskennallinenanturiFetcher implements Callable<Integer> {

        private final Long tmsLotjuId;
        private final ConcurrentMap<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturisMappedByTmsLotjuId;

        public LaskennallinenanturiFetcher(final Long tmsLotjuId, final ConcurrentMap<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturisMappedByTmsLotjuId) {
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

    private class AnturiVakioFetcher implements Callable<Integer> {

        private final Long tmsLotjuId;
        private final ConcurrentMap<Long, List<LamAnturiVakioVO>> currentLamAnturiVakiosMappedByTmsLotjuId;

        public AnturiVakioFetcher(final Long tmsLotjuId, final ConcurrentMap<Long, List<LamAnturiVakioVO>> currentLamAnturiVakiosMappedByTmsLotjuId) {
            this.tmsLotjuId = tmsLotjuId;
            this.currentLamAnturiVakiosMappedByTmsLotjuId = currentLamAnturiVakiosMappedByTmsLotjuId;
        }

        @Override
        public Integer call() throws Exception {
            final List<LamAnturiVakioVO> anturis = lotjuTmsStationMetadataClient.getAsemanAnturiVakios(tmsLotjuId);
            currentLamAnturiVakiosMappedByTmsLotjuId.put(tmsLotjuId, anturis);
            return anturis.size();
        }
    }
}
