package fi.livi.digitraffic.tie.model.roadstation;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum SensorValueReliability {

    OK("OK"),
    SUSPICIOUS("KYSEENALAINEN"),
    FAULTY("VIALLINEN"),
    UNKNOWN("TUNTEMATON");

    private static final Logger log = LoggerFactory.getLogger(SensorValueReliability.class);

    private final String srcTypeString;

    private static final Map<String, SensorValueReliability> lookup = new HashMap<>();

    SensorValueReliability(final String srcType) {
        this.srcTypeString = srcType;
    }

    static{
        for (final SensorValueReliability rst : EnumSet.allOf(SensorValueReliability.class)) {
            lookup.put(rst.getSrcType(), rst);
        }
    }

    public static SensorValueReliability fromSrcType(final String srcType) {
        if (StringUtils.isBlank(srcType)) {
            return null;
        }
        final SensorValueReliability value = lookup.get(srcType);
        if (value == null) {
            log.error("method=fromSrcType SensorValueReliability not found for srcType {} using {}", srcType, UNKNOWN);
            return UNKNOWN;
        }
        return value;
    }

    public String getSrcType() {
        return srcTypeString;
    }
}
