package fi.livi.digitraffic.tie.dto.v1.tms;

import java.time.Instant;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Sensor constant value")
@JsonPropertyOrder({ "name", "value", "validFrom", "validTo"})
@Entity
@Immutable
public class TmsSensorConstantValueDto {

    @JsonIgnore
    @Id
    private Long lotjuId;

    @JsonIgnore
    private Long constantLotjuId;

    @JsonIgnore
    private Long roadStationId;

    @JsonIgnore
    private Instant modified;

    @Schema(description = "Name of the sensor constant", required = true)
    @NotNull
    private String name;

    @Schema(description = "Value of the sensor constant", required = true)
    @NotNull
    private Integer value;

    @JsonIgnore
    @NotNull
    private Integer validFrom;

    @JsonIgnore
    @NotNull
    private Integer validTo;

    public TmsSensorConstantValueDto() {
    }

    public TmsSensorConstantValueDto(@NotNull final Long lotjuId,
                                     @NotNull final String name, @NotNull final Integer value,
                                     @NotNull final Integer validFrom, @NotNull final Integer validTo,
                                     @NotNull final Long roadStationId, @NotNull final Instant modified) {
        this.lotjuId = lotjuId;
        this.name = name;
        this.value = value;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.roadStationId = roadStationId;
        this.modified = modified;
    }

    public String getName() {
        return name;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getValidFrom() {
        return validFrom;
    }

    public Integer getValidTo() {
        return validTo;
    }

    @Schema(description = "Validity start in format mm-dd ie. value 01-31 is 31th of January", required = true)
    @JsonProperty("validFrom")
    public String getValidFromFormated() {
        return formatValidity(validFrom);
    }

    @Schema(description = "Validity end in format mm-dd ie. value 01-31 is 31th of January", required = true)
    @JsonProperty("validTo")
    public String getValidToFormated() {
        return formatValidity(validTo);
    }

    public Long getRoadStationId() {
        return roadStationId;
    }

    public Instant getModified() {
        return modified;
    }

    public void setModified(final Instant modified) {
        this.modified = modified;
    }

    private static String formatValidity(final Integer value) {
        return String.format("%02d-%02d", value / 100, value % 100);
    }

    public Long getConstantLotjuId() {
        return constantLotjuId;
    }
}
