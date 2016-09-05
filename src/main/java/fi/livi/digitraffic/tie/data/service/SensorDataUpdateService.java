package fi.livi.digitraffic.tie.data.service;

import java.sql.SQLException;
import java.util.List;

import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;

public interface SensorDataUpdateService {

    void updateLamData(List<Lam> data) throws SQLException;

    void updateWeatherData(List<Tiesaa> data) throws SQLException;
}
