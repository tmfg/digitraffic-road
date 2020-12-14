package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;

import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "GeoJSON Properties object")
public abstract class Properties extends JsonAdditionalProperties implements Serializable {

}