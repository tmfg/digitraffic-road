package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.lam.LamDataObjectDto;

public interface LamDataService {
    LamDataObjectDto listAllLamDataFromNonObsoleteStations();
}
