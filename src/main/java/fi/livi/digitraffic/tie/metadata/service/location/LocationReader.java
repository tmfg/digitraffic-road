package fi.livi.digitraffic.tie.metadata.service.location;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fi.livi.digitraffic.tie.metadata.model.location.Location;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;

public class LocationReader extends AbstractReader<Location> {
    private final Map<Integer, Location> locationMap;
    private final Map<String, LocationSubtype> subtypeMap;

    public LocationReader(final Map<Integer, Location> locationMap, final Map<String, LocationSubtype> subtypeMap) {
        this.locationMap = locationMap;
        this.subtypeMap = subtypeMap;
    }

    @Override protected Location convert(final String line) {
        final String components[] = StringUtils.splitPreserveAllTokens(line, DELIMETER);
        final Location location = new Location();

        location.setLocationCode(parseInteger(components[2]));
        location.setRoadJunction(components[6]);
        location.setRoadName(components[7]);
        location.setFirstName(components[8]);
        location.setSecondName(components[9]);
        location.setNegOffset(parseInteger(components[12]));
        location.setPosOffset(parseInteger(components[13]));
        location.setUrban(parseBoolean(components[14]));
        location.setWgs84Lat(parseDecimal(components[16]));
        location.setWgs84Long(parseDecimal(components[17]));
        location.setPosDirection(components[21]);
        location.setNegDirection(components[22]);

        location.setAreaRef(parseReference(components[10], locationMap));
        location.setLinearRef(parseReference(components[11], locationMap));
        location.setLocationSubtype(parseSubtype(components[3], components[4], components[5], subtypeMap));

        locationMap.put(location.getLocationCode(), location);

        return location;
    }

    private LocationSubtype parseSubtype(final String classValue, final String typeValue, final String subtypeValue, final Map<String, LocationSubtype> subtypeMap) {
        final String subtypeCode = String.format("%s%s.%s", classValue, typeValue, subtypeValue);

        final LocationSubtype subtype = subtypeMap.get(subtypeCode);

        if(subtype == null) {
            log.error("Could not find subtype " + subtypeCode);
        }

        return subtype;
    }

    private Boolean parseBoolean(final String value) {
        final Integer i = parseInteger(value);

        return i == null ? null : (i == 0 ? false : true);
    }

    private BigDecimal parseDecimal(final String value) {
        return StringUtils.isEmpty(value) ? null : new BigDecimal(value).setScale(5, BigDecimal.ROUND_HALF_UP);
    }

    private Location parseReference(final String value, final Map<Integer, Location> locationMap) {
        final Integer refValue = parseInteger(value);

        // for some reason, there is no 0 present
        if(refValue == null || refValue == 0) return null;

        final Location refLocation = locationMap.get(refValue);

        if(refLocation == null) {
            log.error("Could not find reference " + refValue);
        }

        return refLocation;
    }

    private Integer parseInteger(final String value) {
        return StringUtils.isEmpty(value) ? null : Integer.parseInt(value);
    }
}
