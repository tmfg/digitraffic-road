package fi.livi.digitraffic.tie.metadata.geojson.trafficsigns;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Properties", description = "Traffic Sign properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficSignProperties {
    // device properties
    public final String id;
    public final String type;
    public final String roadAddress;
    @ApiModelProperty("Direction of traffic sign, increasing or decreasing road address")
    public final Direction direction;
    @ApiModelProperty("Traffic sign placement")
    public final Carriageway carriageway;

    // data properties
    public final String displayInformation;
    public final String additionalInformation;
    @ApiModelProperty("Information is effect after this date")
    public final ZonedDateTime effectDate;
    public final String cause;

    public TrafficSignProperties(final String id, final String type, final String roadAddress, final Direction direction,
        final Carriageway carriageway, final String displayInformation, final String additionalInformation, final ZonedDateTime effectDate, final String cause) {
        this.id = id;
        this.type = type;
        this.roadAddress = roadAddress;
        this.direction = direction;
        this.carriageway = carriageway;
        this.displayInformation = displayInformation;
        this.additionalInformation = additionalInformation;
        this.effectDate = effectDate;
        this.cause = cause;
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
            else if(StringUtils.equals("KASVAVA", value)) {
                return INCREASING;
            }

            throw new IllegalArgumentException("No Direction by value " + value);
        }
    }

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
}
