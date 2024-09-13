package fi.livi.digitraffic.tie.service.tms;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.TieosoiteVO;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.roadstation.RoadAddress;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationState;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.AbstractRoadStationAttributeUpdater;

public abstract class AbstractTmsStationAttributeUpdater extends AbstractRoadStationAttributeUpdater {

    public static boolean updateRoadStationAttributes(final LamAsemaVO la, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if ( CollectionStatus.isPermanentlyDeletedKeruunTila(la.getKeruunTila()) ) {
            to.makeObsolete();
        } else {
            to.unobsolete();
        }
        to.setLotjuId(la.getId());
        to.updatePublicity(la.isJulkinen() == null || la.isJulkinen());
        to.setNaturalId(la.getVanhaId().longValue());
        to.setType(RoadStationType.TMS_STATION);
        to.setName(la.getNimi());
        to.setNameFi(la.getNimiFi());
        to.setNameSv(la.getNimiSe());
        to.setNameEn(la.getNimiEn());
        to.setLatitude(getScaledToDbCoordinate(la.getLatitudi()));
        to.setLongitude(getScaledToDbCoordinate(la.getLongitudi()));
        to.setAltitude(getScaledToDbAltitude(la.getKorkeus()));
        to.setCollectionInterval(la.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(la.getKeruunTila()));
        to.setMunicipality(la.getKunta());
        to.setMunicipalityCode(la.getKuntaKoodi());
        to.setProvince(la.getMaakunta());
        to.setProvinceCode(la.getMaakuntaKoodi());
        to.setLiviId(la.getLiviId());
        to.setStartDate(TimeUtil.toZonedDateTimeWithoutMillisAtUtc(la.getAlkamisPaiva()));
        to.setRepairMaintenanceDate(TimeUtil.toZonedDateTimeWithoutMillisAtUtc(la.getKorjaushuolto()));
        to.setAnnualMaintenanceDate(TimeUtil.toZonedDateTimeWithoutMillisAtUtc(la.getVuosihuolto()));
        to.setState(RoadStationState.fromTilaTyyppi(la.getAsemanTila()));
        to.setLocation(la.getAsemanSijainti());
        to.setCountry(la.getMaa());
        to.setPurpose(la.getKayttotarkoitus());

        return updateRoadAddressAttributes(la.getTieosoite(), to.getRoadAddress()) ||
               HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    public static boolean updateRoadAddressAttributes(final TieosoiteVO lat, final RoadAddress to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setRoadNumber(lat.getTienumero());
        to.setRoadSection(lat.getTieosa());
        to.setDistanceFromRoadSectionStart(lat.getEtaisyysTieosanAlusta());
        to.setCarriagewayCode(lat.getAjorata());
        to.setSideCode(lat.getPuoli());
        to.setRoadMaintenanceClass(lat.getTienHoitoluokka());
        to.setContractArea(lat.getUrakkaAlue());
        to.setContractAreaCode(lat.getUrakkaAlueKoodi());
        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}
