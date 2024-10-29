package fi.livi.digitraffic.tie.dto.roadstation.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Road station road address")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "roadNumber", "roadSection", "distanceFromRoadSectionStart", "carriageway", "side" })
public class StationRoadAddressV1 {

    /**
     * tierekisteri_tietosisallon_kuvaus.pdf
     * <p>
     * Sijaintitarkenteita (4.11.2020)
     * Puoli: Tietolajin tie-/ajorata -tasoisuudesta riippuen ilmaistaan tiedon puoli suhteessa tieosoitteen kasvusuuntaan
     * 1 = oikealla
     * 2 = vasemmalla
     * 3 = ajoratojen välissä (käytetään vain varusteilla ajoradalla 1)
     * 4 = kävely- tai pyöräilytien takana (esim. viherkuviot, kun viheralue on päätien osoitteella)
     * 7 = tien päässä (tien lopussa, jos ei ole oikealla/vasemmalla)
     * 8 = tien tai ajoradan keskellä / ajorataa pitkin (välikohtainen tieto)
     * 9 = tien päällä / ajoradan poikki (esim. korkeusrajoitukset)
     * 0 = puoli ei ole tiedossa tai sitä ei ole määritelty
     *
     * @see <a href="https://julkaisut.vayla.fi/tierekisteri/tierekisteri_tietosisallon_kuvaus.pdf">tierekisteri_tietosisallon_kuvaus.pdf</a>
     * @see <a href="https://vayla.fi/palveluntuottajat/aineistot/tierekisteri">Tierekisterin ohjeet ja kuvaukset</a>
     */
    public static final String ROAD_ADDRESS_SIDE_DESCRIPTION =
        "Road address side information <br>" +
        "* UNKNOWN: 0 = Unknown, <br>" +
        "* RIGHT    1 = On the right side of the carriageway in the increasing direction, <br>" +
        "* LEFT:    2 = On the left side of the carriageway in the increasing direction, <br>" +
        "* BETWEEN: 3 = Between the carriageways, <br>" +
        "* END:     7 = At the end of the road, <br>" +
        "* MIDDLE:  8 = In the middle of the carriageway / on the carriageway, <br>" +
        "* CROSS:   9 = Across the road";

    /**
     * Tieosoitejärjestelmä.pdf
     * 8. AJORATA
     * <p>
     * Tiellä voi olla yksi tai kaksi ajorataa. Ns. tietason tietolajit (kuten tieluokka) ovat molemmille
     * ajoradoille yhteisiä, mutta esimerkiksi varusteet ja laitteet ovat yleensä ajoratakohtaisia.
     * Ajorata yksilöidään seuraavasti:
     * Ajr 0 = 1-ajoratainen tieosuus
     * Ajr 1 = 2-ajorataisen tien kasvusuunnassa oikeanpuoleinen ajorata
     * Ajr 2 = 2-ajorataisen tien kasvusuunnassa vasemmanpuoleinen ajorata
     *
     * @see <a href="https://vayla.fi/documents/25230764/35411009/Tieosoitejärjestelmä.pdf">Tieosoitejärjestelmä.pdf</a>
     * @see <a href="https://vayla.fi/palveluntuottajat/aineistot/tierekisteri">Tierekisterin ohjeet ja kuvaukset</a>
     */
    public static final String ROAD_ADDRESS_CARRIAGEWAY_DESCRIPTION =
        "Carriageway <br>" +
        "ONE_CARRIAGEWAY:                                0 = One carriageway road section <br>" +
        "DUAL_CARRIAGEWAY_RIGHT_IN_INCREASING_DIRECTION: 1 = Dual carriageway's right carriageway on increasing direction <br>" +
        "DUAL_CARRIAGEWAY_LEFT_IN_INCREASING_DIRECTION:  2 = Dual carriageway's left carriageway on increasing direction (upstream)";

