package fi.livi.digitraffic.tie.metadata.service.tms;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationState;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.AbstractRoadStationAttributeUpdater;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.metatiedot._2015._09._29.TieosoiteVO;

public abstract class AbstractTmsStationAttributeUpdater extends AbstractRoadStationAttributeUpdater {

    public AbstractTmsStationAttributeUpdater(final Logger logger) {
        super(logger);
    }

    public static boolean updateRoadStationAttributes(final LamAsemaVO from, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if ( CollectionStatus.isPermanentlyDeletedKeruunTila(from.getKeruunTila()) ) {
            to.obsolete();
        } else {
            to.setObsolete(false);
        }
        to.setLotjuId(from.getId());
        to.setPublic(from.isJulkinen() == null || from.isJulkinen());
        to.setNaturalId(from.getVanhaId().longValue());
        to.setType(RoadStationType.TMS_STATION);
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
        to.setStartDate(DateHelper.toZonedDateTimeWithoutMillis(from.getAlkamisPaiva()));
        to.setRepairMaintenanceDate(DateHelper.toZonedDateTimeWithoutMillis(from.getKorjaushuolto()));
        to.setAnnualMaintenanceDate(DateHelper.toZonedDateTimeWithoutMillis(from.getVuosihuolto()));
        to.setState(RoadStationState.fromTilaTyyppi(from.getAsemanTila()));
        to.setLocation(from.getAsemanSijainti());
        to.setCountry(from.getMaa());
        to.setPurpose(from.getKayttotarkoitus());

        return updateRoadAddressAttributes(from.getTieosoite(), to.getRoadAddress()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    public static boolean updateRoadAddressAttributes(final TieosoiteVO from, final RoadAddress to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setRoadNumber(from.getTienumero());
        to.setRoadSection(from.getTieosa());
        to.setDistanceFromRoadSectionStart(from.getEtaisyysTieosanAlusta());
        to.setCarriagewayCode(from.getAjorata());
        to.setSideCode(from.getPuoli());
        to.setRoadMaintenanceClass(from.getTienHoitoluokka());
        to.setContractArea(from.getUrakkaAlue());
        to.setContractAreaCode(from.getUrakkaAlueKoodi());
        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}
