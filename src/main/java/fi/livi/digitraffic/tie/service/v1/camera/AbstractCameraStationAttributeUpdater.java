package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.model.RoadStationType.CAMERA_STATION;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.Julkisuus;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.TieosoiteVO;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStationState;
import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.service.AbstractRoadStationAttributeUpdater;

public abstract class AbstractCameraStationAttributeUpdater extends AbstractRoadStationAttributeUpdater {

    private static final Logger log = LoggerFactory.getLogger(AbstractCameraStationAttributeUpdater.class);

    public static boolean updateRoadStationAttributes(final KameraVO kamera, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if ( CollectionStatus.isPermanentlyDeletedKeruunTila(kamera.getKeruunTila())) {
            to.makeObsolete();
        } else {
            to.unobsolete();
        }
        to.setLotjuId(kamera.getId());

        final boolean isPublicOld = to.internalIsPublic();
        final boolean isPublicPreviousOld = to.isPublicPrevious();
        final ZonedDateTime publicityStartTimeOld = to.getPublicityStartTime();

        final ZonedDateTime publicityStartTimeNew = kamera.getJulkisuus() != null ? DateHelper.toZonedDateTimeWithoutMillisAtUtc(kamera.getJulkisuus().getAlkaen()) : null;
        final boolean isPublicNew = kamera.getJulkisuus() != null && JulkisuusTaso.JULKINEN == kamera.getJulkisuus().getJulkisuusTaso();
        final boolean changed = to.updatePublicity(isPublicNew, publicityStartTimeNew);
        if ( changed ) {
            log.info("method=updateRoadStationAttributes {} roadStationPublicityChanged naturalId={} lotuId={} " +
                     "fromPublic={} toPublic={} fromPreviousPublic={} toPreviousPublic={} " +
                     "fromPublicityStartTime={} toPublicityStartTime={}",
                     to.getType(), CAMERA_STATION.equals(to.getType()) ? "C" + to.getNaturalId() : to.getNaturalId(), to.getLotjuId(),
                     isPublicOld, to.internalIsPublic(), isPublicPreviousOld, to.isPublicPrevious(),
                     publicityStartTimeOld, to.getPublicityStartTime());
        }

        to.setNaturalId(kamera.getVanhaId().longValue());
        to.setType(CAMERA_STATION);
        to.setName(kamera.getNimi());
        to.setNameFi(kamera.getNimiFi());
        to.setNameSv(kamera.getNimiSe());
        to.setNameEn(kamera.getNimiEn());
        to.setLatitude(getScaledToDbCoordinate(kamera.getLatitudi()));
        to.setLongitude(getScaledToDbCoordinate(kamera.getLongitudi()));
        to.setAltitude(getScaledToDbAltitude(kamera.getKorkeus()));
        to.setCollectionInterval(kamera.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(kamera.getKeruunTila()));
        to.setMunicipality(kamera.getKunta());
        to.setMunicipalityCode(kamera.getKuntaKoodi());
        to.setProvince(kamera.getMaakunta());
        to.setProvinceCode(kamera.getMaakuntaKoodi());
        to.setLiviId(kamera.getLiviId());
        to.setStartDate(DateHelper.toZonedDateTimeWithoutMillisAtUtc(kamera.getAlkamisPaiva()));
        to.setRepairMaintenanceDate(DateHelper.toZonedDateTimeWithoutMillisAtUtc(kamera.getKorjaushuolto()));
        to.setAnnualMaintenanceDate(DateHelper.toZonedDateTimeWithoutMillisAtUtc(kamera.getVuosihuolto()));
        to.setState(RoadStationState.fromTilaTyyppi(kamera.getAsemanTila()));
        to.setLocation(kamera.getAsemanSijainti());
        to.setCountry(kamera.getMaa());
        to.setPurpose(kamera.getKayttotarkoitus());

        return updateRoadAddressAttributes(kamera.getTieosoite(), to.getRoadAddress()) ||
                HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    public static boolean updateRoadAddressAttributes(final TieosoiteVO ct, final RoadAddress to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setRoadNumber(ct.getTienumero());
        to.setRoadSection(ct.getTieosa());
        to.setDistanceFromRoadSectionStart(ct.getEtaisyysTieosanAlusta());
        to.setCarriagewayCode(ct.getAjorata());
        to.setSideCode(ct.getPuoli());
        to.setRoadMaintenanceClass(ct.getTienHoitoluokka());
        to.setContractArea(ct.getUrakkaAlue());
        to.setContractAreaCode(ct.getUrakkaAlueKoodi());
        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    protected static boolean isPublic(final EsiasentoVO esiasento) {
        return Julkisuus.JULKINEN.equals(esiasento.getJulkisuus());
    }


}
