package fi.livi.digitraffic.tie.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public enum RoadStationType {
    LAM_STATION(1),
    WEATHER_STATION(2),
    CAMERA(3);

    private static final Logger LOG = Logger.getLogger(RoadStationType.class);

    private final int typeNumber;

    RoadStationType(final int typeNumber) {
        this.typeNumber = typeNumber;
    }

    private static final Map<Integer, RoadStationType> lookup = new HashMap<Integer, RoadStationType>();

    static{
        for (final RoadStationType rst : EnumSet.allOf(RoadStationType.class)) {
            lookup.put(rst.getTypeNumber(), rst);
        }
    }

    public static RoadStationType fromTypeNumber(final int typeNumber) {
        final RoadStationType value = lookup.get(typeNumber);
        if (value == null) {
            LOG.error("RoadStationType not found for typeNumber " + typeNumber);
        }
        return value;
    }

    public int getTypeNumber() {
        return typeNumber;
    }
}
