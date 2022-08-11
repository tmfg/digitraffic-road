package fi.livi.digitraffic.tie.dto.tms.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of TMS stations")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class TmsStationFeatureCollectionSimpleV1 extends FeatureCollectionV1<TmsStationFeatureSimpleV1> {

    public TmsStationFeatureCollectionSimpleV1(final Instant dataUpdatedTime, final Instant dataLastCheckedTime,
                                               final List<TmsStationFeatureSimpleV1> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }
}
