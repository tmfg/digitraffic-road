package fi.livi.digitraffic.tie.metadata.geojson;

import io.swagger.v3.oas.annotations.media.Schema;

public abstract class PropertiesWithId<ID_TYPE> extends Properties {

    @Schema(description = "Id of the object", required = true)
    public final ID_TYPE id;

    public PropertiesWithId(final ID_TYPE id) {
        this.id = id;
    }

}