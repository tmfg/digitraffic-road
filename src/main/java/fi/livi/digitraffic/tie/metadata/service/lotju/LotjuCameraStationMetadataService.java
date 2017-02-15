package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
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
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;

@Service
public class LotjuCameraStationMetadataService extends AbstractLotjuMetadataService {

    private static final Logger log = LoggerFactory.getLogger(LotjuCameraStationMetadataService.class);
    private final LotjuCameraStationMetadataClient lotjuCameraStationClient;

    @Autowired
    public LotjuCameraStationMetadataService(final LotjuCameraStationMetadataClient lotjuCameraStationClient) {
        super(lotjuCameraStationClient.isEnabled());
        this.lotjuCameraStationClient = lotjuCameraStationClient;
    }

    public Map<Long, Pair<KameraVO, List<EsiasentoVO>>> getLotjuIdToKameraAndEsiasentoMap() {

        final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> kameraAndEsiasentosPairMappedByKameraLotjuId = new HashMap<>();

        log.info("Fetch Kameras");
        final List<KameraVO> kamerat = lotjuCameraStationClient.getKameras();
        log.info("Fetched " + kamerat.size() + " Kameras");
        log.info("Fetch Esiasentos for Kameras");

        StopWatch start = StopWatch.createStarted();
        final AtomicInteger countEsiasentos = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(3 );
        CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        for (final KameraVO kamera : kamerat) {
            completionService.submit(new EsiasentoFetcher(kamera, kameraAndEsiasentosPairMappedByKameraLotjuId));
        }

        kamerat.forEach(c -> {
            try {
                Future<Integer> f = completionService.take();
                countEsiasentos.addAndGet(f.get());
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

        log.info("Fetched {} Esiasentos for {} Kameras, took {} ms", countEsiasentos.get(), kameraAndEsiasentosPairMappedByKameraLotjuId.size(), start.getTime());
        return kameraAndEsiasentosPairMappedByKameraLotjuId;
    }

    public List<KameraVO> getKameras() {
        return lotjuCameraStationClient.getKameras();
    }

    private List<EsiasentoVO> getEsiasentos(Long kameraId) {
        return lotjuCameraStationClient.getEsiasentos(kameraId);
    }

    public boolean isEnabled() {
        return lotjuCameraStationClient.isEnabled();
    }

    private class EsiasentoFetcher implements Callable<Integer> {

        private final KameraVO kamera;
        private final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasentoMap;

        public EsiasentoFetcher(KameraVO kamera, final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> kameraAndEsiasentosPairMappedByKameraLotjuId) {
            this.kamera = kamera;
            this.lotjuIdToKameraAndEsiasentoMap = kameraAndEsiasentosPairMappedByKameraLotjuId;
        }

        @Override
        public Integer call() throws Exception {
            if (kamera.getVanhaId() != null) {
                final List<EsiasentoVO> esiasennot = getEsiasentos(kamera.getId());

                final String kameraId = CameraHelper.convertVanhaIdToKameraId(kamera.getVanhaId());
                esiasennot.stream().forEach(esiasento -> {

                    final String presetId = CameraHelper.convertCameraIdToPresetId(kameraId, esiasento.getSuunta());

                    if (CameraHelper.validatePresetId(presetId)) {
                        Pair<KameraVO, List<EsiasentoVO>> kameraPair = lotjuIdToKameraAndEsiasentoMap.get(kamera.getId());
                        if (kameraPair == null) {
                            kameraPair = Pair.of(kamera, new ArrayList<EsiasentoVO>());
                            lotjuIdToKameraAndEsiasentoMap.put(kamera.getId(), kameraPair);
                        }
                        kameraPair.getRight().add(esiasento);
                    } else {
                        log.error("Invalid cameraPresetId for {} and {}",
                                ToStringHelpper.toString(kamera),
                                ToStringHelpper.toString(esiasento));
                    }
                });
                return esiasennot.size();
            }

            log.error("Cannot update " + ToStringHelpper.toString(kamera) + " is invalid: has null vanhaId");
            return 0;
        }
    }

}
