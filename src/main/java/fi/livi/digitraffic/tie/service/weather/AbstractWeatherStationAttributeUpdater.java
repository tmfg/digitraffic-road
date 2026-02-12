package fi.livi.digitraffic.tie.service.weather;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TieosoiteVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.roadstation.RoadAddress;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationState;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.AbstractRoadStationAttributeUpdater;

public abstract class AbstractWeatherStationAttributeUpdater extends AbstractRoadStationAttributeUpdater {

    public static boolean updateRoadStationAttributes(final TiesaaAsemaVO ta, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if ( CollectionStatus.isPermanentlyDeletedKeruunTila(ta.getKeruunTila())) {
            to.makeObsolete();
        } else {
            to.unobsolete();
        }
        to.setLotjuId(ta.getId());
        to.updatePublicity(ta.isJulkinen() == null || ta.isJulkinen());
        to.setNaturalId(ta.getVanhaId().longValue());
        to.setName(ta.getNimi());
        to.setNameFi(ta.getNimiFi());
        to.setNameSv(ta.getNimiSe());
        to.setNameEn(ta.getNimiEn());
        to.setLatitude(getScaledToDbCoordinate(ta.getLatitudi()));
        to.setLongitude(getScaledToDbCoordinate(ta.getLongitudi()));
        to.setAltitude(getScaledToDbAltitude(ta.getKorkeus()));
        to.setCollectionInterval(ta.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(ta.getKeruunTila()));
        to.setMunicipality(ta.getKunta());
        to.setMunicipalityCode(ta.getKuntaKoodi());
        to.setProvince(ta.getMaakunta());
        to.setProvinceCode(ta.getMaakuntaKoodi());
        to.setLiviId(ta.getLiviId());
        to.setStartDate(TimeUtil.toInstantWithoutMillis(ta.getAlkamisPaiva()));
        to.setRepairMaintenanceDate(TimeUtil.toInstantWithoutMillis(ta.getKorjaushuolto()));
        to.setAnnualMaintenanceDate(TimeUtil.toInstantWithoutMillis(ta.getVuosihuolto()));
        to.setState(RoadStationState.fromTilaTyyppi(ta.getAsemanTila()));
        to.setLocation(ta.getAsemanSijainti());
        to.setCountry(ta.getMaa());

        return updateRoadAddressAttributes(ta.getTieosoite(), to.getRoadAddress()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    public static boolean updateRoadAddressAttributes(final TieosoiteVO wt, final RoadAddress to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setRoadNumber(wt.getTienumero());
        to.setRoadSection(wt.getTieosa());
        to.setDistanceFromRoadSectionStart(wt.getEtaisyysTieosanAlusta());
        to.setCarriagewayCode(wt.getAjorata());
        to.setSideCode(wt.getPuoli());
        to.setRoadMaintenanceClass(wt.getTienHoitoluokka());
        to.setContractArea(wt.getUrakkaAlue());
        to.setContractAreaCode(wt.getUrakkaAlueKoodi());
        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}
