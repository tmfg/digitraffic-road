package fi.livi.digitraffic.tie.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;

import fi.livi.digitraffic.tie.model.LamStationData;

public final class Lam2FeatureConverter {
    private Lam2FeatureConverter() {}

    public static FeatureCollection convert(final List<LamStationData> stations) {
        final FeatureCollection collection = new FeatureCollection();

        for(final LamStationData lam : stations) {
            collection.add(convert(lam));
        }

        return collection;
    }

    private static Feature convert(final LamStationData lam) {
        final Feature f = new Feature();

        f.setProperty("lamNumber", lam.getLamNumber());
        f.setProperty("rwsName", lam.getRwsName());
        f.setProperty("name", getNames(lam));
        f.setProperty("province", lam.getProvince());

        f.setGeometry(new Point(lam.getX(), lam.getY(), lam.getZ()));

        return f;
    }

    private static Map<String, String> getNames(final LamStationData lam) {
        final Map<String, String> map = new HashMap<>();

        map.put("fi", lam.getName());
        map.put("sw", lam.getName());
        map.put("en", lam.getName());

        return map;
    }
}
