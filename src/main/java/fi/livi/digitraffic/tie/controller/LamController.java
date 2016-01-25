package fi.livi.digitraffic.tie.controller;

import org.geojson.FeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.dao.LamStationRepository;

@RestController
public class LamController {
    @Autowired
    private LamStationRepository lamStationRepository;

    @RequestMapping("/api/v1/metadata/lam-stations")
    public FeatureCollection listNonObsoleteLamStations() {
        return Lam2FeatureConverter.convert(lamStationRepository.listaLamStations());
    }
}
