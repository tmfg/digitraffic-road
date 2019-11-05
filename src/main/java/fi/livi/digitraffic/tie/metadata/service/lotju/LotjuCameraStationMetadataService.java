package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.AbstractVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraVO;

@ConditionalOnNotWebApplication
@Service
public class LotjuCameraStationMetadataService {
    private static final Logger log = LoggerFactory.getLogger(LotjuCameraStationMetadataService.class);
    private final LotjuCameraStationMetadataClient lotjuCameraStationClient;

    @Autowired
    public LotjuCameraStationMetadataService(final LotjuCameraStationMetadataClient lotjuCameraStationClient) {
        this.lotjuCameraStationClient = lotjuCameraStationClient;
    }

    public Set<Long> getKamerasLotjuids() {
        return lotjuCameraStationClient.getKameras().stream().map(AbstractVO::getId).collect(Collectors.toSet());
    }

    public KameraVO getKamera(final long kameraLotjuId) {
        return lotjuCameraStationClient.getKamera(kameraLotjuId);
    }

    public List<EsiasentoVO> getEsiasentos(final Long kameraId) {
        return lotjuCameraStationClient.getEsiasentos(kameraId);
    }

    public EsiasentoVO getEsiasento(final long esiasentoLotjuId) {
        return lotjuCameraStationClient.getEsiasento(esiasentoLotjuId);
    }
}
