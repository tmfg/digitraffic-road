package fi.livi.digitraffic.tie.dto.geojson.v1;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

public abstract class PropertiesWithIdV1<ID_TYPE> extends PropertiesV1 {

    @Schema(description = "Id of the road station", requiredMode = REQUIRED)
    public final ID_TYPE id;

    public PropertiesWithIdV1(final ID_TYPE id) {
        this.id = id;
    }
}
