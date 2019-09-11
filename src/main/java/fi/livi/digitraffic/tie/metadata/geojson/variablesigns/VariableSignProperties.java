package fi.livi.digitraffic.tie.metadata.geojson.variablesigns;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Properties", description = "Variable Sign properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariableSignProperties {
    // device properties
    public final String id;
    @ApiModelProperty(value = "Variable sign type",
        allowableValues = "NOPEUSRAJOITUS,VAIHTUVAVAROITUSMERKKI")
    public final String type;
    public final String roadAddress;
    @ApiModelProperty(value = "Direction of variable sign, increasing or decreasing road address",
        allowableValues = "INCREASING,DECREASING")
    public final Direction direction;
    @ApiModelProperty(value = "Variable sign placement",
        allowableValues = "NORMAL,RIGHT,LEFT,BETWEEN,END_OF_ROAD,ALONG,ACROSS")
    public final Carriageway carriageway;

    // data properties
    public final String displayInformation;
    public final String additionalInformation;
    @ApiModelProperty("Information is effect after this date")
    public final ZonedDateTime effectDate;
    public final String cause;
    @ApiModelProperty(value = "Variable sign reliability",
        allowableValues = "NORMAL,DISCONNECTED,MALFUNCTION")
    public final Reliability reliability;

    public VariableSignProperties(final String id, final String type, final String roadAddress, final Direction direction,
        final Carriageway carriageway, final String displayInformation, final String additionalInformation, final ZonedDateTime effectDate,
        final String cause, final Reliability reliability) {
        this.id = id;
        this.type = type;
        this.roadAddress = roadAddress;
        this.direction = direction;
        this.carriageway = carriageway;
        this.displayInformation = displayInformation;
        this.additionalInformation = additionalInformation;
        this.effectDate = effectDate;
        this.cause = cause;
        this.reliability = reliability;
    }

    @ApiModel
    public enum Direction {
        INCREASING,
        DECREASING;

        public static Direction byValue(final String value) {
            if(value == null) {
                return null;
            } else if(StringUtils.equals("KASVAVA", value)) {
                return INCREASING;
            }
            else if(StringUtils.equals("LASKEVA", value)) {
                return DECREASING;
            }

            throw new IllegalArgumentException("No Direction by value " + value);
        }
    }

    @ApiModel
    public enum Carriageway {
        NORMAL("NORMAALI"),
        RIGHT("OIKEANPUOLEINEN"),
        LEFT("VASEMMANPUOLEINEN"),
        BETWEEN("AJORATOJEN_VALISSA"),
        END_OF_ROAD("TIEN_PAASSA"),
        ALONG("AJORATAA_PITKIN"),
        ACROSS("AJORADAN_POIKKI");

        private final String value;

        Carriageway(final String value) {
            this.value = value;
        }

        public static Carriageway byValue(final String value) {
            if(value == null) {
                return null;
            }

            final Optional<Carriageway> first = Arrays.stream(Carriageway.values()).filter(c -> StringUtils.equals(c.value, value)).findFirst();

            return first.orElseThrow(() -> new IllegalArgumentException("No Carriageway by value " + value));
        }
    }

    @ApiModel
    public enum Reliability {
        NORMAL("NORMAALI"),
        DISCONNECTED("YHTEYSKATKO"),
        MALFUNCTION("LAITEVIKA");

        private final String value;

        Reliability(final String value) {
            this.value = value;
        }

        public static Reliability byValue(final String value) {
            if(value == null) {
                return null;
            }

            final Optional<Reliability> first = Arrays.stream(Reliability.values()).filter(c -> StringUtils.equals(c.value, value)).findFirst();

            return first.orElseThrow(() -> new IllegalArgumentException("No Reliability by value " + value));
        }

        }
}
