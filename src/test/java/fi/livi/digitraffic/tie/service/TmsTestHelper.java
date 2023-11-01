package fi.livi.digitraffic.tie.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.tms.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;

@Service
public class TmsTestHelper {

    @Autowired
    private TmsSensorConstantDao tmsSensorConstantDao;

    public LamAnturiVakioArvoVO createAndSaveLamAnturiVakioArvo(final LamAnturiVakioVO vakio, final Integer vakioArvo) {
        final LamAnturiVakioArvoVO arvo = TestUtils.createLamAnturiVakioArvo(vakio.getId(),101,1231, vakioArvo);
        tmsSensorConstantDao.updateSensorConstantValues(Collections.singletonList(arvo));
        return arvo;
    }

    public LamAnturiVakioVO createAndSaveLamAnturiVakio(final Long tmsStationlotjuId,  final String vakioNimi) {
        final LamAnturiVakioVO vakio = TestUtils.createLamAnturiVakio(tmsStationlotjuId, vakioNimi);
        tmsSensorConstantDao.updateSensorConstants(Collections.singletonList(vakio));
        return vakio;
    }
}