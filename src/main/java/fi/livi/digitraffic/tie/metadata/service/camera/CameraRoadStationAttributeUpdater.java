package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.kamera.KeruunTILA;
import fi.livi.digitraffic.tie.wsdl.kamera.Tieosoite;

public abstract class CameraRoadStationAttributeUpdater {

    private static final Logger log = Logger.getLogger(CameraRoadStationAttributeUpdater.class);

    private static final EnumSet<KeruunTILA> POISTETUT = EnumSet.of(KeruunTILA.POISTETTU_PYSYVASTI, KeruunTILA.POISTETTU_TILAPAISESTI);

    protected RoadStationService roadStationService;

    public CameraRoadStationAttributeUpdater(
            RoadStationService roadStationService) {
        this.roadStationService = roadStationService;
    }

    public static boolean updateRoadStationAttributes(final RoadStation to, final Kamera from) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if (POISTETUT.contains(from.getKeruunTila())) {
            to.obsolete();
        } else {
            to.setObsolete(false);
            to.setObsoleteDate(null);
        }

        to.setNaturalId(from.getVanhaId().longValue());
        to.setType(RoadStationType.CAMERA);
        to.setName(from.getNimi());
        to.setNameFi(from.getNimiFi());
        to.setNameSv(from.getNimiSe());
        to.setNameEn(from.getNimiEn());
        to.setDescription(from.getKuvaus());
        to.setAdditionalInformation(from.getLisatieto());
        to.setLatitude(from.getLatitudi());
        to.setLongitude(from.getLongitudi());
        to.setAltitude(from.getKorkeus());
        to.setCollectionInterval(from.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(from.getKeruunTila()));
        to.setMunicipality(from.getKunta());
        to.setMunicipalityCode(from.getKuntaKoodi());
        to.setProvince(from.getMaakunta());
        to.setProvinceCode(from.getMaakuntaKoodi());

        return updateRoadAddressAttributes(from.getTieosoite(), to.getRoadAddress()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    public static boolean updateRoadAddressAttributes(final Tieosoite from, final RoadAddress to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadNumber(from.getTienumero());
        to.setRoadSection(from.getTieosa());
        to.setDistanceFromRoadSectionStart(from.getEtaisyysTieosanAlusta());
        to.setCarriagewayCode(from.getAjorata());
        to.setSideCode(from.getPuoli());
        to.setRoadMaintenanceClass(from.getTienHoitoluokka());

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    protected RoadAddress resolveOrCreateRoadAddress(Kamera la, Map<Long, RoadAddress> roadAddressesMappedByLotjuId) {
        if (la.getTieosoiteId() == null) {
            log.info(ToStringHelpper.toString(la) + " had null tieosoiteId");
        }
        // Set road address only if it is set in lotju
        RoadAddress ra = roadAddressesMappedByLotjuId.get(la.getTieosoiteId());
        if (ra == null && la.getTieosoiteId() != null) {
            ra = new RoadAddress(la.getTieosoiteId());
            roadStationService.save(ra);
            roadAddressesMappedByLotjuId.put(ra.getLotjuId(), ra);
            log.info("Created new " + ra);
        }
        return ra;
    }
}
