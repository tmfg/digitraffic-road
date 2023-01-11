package fi.livi.digitraffic.tie.dto.variablesigns.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of variable signs")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class VariableSignFeatureCollectionV1 extends FeatureCollectionV1<VariableSignFeatureV1> {

    public VariableSignFeatureCollectionV1(final Instant dataUpdatedTime,  final List<VariableSignFeatureV1> features) {
        super(dataUpdatedTime, features);
    }
}
