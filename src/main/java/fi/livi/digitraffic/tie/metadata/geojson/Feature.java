package fi.livi.digitraffic.tie.metadata.geojson;

public interface Feature<T> {

    String getType();

    T getGeometry();

    void setGeometry(T geometry);
}
