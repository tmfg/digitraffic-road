package fi.livi.digitraffic.tie.dto.variablesigns.v1;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.dto.geojson.v1.PropertiesV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Variable Sign properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariableSignPropertiesV1 extends PropertiesV1 {
    @Schema(description = "Id")
    public final String id;

    @Schema(description = "Type")
    public final SignType type;

    @Schema(description = "Sign location as road address")
    public final String roadAddress;

    @Schema(description = "Direction of variable sign, increasing or decreasing road address", nullable = true)
    public final Direction direction;

    @Schema(description = "Variable sign placement:\n" +
                          "SINGLE = Single carriageway rod\n" +
                          "RIGHT = First carriageway on the right in the direction of the road number\n" +
                          "LEFT = Second carriageway on the left in the direction of the road number\n" +
                          "BETWEEN = Between the carriageways")
    public final Carriageway carriageway;

    // data properties
    @Schema(description = "Value that is displayed on the device")
    public final String displayValue;

    @Schema(description = "Additional information displayed on the device", nullable = true)
    public final String additionalInformation;

    @Schema(description = "Information is effect after this date")
    public final Instant effectDate;

    @JsonIgnore
    public final Instant createDate;

    @Schema(description = "Cause for changing the sign:\n" +
                          "Automaatti = Automatic\n" +
                          "Käsiohjaus = By hand", nullable = true)
    public final String cause;

    @Schema(description = "Variable sign reliability")
    public final Reliability reliability;

    @Schema(description = "Text rows if sign contains a screen")
    public final List<SignTextRowV1> textRows;
    private final Instant lastModified;

    public VariableSignPropertiesV1(final String id, final SignType type, final String roadAddress, final Direction direction,
                                    final Carriageway carriageway, final String displayValue, final String additionalInformation,
                                    final Instant createDate, final Instant effectDate,
                                    final String cause, final Reliability reliability, final List<SignTextRowV1> textRows, final Instant lastModified) {
        this.id = id;
        this.type = type;
        this.roadAddress = roadAddress;
        this.direction = direction;
        this.carriageway = carriageway;
        this.displayValue = displayValue;
        this.additionalInformation = additionalInformation;
        this.createDate = createDate;
        this.effectDate = effectDate;
        this.cause = cause;
        this.reliability = reliability;
        this.textRows = textRows;
        this.lastModified = lastModified;
    }

    @Override
    public Instant getLastModified() {
        return lastModified;
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
