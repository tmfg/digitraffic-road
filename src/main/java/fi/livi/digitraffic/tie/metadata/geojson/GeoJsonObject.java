package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON object", discriminatorProperty = "type")
public abstract class GeoJsonObject extends JsonAdditionalProperties implements Serializable {

    public GeoJsonObject() {
    }

    @Schema(description = "GeoJSON Object Type", requiredMode = Schema.RequiredMode.REQUIRED)
    public abstract String getType();

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
