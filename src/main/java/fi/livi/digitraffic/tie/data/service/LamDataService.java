package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.model.LamDataObject;

public interface LamDataService {
    LamDataObject listAllLamDataFromNonObsoleteStations();
}
