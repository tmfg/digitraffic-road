package fi.livi.digitraffic.tie.dto.tms.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesDetailedV1;
import fi.livi.digitraffic.tie.model.tms.CalculatorDeviceType;
import fi.livi.digitraffic.tie.model.tms.TmsStationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tms station properties object with basic information")
@JsonPropertyOrder({ "id", "tmsNumber", "name" })
public class TmsStationPropertiesDetailedV1 extends RoadStationPropertiesDetailedV1<Long> {

    @Schema(description = "TMS station number", required = true)
    public final long tmsNumber;

    @Schema(description = "Direction 1 municipality (1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso.)", required = true)
    public final String direction1Municipality;

    @Schema(description = "Direction 1 municipality code")
    public final Integer direction1MunicipalityCode;

    @Schema(description = "Direction 2 municipality (2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso.)", required = true)
    public final String direction2Municipality;

    @Schema(description = "Direction 2 municipality code")
    public final Integer direction2MunicipalityCode;

    @Schema(description = "Type of  TMS station")
    public final TmsStationType stationType;

    @Schema(description = "Type of calculation device")
    public final CalculatorDeviceType calculatorDeviceType;

    /** Sensors natural ids */
    @Schema(description = "Tms Station Sensors ids")
    public final List<Long> sensors;

    @Schema(description = "Free flow speed to direction 1 [km/h]")
    public final Double freeFlowSpeed1;

    @Schema(description = "Free flow speed to direction 2 [km/h]")
    public final Double freeFlowSpeed2;

    public TmsStationPropertiesDetailedV1(final long id, final long tmsNumber,
                                          final String direction1Municipality, final Integer direction1MunicipalityCode,
                                          final String direction2Municipality, final Integer direction2MunicipalityCode, final TmsStationType stationType,
                                          final CalculatorDeviceType calculatorDeviceType, final List<Long> sensors,
                                          final Double freeFlowSpeed1, final Double freeFlowSpeed2) {
        super(id);
        this.tmsNumber = tmsNumber;
        this.direction1Municipality = direction1Municipality;
        this.direction1MunicipalityCode = direction1MunicipalityCode;
        this.direction2Municipality = direction2Municipality;
        this.direction2MunicipalityCode = direction2MunicipalityCode;
        this.stationType = stationType;
        this.calculatorDeviceType = calculatorDeviceType;
        this.sensors = sensors;
        this.freeFlowSpeed1 = freeFlowSpeed1;
        this.freeFlowSpeed2 = freeFlowSpeed2;
    }
}
