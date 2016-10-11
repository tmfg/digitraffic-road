package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationState;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.AbstractRoadStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.metatiedot._2015._09._29.TieosoiteVO;

public abstract class AbstractCameraStationUpdater extends AbstractRoadStationUpdater {

    private static final Logger log = LoggerFactory.getLogger(AbstractCameraStationUpdater.class);

    private static final Pattern cameraPresetIdPattern = Pattern.compile("^C[0-9]{7}$");

    protected RoadStationService roadStationService;

    public AbstractCameraStationUpdater(
            final RoadStationService roadStationService) {
        this.roadStationService = roadStationService;
    }

    public static boolean updateRoadStationAttributes(final KameraVO from, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if ( CollectionStatus.isPermanentlyDeletedKeruunTila(from.getKeruunTila()) ) {
            to.obsolete();
        } else {
            to.setObsolete(false);
            to.setObsoleteDate(null);
        }
        to.setLotjuId(from.getId());
        to.setPublic(from.isJulkinen() == null || from.isJulkinen());
        to.setNaturalId(from.getVanhaId().longValue());
        to.setType(RoadStationType.CAMERA_STATION);
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
        final String before = ReflectionToStringBuilder.toString(to);

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

    public static String convertVanhaIdToKameraId(final Integer vanhaId) {
        final String vanha = vanhaId.toString();
        return StringUtils.leftPad(vanha, 6, "C00000");
    }

    public static String convertCameraIdToPresetId(final String cameraId, final String suunta) {
        return cameraId + StringUtils.leftPad(suunta, 2, "00");
    }

    public static String convertPresetIdToCameraId(final String presetId) {
        return presetId.substring(0, 6);
    }

    public static long convertPresetIdToVanhaId(final String presetId) {
        String cameraId = convertPresetIdToCameraId(presetId);
        cameraId = StringUtils.removeStart(cameraId, "C0");
        return Long.parseLong(StringUtils.removeStart(cameraId, "C"));
    }

    public static boolean validatePresetId(String presetId) {
        Matcher m = cameraPresetIdPattern.matcher(presetId);
        return m.matches();
    }
}
