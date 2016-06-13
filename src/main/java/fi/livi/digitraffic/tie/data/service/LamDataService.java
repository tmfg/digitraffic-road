package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;

public interface LamDataService {
    LamRootDataObjectDto listAllLamDataFromNonObsoleteStations();
}
