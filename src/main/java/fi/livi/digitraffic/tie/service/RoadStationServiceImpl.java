package fi.livi.digitraffic.tie.service;

import fi.livi.digitraffic.tie.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadStationServiceImpl implements RoadStationService {

    private static final Logger LOG = Logger.getLogger(RoadStationServiceImpl.class);

    private final RoadStationRepository roadStationRepository;

    @Autowired
    public RoadStationServiceImpl(final RoadStationRepository roadStationRepository) {
        this.roadStationRepository = roadStationRepository;
    }

    public RoadStation createRoadStation(final LamAsema lamAsema) {
        final RoadStation rs = new RoadStation();

        rs.setNaturalId(lamAsema.getVanhaId());
        rs.setType(RoadStationType.LAM_STATION);
        rs.setObsolete(false);
        rs.setObsoleteDate(null);
        rs.setName(lamAsema.getNimi());
        rs.setNameFi(lamAsema.getNimiFi());
        rs.setNameSe(lamAsema.getNimiSe());
        rs.setNameEn(lamAsema.getNimiEn());
        rs.setDescription(lamAsema.getKuvaus());
        rs.setLatitude(lamAsema.getLatitudi());
        rs.setLongitude(lamAsema.getLongitudi());
        rs.setAltitude(lamAsema.getKorkeus());
        rs.setRoadNumber(lamAsema.getTieosoite().getTienumero());
        rs.setRoadPart(lamAsema.getTieosoite().getTieosa());
        rs.setDistance(lamAsema.getTieosoite().getEtaisyysTieosanAlusta());

        rs.setCollectionInterval(lamAsema.getKeruuVali());
        rs.setCollectionStatus(CollectionStatus.convertKeruunTila(lamAsema.getKeruunTila()));
        rs.setMunicipality(lamAsema.getKunta());
        rs.setMunicipalityCode(lamAsema.getKuntaKoodi());
        rs.setProvince(lamAsema.getMaakunta());
        rs.setProvinceCode(lamAsema.getMaakuntaKoodi());

        return rs;
    }

    @Transactional
    @Override
    public RoadStation save(RoadStation roadStation) {
        return roadStationRepository.save(roadStation);
    }
}
