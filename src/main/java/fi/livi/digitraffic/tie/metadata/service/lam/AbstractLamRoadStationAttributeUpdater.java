package fi.livi.digitraffic.tie.metadata.service.lam;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationState;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.AbstractRoadStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.metatiedot._2015._09._29.TieosoiteVO;

public abstract class AbstractLamRoadStationAttributeUpdater extends AbstractRoadStationUpdater {

    private static final Logger log = Logger.getLogger(AbstractLamRoadStationAttributeUpdater.class);

    protected RoadStationService roadStationService;

    public AbstractLamRoadStationAttributeUpdater(RoadStationService roadStationService) {
        this.roadStationService = roadStationService;
    }

    protected static boolean updateRoadStationAttributes(final LamAsemaVO from, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if ( CollectionStatus.isPermanentlyDeletedKeruunTila(from.getKeruunTila()) ) {
            to.obsolete();
        } else {
            to.setObsolete(false);
            to.setObsoleteDate(null);
        }
        to.setPublic(from.isJulkinen() == null || from.isJulkinen());
        to.setNaturalId(from.getVanhaId().longValue());
        to.setType(RoadStationType.LAM_STATION);
        to.setName(from.getNimi());
        to.setNameFi(from.getNimiFi());
        to.setNameSv(from.getNimiSe());
        to.setNameEn(from.getNimiEn());
        to.setLatitude(from.getLatitudi());
        to.setLongitude(from.getLongitudi());
        to.setAltitude(from.getKorkeus());
        to.setCollectionInterval(from.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(from.getKeruunTila()));
        to.setMunicipality(from.getKunta());
        to.setMunicipalityCode(from.getKuntaKoodi());
        to.setProvince(from.getMaakunta());
        to.setProvinceCode(from.getMaakuntaKoodi());
        to.setLiviId(from.getLiviId());
        to.setStartDate(from.getAlkamisPaiva() != null ? from.getAlkamisPaiva().toGregorianCalendar().toZonedDateTime().toLocalDateTime().withNano(0) : null);
        to.setRepairMaintenanceDate(from.getKorjaushuolto() != null ? from.getKorjaushuolto().toGregorianCalendar().toZonedDateTime().toLocalDateTime().withNano(0) : null);
        to.setAnnualMaintenanceDate(from.getVuosihuolto() != null ? from.getVuosihuolto().toGregorianCalendar().toZonedDateTime().toLocalDateTime().withNano(0) : null);
        to.setState(RoadStationState.convertAsemanTila(from.getAsemanTila()));
        to.setLocation(from.getAsemanSijainti());
        to.setCountry(from.getMaa());

        return updateRoadAddressAttributes(from.getTieosoite(), to.getRoadAddress()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    public static boolean updateRoadAddressAttributes(final TieosoiteVO from, final RoadAddress to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        String before = ReflectionToStringBuilder.toString(to);

        to.setRoadNumber(from.getTienumero());
        to.setRoadSection(from.getTieosa());
        to.setDistanceFromRoadSectionStart(from.getEtaisyysTieosanAlusta());
        to.setCarriagewayCode(from.getAjorata());
        to.setSideCode(from.getPuoli());
        to.setRoadMaintenanceClass(from.getTienHoitoluokka());
        to.setContractArea(from.getUrakkaAlue());
        to.setContractAreaCode(from.getUrakkaAlueKoodi());
        if (HashCodeBuilder.reflectionHashCode(to) != hash) {
            log.info("Updated:\n" + before + " ->\n" + ReflectionToStringBuilder.toString(to));
        }
        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}
