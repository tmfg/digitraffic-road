package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;

@Service
public class LotjuCameraStationMetadataService {

    private static final Logger log = LoggerFactory.getLogger(LotjuCameraStationMetadataService.class);
    private final LotjuCameraStationMetadataClient lotjuCameraStationClient;

    @Autowired
    public LotjuCameraStationMetadataService(final LotjuCameraStationMetadataClient lotjuCameraStationClient) {
        this.lotjuCameraStationClient = lotjuCameraStationClient;
    }

    public Map<Long, Pair<KameraVO, List<EsiasentoVO>>> getLotjuIdToKameraAndEsiasentoMap() {

        final ConcurrentMap<Long, Pair<KameraVO, List<EsiasentoVO>>> kameraAndEsiasentosPairMappedByKameraLotjuId = new ConcurrentHashMap<>();

        log.info("Fetch Cameras");
        final List<KameraVO> kamerat = lotjuCameraStationClient.getKameras();
        log.info("method=getLotjuIdToKameraAndEsiasentoMap cameraFetchedCount={} Cameras", kamerat.size());
        log.info("Fetch Presets for Cameras");

        final StopWatch start = StopWatch.createStarted();
        final ExecutorService executor = Executors.newFixedThreadPool(1 );
        final CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        for (final KameraVO kamera : kamerat) {
            completionService.submit(new EsiasentoFetcher(kamera, kameraAndEsiasentosPairMappedByKameraLotjuId));
        }

        final AtomicInteger countEsiasentos = new AtomicInteger();
        // Tämä laskenta on välttämätön, jotta executor suorittaa loppuun jokaisen submitatun taskin.
        kamerat.forEach(c -> {
            try {
                final Future<Integer> f = completionService.take();
                countEsiasentos.addAndGet(f.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error while fetching esiasentos", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        log.info("presetFetchedCount={} Esiasentos for cameraCount={} Cameras, tookMs={}", countEsiasentos.get(), kameraAndEsiasentosPairMappedByKameraLotjuId.size(), start.getTime());
        return kameraAndEsiasentosPairMappedByKameraLotjuId;
    }

    public List<KameraVO> getKameras() {
        return lotjuCameraStationClient.getKameras();
    }

    private List<EsiasentoVO> getEsiasentos(final Long kameraId) {
        return lotjuCameraStationClient.getEsiasentos(kameraId);
    }

    private class EsiasentoFetcher implements Callable<Integer> {

        private final KameraVO kamera;
        private final ConcurrentMap<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasentoMap;

        EsiasentoFetcher(final KameraVO kamera, final ConcurrentMap<Long, Pair<KameraVO, List<EsiasentoVO>>> kameraAndEsiasentosPairMappedByKameraLotjuId) {
            this.kamera = kamera;
            this.lotjuIdToKameraAndEsiasentoMap = kameraAndEsiasentosPairMappedByKameraLotjuId;
        }

        @Override
        public Integer call() throws Exception {
            if (kamera.getVanhaId() != null) {
                final List<EsiasentoVO> esiasennot = getEsiasentos(kamera.getId());

                final String kameraId = CameraHelper.convertVanhaIdToKameraId(kamera.getVanhaId());
                esiasennot.forEach(esiasento -> {

                    final String presetId = CameraHelper.convertCameraIdToPresetId(kameraId, esiasento.getSuunta());

                    if (CameraHelper.validatePresetId(presetId)) {
                        final Pair<KameraVO, List<EsiasentoVO>> kameraPair = lotjuIdToKameraAndEsiasentoMap
                            .computeIfAbsent(kamera.getId(), k -> Pair.of(kamera, new CopyOnWriteArrayList<>()));
                        kameraPair.getRight().add(esiasento);
                    } else {
                        log.error("Invalid cameraPresetId for {} and {}",
                                ToStringHelper.toString(kamera),
                                ToStringHelper.toString(esiasento));
                    }
                });
                return esiasennot.size();
            }

            log.error("Cannot update {}. It has null vanhaId", ToStringHelper.toString(kamera));
            return 0;
        }
    }

}
