package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuKameraPerustiedotServiceEndpoint;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.Julkisuus;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.KeruunTILA;
import fi.livi.ws.wsdl.lotju.metatiedot._2015._09._29.TieosoiteVO;

public class CameraStationUpdaterTest extends AbstractTest {
    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Autowired
    private LotjuKameraPerustiedotServiceEndpoint lotjuKameraPerustiedotServiceMock;


    @Test
    @Transactional
    @Rollback
    public void updateCamerasSwitchPresetIds() {
        lotjuKameraPerustiedotServiceMock.initDataAndService();

        // initial state cameras with lotjuId 443 has public and non public presets, 121 has 2 and 56 has 1 non public preset
        cameraStationUpdater.updateCameras();

        final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> map = createMap();

        cameraStationUpdater.updateCamerasAndPresets(map);
    }

    private Map<Long, Pair<KameraVO, List<EsiasentoVO>>> createMap() {
        final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> map = new HashMap<>();

        map.put(1L, Pair.of(createKamera(), createEsiasentoList()));

        return map;
    }

    private List<EsiasentoVO> createEsiasentoList() {
        final EsiasentoVO e1 = new EsiasentoVO();

        e1.setKameraId(121);
        e1.setId(1663L);
        e1.setSuunta("2");
        e1.setJulkisuus(Julkisuus.JULKINEN);
        e1.setKeruussa(true);

        final EsiasentoVO e2 = new EsiasentoVO();

        e1.setKameraId(121);
        e2.setId(1781L);
        e2.setSuunta("1");
        e2.setJulkisuus(Julkisuus.JULKINEN);
        e2.setKeruussa(true);

        return Arrays.asList(e1, e2);
    }

    private KameraVO createKamera() {
        final KameraVO k = new KameraVO();
        final TieosoiteVO t1 = new TieosoiteVO();

        k.setId(121L);
        k.setVanhaId(1628);
        k.setKeruunTila(KeruunTILA.KERUUSSA);
        k.setTieosoite(t1);
        k.setNimi("vt7_Loviisa_Länsi");

        return k;
    }
}
