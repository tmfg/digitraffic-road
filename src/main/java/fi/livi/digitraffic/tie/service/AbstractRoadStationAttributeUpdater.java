package fi.livi.digitraffic.tie.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;

public abstract class AbstractRoadStationAttributeUpdater {

    public static boolean setRoadAddressIfNotSet(final RoadStation rs) {
        if (rs.getRoadAddress() == null) {
            rs.setRoadAddress(new RoadAddress());
            return true;
        }
        return false;
    }

    public static BigDecimal getScaledToDbCoordinate(final BigDecimal value) {
        return value != null ? value.setScale(0, RoundingMode.HALF_UP) : null;
    }

    public static BigDecimal getScaledToDbAltitude(final BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP) : null;
    }

    public abstract boolean updateStationToObsoleteWithLotjuId(final long lotjuId);
}
