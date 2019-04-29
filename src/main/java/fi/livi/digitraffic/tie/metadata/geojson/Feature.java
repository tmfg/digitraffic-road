package fi.livi.digitraffic.tie.metadata.geojson;

public interface Feature<T extends Geometry> {

    String getType();

    T getGeometry();

    void setGeometry(T geometry);
}
