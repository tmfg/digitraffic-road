package fi.livi.digitraffic.tie.dao.v1.forecast;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.v3.oas.annotations.media.Schema;

public interface ForecastSectionDto {

    ObjectMapper mapper = new ObjectMapper();

    /**
     * Road section identifier 15 characters ie. 00004_112_000_0
     * 1. Road number 5 characters ie. 00004
     * 2. Road section 3 characters ie. 112
     * 3. Road section version 3 characters ie. 000
     * 4. Reserver for future needs 1 characters default 0
     * Delimiter is underscore "_"
     */

    @Schema(description =
            "Forecast section identifier 15 characters ie. 00004_112_000_0: \n" +
            "1. Road number 5 characters ie. 00004, \n" +
            "2. Road section 3 characters ie. 112, \n" +
            "3. Road section version 3 characters ie. 000, \n" +
            "4. Reserved for future needs 1 characters default 0")
    @JsonProperty("roadId")
    String getNaturalId();

    @Schema(description = "Forecast section description")
    String getDescription();

    @Schema(description = "Forecast section length")
    Integer getLength();

    @Schema(description = "Forecast section road number")
    int getRoadNumber();

    @Schema(description = "Road section number")
    int getRoadSectionNumber();

    // linkids, in string as "134, 3243, 4354, 345543"
    String getLinkIdsAsString();

    String getGeometryAsGeoJsonString();

    String getGeometrySimplifiedAsGeoJsonString();

    String getRoadSegmentsAsJsonString();

    Instant getModified();

    default List<Long> getLinkIds() {
        if (getLinkIdsAsString() == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(getLinkIdsAsString().split(",")).map(Long::parseLong).collect(Collectors.toList());
    }

    default Geometry<?> getGeometry() {
        return PostgisGeometryHelper.convertFromGeoJSONStringToGeoJSON(getGeometryAsGeoJsonString());
    }

    default Geometry<?> getGeometrySimplified() {
        return PostgisGeometryHelper.convertFromGeoJSONStringToGeoJSON(getGeometrySimplifiedAsGeoJsonString());
    }



    default List<RoadSegmentDto> getRoadSegments()  {
        try {
            return mapper.readValue(getRoadSegmentsAsJsonString(), new TypeReference<>() {} );
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }



}
