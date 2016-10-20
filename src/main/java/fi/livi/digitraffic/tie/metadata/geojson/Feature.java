package fi.livi.digitraffic.tie.metadata.geojson;

public interface Feature {
    Point getGeometry();
    void setGeometry(final Point geometry);
}
