package fi.livi.digitraffic.tie.metadata.geojson.variablesigns;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Properties", description = "Variable Sign properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariableSignProperties {
    // device properties
    // TODO @Schema(description = "TODO")
    public final String id;

    @Schema(description = "Variable sign type")
    public final SignType type;

    // TODO @Schema(description = "TODO")
    public final String roadAddress;

    @Schema(description = "Direction of variable sign, increasing or decreasing road address")
    public final Direction direction;

    @Schema(description = "Variable sign placement:\n" +
                          "SINGLE = Single carriageway rod\n" +
                          "RIGHT = First carriageway on the right in the direction of the road number\n" +
                          "LEFT = Second carriageway on the left in the direction of the road number\n" +
                          "BETWEEN = Between the carriageways")
    public final Carriageway carriageway;

    // data properties
    // TODO @Schema(description = "TODO")
    public final String displayValue;

    // TODO @Schema(description = "TODO")
    public final String additionalInformation;

    @Schema(description = "Information is effect after this date")
    public final ZonedDateTime effectDate;

    // TODO @Schema(description = "TODO")
    public final String cause;

    @Schema(description = "Variable sign reliability")
    public final Reliability reliability;

    // TODO @Schema(description = "TODO")
    public final List<SignTextRow> textRows;

    public VariableSignProperties(final String id, final SignType type, final String roadAddress, final Direction direction,
                                  final Carriageway carriageway, final String displayValue, final String additionalInformation, final ZonedDateTime effectDate,
                                  final String cause, final Reliability reliability, final List<SignTextRow> textRows) {
        this.id = id;
        this.type = type;
        this.roadAddress = roadAddress;
        this.direction = direction;
        this.carriageway = carriageway;
        this.displayValue = displayValue;
        this.additionalInformation = additionalInformation;
        this.effectDate = effectDate;
        this.cause = cause;
        this.reliability = reliability;
        this.textRows = textRows;
    }

    @Schema
    public enum SignType {
        SPEEDLIMIT, WARNING, INFORMATION;

        public static SignType byValue(final String value) {
            if(value == null)  {
                return null;
            } else if (StringUtils.equals("NOPEUSRAJOITUS", value)) {
                return SPEEDLIMIT;
            } else if (StringUtils.equals("VAIHTUVAVAROITUSMERKKI", value)) {
                return WARNING;
            } else if (StringUtils.equals("TIEDOTUSOPASTE", value)) {
                return INFORMATION;
            }

            throw new IllegalArgumentException("No SignType by value " + value);
        }
    }

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

    @Schema
    public enum Carriageway {
        SINGLE("NORMAALI"),
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

    @Schema
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
