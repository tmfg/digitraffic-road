package fi.livi.digitraffic.tie.metadata.geojson.traveltime;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootMetadataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of travel time links", value = "LinkFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "features" })
public class LinkFeatureCollection extends RootMetadataObjectDto implements Iterable<LinkFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    public final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    public final List<LinkFeature> features;

    public LinkFeatureCollection(final List<LinkFeature> linkFeatures, final ZonedDateTime localTimestamp, final ZonedDateTime dataLastCheckedTime) {
        super(localTimestamp, dataLastCheckedTime);
        this.features = linkFeatures;
    }

    public void addAll(final Collection<LinkFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<LinkFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "LinkFeatureCollection{" + "features=" + features + '}';
    }
}
