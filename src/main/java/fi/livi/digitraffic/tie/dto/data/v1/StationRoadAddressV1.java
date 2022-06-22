package fi.livi.digitraffic.tie.dto.data.v1;

import javax.persistence.Column;

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
@JsonPropertyOrder({ "roadNumber", "roadSection", "distanceFromRoadSectionStart", "carriagewayCode", "sideCode" })
public class StationRoadAddressV1 {

    @Schema(description = "Road address side information")
    public enum RoadAddressSide {
        UNKNOWN(0),
        RIGHT(1), // oikea ( mittaussuunnan oikealla puolella)
        LEFT(2), // vasen ( mittaussuunnan vasemmalla puolella)
        BETWEEN(3), // välissä (ajoratojen välissä)
        END(7), // päässä ( tien päässä)
        MIDDLE(8), // keskellä
        CROSS (9); // poikki

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

    @Schema(description = "Road address carriageway information")
    public enum RoadAddressCarriageway {
        ONE_CARRIAGEWAY(0), // yksiajoratainen osuus
        DUAL_CARRIAGEWAY_FIRST_MEASURING_DIRECTION(1), // kaksiajorataisen osuuden ykkösajorata (mittaussuunta)
        DUAL_CARRIAGEWAY_SECOND_UPSTREAM(2); // kaksiajorataisen osuuden kakkosajorata (vastasuunta)

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
    @Column(name="DISTANCE_FROM_ROAD_SECTION_ST")
    public final Integer distanceFromRoadSectionStart;

    @Schema(description = "Carriageway (" +
                              "0 = One carriageway portion, " +
                              "1 = First carriageway of dual carriageway portion (measuring direction) " +
                              "2 = Second carriageway of dual carriageway portion (upstream))",
            example = "1")
    @Column(name = "CARRIAGEWAY")
    public final Integer carriagewayCode;

    @JsonIgnore // Value can contain handwritten values
    @Schema(description = "Road winter maintenance class", example = "1")
    public final String roadMaintenanceClass;

    @Schema(description = "Road contract area", example = "Espoo 19-24")
    public final String contractArea;

    @Schema(description = "Road contract area code", example = "142")
    public final Integer contractAreaCode;

    public StationRoadAddressV1(final Integer roadNumber, final Integer roadSection, final Integer distanceFromRoadSectionStart,
                                final Integer carriagewayCode, final String contractArea, final Integer contractAreaCode,
                                final String roadMaintenanceClass) {
        this.roadNumber = roadNumber;
        this.roadSection = roadSection;
        this.distanceFromRoadSectionStart = distanceFromRoadSectionStart;
        this.carriagewayCode = carriagewayCode;
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
                .append(carriagewayCode, that.carriagewayCode)
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
                .append(carriagewayCode)
                .append(roadMaintenanceClass)
                .append(contractArea)
                .append(contractAreaCode)
                .toHashCode();
    }
}
