package fi.livi.digitraffic.tie.dto.trafficmessage.v1.location;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.LocationUtils;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location subtype")
@JsonPropertyOrder({ "subtypeCode", "descriptionFi", "descriptionEn" })
public interface LocationSubtypeDtoV1 extends Comparable<LocationSubtypeDtoV1> {
    @Value("#{target.id.subtypeCode}")
    String getSubtypeCode();

    String getDescriptionEn();

    String getDescriptionFi();

    @Override
    default int compareTo(final LocationSubtypeDtoV1 o) {
        return LocationUtils.compareTypesOrVersions(getSubtypeCode(), o.getSubtypeCode());
    }
}
