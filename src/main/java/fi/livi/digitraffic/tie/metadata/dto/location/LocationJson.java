package fi.livi.digitraffic.tie.metadata.dto.location;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

public interface LocationJson {
    int getLocationCode();

    @Value("#{target.locationSubtype.subtypeCode}")
    String getSubtypeCode();

    String getRoadJunction();

    String getRoadName();
    String getFirstName();
    String getSecondName();

    @Value("#{target.areaRef == null ? null : target.areaRef.locationCode}")
    Integer getAreaRef();

    @Value("#{target.linearRef == null ? null : target.linearRef.locationCode}")
    Integer getLinearRef();

    Integer getNegOffset();

    Integer getPosOffset();

    Boolean getUrban();

    BigDecimal getWgs84Lat();
    BigDecimal getWgs84Long();

    BigDecimal getEtrsTm35FinX();
    BigDecimal getEtrsTm35FixY();

    String getNegDirection();
    String getPosDirection();

    String getGeocode();
    String getOrderOfPoint();
}
