package fi.livi.digitraffic.tie.dto.trafficmessage.v1.location;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.LocationUtils;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location type")
@JsonPropertyOrder({ "typeCode", "descriptionFi", "descriptionEn" })
public interface LocationTypeDtoV1 extends Comparable<LocationTypeDtoV1> {
    @Value("#{target.id.typeCode}")
    @Schema(description = "Code of location type", requiredMode = Schema.RequiredMode.REQUIRED)
    String getTypeCode();

    @Schema(description = "Description of type in english", requiredMode = Schema.RequiredMode.REQUIRED)
    String getDescriptionEn();

    @Schema(description = "Description of type in finnish", requiredMode = Schema.RequiredMode.REQUIRED)
    String getDescriptionFi();

    @Override
    default int compareTo(final LocationTypeDtoV1 o) {
        return LocationUtils.compareTypesOrVersions(getTypeCode(), o.getTypeCode());
    }
}