    @Schema(description = ROAD_ADDRESS_SIDE_DESCRIPTION)
    public enum RoadAddressSide {
        UNKNOWN(0),
        RIGHT(1),
        LEFT(2),
        BETWEEN(3),
        END(7),
        MIDDLE(8),
        CROSS (9);

        private final int code;

        private static final Logger log = LoggerFactory.getLogger(RoadAddressSide.class);

        RoadAddressSide(final int code) {
            this.code = code;
        }
        public static RoadAddressSide getByCode(final Integer code) {
            if (code != null) {
                for (final RoadAddressSide side : values()) {
                    if (side.code == code) {
                        return side;
                    }
                }
                log.error("No Side found with code " + code);
            }
            return null;
        }
    }

    @Schema(description = ROAD_ADDRESS_CARRIAGEWAY_DESCRIPTION)
    public enum RoadAddressCarriageway {
        ONE_CARRIAGEWAY(0),
        DUAL_CARRIAGEWAY_RIGHT_IN_INCREASING_DIRECTION(1),
        DUAL_CARRIAGEWAY_LEFT_IN_INCREASING_DIRECTION(2);

        private final int code;
        private static final Logger log = LoggerFactory.getLogger(RoadAddressSide.class);

        RoadAddressCarriageway(final int code) {
            this.code = code;
        }
        public static RoadAddressCarriageway getByCode(final Integer code) {
            if (code != null) {
                for (final RoadAddressCarriageway carriageway : values()) {
                    if (carriageway.code == code) {
                        return carriageway;
                    }
                }
                log.error("No Carriageway found with code " + code);
            }
            return null;
        }
    }

    @Schema(description = "Road number (values 1–99999)", example = "7")
    public final Integer roadNumber;

    @Schema(description = "Road section (values 1–999)", example = "8")
    public final Integer roadSection;

    @Schema(description = "Distance from start of the road portion [m]", example = "3801")
    public final Integer distanceFromRoadSectionStart;

    @Schema(description = ROAD_ADDRESS_SIDE_DESCRIPTION)
    public final RoadAddressSide side;

    @Schema(description = "Carriageway")
    public final RoadAddressCarriageway carriageway;

    @JsonIgnore // Value can contain handwritten values
    @Schema(description = "Road winter maintenance class", example = "1")
    public final String roadMaintenanceClass;

    @Schema(description = "Road contract area", example = "Espoo 19-24")
    public final String contractArea;

    @Schema(description = "Road contract area code", example = "142")
    public final Integer contractAreaCode;

    public StationRoadAddressV1(final Integer roadNumber, final Integer roadSection, final Integer distanceFromRoadSectionStart,
                                final Integer carriagewayCode, final Integer sideCode, final String contractArea, final Integer contractAreaCode,
                                final String roadMaintenanceClass) {
        this.roadNumber = roadNumber;
        this.roadSection = roadSection;
        this.distanceFromRoadSectionStart = distanceFromRoadSectionStart;
        this.carriageway = RoadAddressCarriageway.getByCode(carriagewayCode);
        this.side = RoadAddressSide.getByCode(sideCode);
        this.contractArea = contractArea;
        this.contractAreaCode = contractAreaCode;
        this.roadMaintenanceClass = roadMaintenanceClass;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("roadNumber", roadNumber)
                .appendField("roadSection", roadSection)
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final StationRoadAddressV1 that = (StationRoadAddressV1) o;

        return new EqualsBuilder()
                .append(roadNumber, that.roadNumber)
                .append(roadSection, that.roadSection)
                .append(distanceFromRoadSectionStart, that.distanceFromRoadSectionStart)
                .append(carriageway, that.carriageway)
                .append(side, that.side)
                .append(roadMaintenanceClass, that.roadMaintenanceClass)
                .append(contractArea, that.contractArea)
                .append(contractAreaCode, that.contractAreaCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(roadNumber)
                .append(roadSection)
                .append(distanceFromRoadSectionStart)
                .append(carriageway)
                .append(side)
                .append(roadMaintenanceClass)
                .append(contractArea)
                .append(contractAreaCode)
                .toHashCode();
    }
}
