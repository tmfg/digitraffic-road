package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;

@ConditionalOnNotWebApplication
@Component
public class LotjuTmsStationMetadataClientWrapper {

    private static final Logger log = LoggerFactory.getLogger(LotjuTmsStationMetadataClientWrapper.class);
    private final LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    @Autowired
    public LotjuTmsStationMetadataClientWrapper(final LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient) {
        this.lotjuTmsStationMetadataClient = lotjuTmsStationMetadataClient;
    }

    public List<LamAsemaVO> getLamAsemas() {
        return lotjuTmsStationMetadataClient.getLamAsemas();
    }

    public LamAsemaVO getLamAsema(final long tmsStationLotjuId) {
        return lotjuTmsStationMetadataClient.getLamAsema(tmsStationLotjuId);
    }

    public LamLaskennallinenAnturiVO getLamLaskennallinenAnturi(final long lotjuId) {
        return lotjuTmsStationMetadataClient.getLamLaskennallinenAnturi(lotjuId);
    }
    public List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        return lotjuTmsStationMetadataClient.getAllLamLaskennallinenAnturis();
    }

    public List<LamLaskennallinenAnturiVO> getLamAsemanLaskennallisetAnturit(final long tmsLotjuId) {
        return lotjuTmsStationMetadataClient.getLamAsemanLaskennallisetAnturit(tmsLotjuId);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 800000, maxErroExcecutionTime = 1000000)
    public Map<Long, List<LamLaskennallinenAnturiVO>> getLamLaskennallinenAnturisMappedByAsemaLotjuId(final Set<Long> tmsLotjuIds) {
        final ConcurrentMap<Long, List<LamLaskennallinenAnturiVO>> lamAnturisMappedByTmsLotjuId = new ConcurrentHashMap<>();

        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final CompletionService<Pair<Long, List<LamLaskennallinenAnturiVO>>> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(() -> Pair.of(tmsLotjuId, lotjuTmsStationMetadataClient.getLamAsemanLaskennallisetAnturit(tmsLotjuId)));
        }

        int countAnturis = 0;
        // It's necessary to wait all executors to complete.
        int handledCount = 0;
        while (handledCount < tmsLotjuIds.size()) {
            handledCount++;
            try {
                final Future<Pair<Long, List<LamLaskennallinenAnturiVO>>> f = completionService.take();
                final Long tmsLotjuId = f.get().getKey();
                final List<LamLaskennallinenAnturiVO> anturis = f.get().getValue();
                lamAnturisMappedByTmsLotjuId.put(tmsLotjuId, anturis);
                countAnturis += anturis.size();
                log.debug("Got {} anturis", anturis.size());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamLaskennallinenAnturis", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();

        log.info("lamFetchedCount={} LamLaskennallinenAnturis for lamStationCount={} LAMAsemas, tookMs={}",
                 countAnturis, lamAnturisMappedByTmsLotjuId.size(), start.getTime());
        return lamAnturisMappedByTmsLotjuId;
    }

    public LamAnturiVakioVO getLamAnturiVakio(final long anturiVakiolotjuId) {
        return lotjuTmsStationMetadataClient.getLamAnturiVakio(anturiVakiolotjuId);
    }


    @PerformanceMonitor(maxWarnExcecutionTime = 150000, maxErroExcecutionTime = 200000)
    public List<LamAnturiVakioVO> getAllLamAnturiVakios(final Collection<Long> tmsLotjuIds) {

        final List<LamAnturiVakioVO> allAnturiVakios = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final CompletionService<List<LamAnturiVakioVO>> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(() -> lotjuTmsStationMetadataClient.getAsemanAnturiVakios(tmsLotjuId));
        }

        // It's necessary to wait all executors to complete.
        int handledCount = 0;
        while (handledCount < tmsLotjuIds.size()) {
            handledCount++;
            try {
                final List<LamAnturiVakioVO> values = completionService.take().get();
                allAnturiVakios.addAll(values);
                log.debug("Got {} AnturiVakios", values.size());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamAnturiVakios", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();

        log.info("method=getAllLamAnturiVakios fetchedCount={} for lamStationCount={} tookMs={}",
                 allAnturiVakios.size(), tmsLotjuIds.size(), start.getTime());
        return allAnturiVakios;
    }

    public List<LamAnturiVakioArvoVO> getAnturiVakioArvos(final long anturiVakioArvoLotjuId) {
        final List<LamAnturiVakioArvoVO> lamAnturiVakioArvos = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final CompletionService<LamAnturiVakioArvoVO> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        IntStream.range(1,13).forEach(month ->
            completionService.submit(() -> lotjuTmsStationMetadataClient.getAnturiVakioArvot(anturiVakioArvoLotjuId, month, 1)));
        log.info("method=getAnturiVakioArvos for 12 months");

        // It's necessary to wait all executors to complete.
        IntStream.range(1,13).forEach(month -> {
            try {
                final LamAnturiVakioArvoVO values = completionService.take().get();
                lamAnturiVakioArvos.add(values);
                log.debug("method=getAnturiVakioArvos {}/12", lamAnturiVakioArvos.size());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("method=getAnturiVakioArvos Error while fetching LamAnturiVakioArvo", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        final List<LamAnturiVakioArvoVO> distincLamAnturiVakios = filterDistinctLamAnturiVakioArvos(lamAnturiVakioArvos);

        log.info("method=getAnturiVakioArvos fetchedCount={} for 12 months distincLamAnturiVakiosCount={} tookMs={}",
                 lamAnturiVakioArvos.size(), distincLamAnturiVakios.size(), start.getTime());
        return distincLamAnturiVakios;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 120000, maxErroExcecutionTime = 200000)
    public List<LamAnturiVakioArvoVO> getAllLamAnturiVakioArvos() {

        final List<LamAnturiVakioArvoVO> lamAnturiVakioArvos = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final CompletionService<List<LamAnturiVakioArvoVO>> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        int monthCounter = 0;
        while (monthCounter < 12) {
            monthCounter++;
            final int month = monthCounter;
            completionService.submit(() -> lotjuTmsStationMetadataClient.getAllAnturiVakioArvos(month, 1));
        }
        log.info("method=getAllLamAnturiVakioArvos Fetch LamAnturiVakioArvos for {} months", monthCounter);

        // It's necessary to wait all executors to complete.
        for (int i = 0; i < monthCounter; i++) {
            try {
                final List<LamAnturiVakioArvoVO> values = completionService.take().get();
                lamAnturiVakioArvos.addAll(values);
                log.debug("method=getAllLamAnturiVakioArvos Got {} LamAnturiVakioArvos, {}/{}", values.size(), i+1, monthCounter);
            } catch (final InterruptedException | ExecutionException e) {
                log.error("method=getAllLamAnturiVakioArvos Error while fetching LamAnturiVakioArvos", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();

        final List<LamAnturiVakioArvoVO> distincLamAnturiVakios = filterDistinctLamAnturiVakioArvos(lamAnturiVakioArvos);

        log.info("method=getAllLamAnturiVakioArvos fetchedCount={} for monthCount={} distincLamAnturiVakiosCount={} tookMs={}",
                 lamAnturiVakioArvos.size(), monthCounter, distincLamAnturiVakios.size(), start.getTime());
        return distincLamAnturiVakios;
    }

    /**
     * When LamAnturiVakioArvos are fetched for every month there is distinct values ie.
     * values that are valid from 6 x 1.1.–30.6. and 6 x 1.7.–31.12. So return value will
     * contain only 1 x 1.1.–30.6. and 1 x 1.7.–31.12.
     *
     * Returns distinct LamAnturiVakioArvo
     * @param lamAnturiVakioArvos values to reduce
     * @return distinct values
     */
    private List<LamAnturiVakioArvoVO> filterDistinctLamAnturiVakioArvos(final List<LamAnturiVakioArvoVO> lamAnturiVakioArvos) {
        return lamAnturiVakioArvos.parallelStream()
            .map(LamAnturiVakioArvoWrapper::new)
            .distinct()
            .map(LamAnturiVakioArvoWrapper::unWrap)
            .collect(Collectors.toList());
    }

    private class LamAnturiVakioArvoWrapper {

        private final LamAnturiVakioArvoVO vakio;

        public LamAnturiVakioArvoWrapper(LamAnturiVakioArvoVO vakio) {
            this.vakio = vakio;
        }

        public LamAnturiVakioArvoVO unWrap() {
            return vakio;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LamAnturiVakioArvoVO other = ((LamAnturiVakioArvoWrapper)o).unWrap();

            boolean equals = new EqualsBuilder()
                .append(other.getAnturiVakioId(), vakio.getAnturiVakioId())
                .append(other.getVoimassaAlku(), vakio.getVoimassaAlku())
                .append(other.getVoimassaLoppu(), vakio.getVoimassaLoppu())
                .isEquals();
            if (equals && !new EqualsBuilder().append(other.getArvo(), vakio.getArvo()).isEquals()) {
                log.error("LOTJU returns unequal values for same AnturiVakioArvo {} vs {}", ToStringHelper.toStringFull(vakio), ToStringHelper.toStringFull(other));
            }
            return equals;
        }

        public int hashCode() {
            return new HashCodeBuilder()
                .append(vakio.getAnturiVakioId())
                .append(vakio.getArvo())
                .append(vakio.getVoimassaAlku())
                .append(unWrap().getVoimassaLoppu())
                .toHashCode();
        }
    }
}
