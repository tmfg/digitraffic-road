package fi.livi.digitraffic.tie.dto.trafficmessage.v1.location;

public interface LocationDtoV1 {
    int getLocationCode();

    String getSubtypeCode();
    String getRoadJunction();

    String getRoadName();
    String getFirstName();
    String getSecondName();

    Integer getAreaRef();
    Integer getLinearRef();

    Integer getNegOffset();

    Integer getPosOffset();

    Boolean getUrban();

    Double getWgs84Lat();
    Double getWgs84Long();

    Double getEtrsTm35FinX();
    Double getEtrsTm35FixY();

    String getNegDirection();
    String getPosDirection();

    String getGeocode();
    String getOrderOfPoint();
}
