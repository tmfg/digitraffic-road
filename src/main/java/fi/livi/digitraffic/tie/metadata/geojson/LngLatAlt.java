package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fi.livi.digitraffic.tie.metadata.geojson.jackson.LngLatAltDeserializer;
import fi.livi.digitraffic.tie.metadata.geojson.jackson.LngLatAltSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "", description = "GeoJson Point coordinates")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = LngLatAltDeserializer.class)
@JsonSerialize(using = LngLatAltSerializer.class)
public class LngLatAlt implements Serializable {

    @ApiModelProperty(value = "Longitude (X-coordinate) in ETRS89 (EUREF-FIN)", required = true, position = 1)
    private double longitude;

    @ApiModelProperty(value = "Latitude (Y-coordinate) in ETRS89 (EUREF-FIN)", required = true, position = 2)
    private double latitude;

    @ApiModelProperty(value = "Altitude (Z-coordinate) in metres", required = false, position = 3)
    private double altitude = Double.NaN;

    public LngLatAlt() {
    }

    public LngLatAlt(final double longitude, final double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public LngLatAlt(final double longitude, final double latitude, final double altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    public boolean hasAltitude() {
        return !Double.isNaN(altitude);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(final double altitude) {
        this.altitude = altitude;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LngLatAlt)) {
            return false;
        }
        final LngLatAlt lngLatAlt = (LngLatAlt) o;
        return Double.compare(lngLatAlt.latitude, latitude) == 0 && Double.compare(lngLatAlt.longitude, longitude) == 0
                && Double.compare(lngLatAlt.altitude, altitude) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(longitude);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(altitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LngLatAlt{" + "longitude=" + longitude + ", latitude=" + latitude + ", altitude=" + altitude + '}';
    }
}
