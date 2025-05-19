package fi.livi.digitraffic.tie.service;

import static fi.livi.digitraffic.common.util.TimeUtil.withoutMillis;
import static fi.livi.digitraffic.tie.model.DataType.CAMERA_STATION_IMAGE_UPDATED;
import static fi.livi.digitraffic.tie.model.DataType.CAMERA_STATION_METADATA;
import static fi.livi.digitraffic.tie.model.DataType.CAMERA_STATION_METADATA_CHECK;
import static fi.livi.digitraffic.tie.model.DataType.WEATHER_STATION_METADATA_CHECK;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.common.dto.info.v1.UpdateInfoDtoV1;
import fi.livi.digitraffic.common.dto.info.v1.UpdateInfosDtoV1;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.controller.maintenance.MaintenanceTrackingControllerV1;
import fi.livi.digitraffic.tie.controller.tms.TmsControllerV1;
import fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV1;
import fi.livi.digitraffic.tie.controller.weather.WeatherControllerV1;
import fi.livi.digitraffic.tie.controller.weathercam.WeathercamControllerV1;
import fi.livi.digitraffic.tie.dao.DataUpdatedRepository;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueRepository;
import fi.livi.digitraffic.tie.dao.tms.TmsSensorConstantValueDtoV1Repository;
import fi.livi.digitraffic.tie.dao.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.dao.trafficmessage.datex2.Datex2Repository;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationVersionRepository;
import fi.livi.digitraffic.tie.dao.variablesign.v1.DeviceDataRepositoryV1;
import fi.livi.digitraffic.tie.dao.variablesign.v1.DeviceRepositoryV1;
import fi.livi.digitraffic.tie.dao.weather.WeatherStationRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionWeatherRepository;
import fi.livi.digitraffic.tie.dto.info.v1.DataSourceInfoDtoV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingDomainDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.weather.forecast.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.model.DataSource;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationVersion;

@Service
public class DataStatusService {

    private final DataUpdatedRepository dataUpdatedRepository;
    private final MaintenanceTrackingRepository maintenanceTrackingRepository;
    private final Datex2Repository datex2Repository;

    private final TmsStationRepository tmsStationRepository;
    private final TmsSensorConstantValueDtoV1Repository tmsSensorConstantValueDtoRepository;
    private final WeatherStationRepository weatherStationRepository;
    private final SensorValueRepository sensorValueRepository;
    private final ForecastSectionRepository forecastSectionRepository;
    private final ForecastSectionWeatherRepository forecastSectionWeatherRepository;
    private final DeviceRepositoryV1 deviceRepositoryV1;
    private final DeviceDataRepositoryV1 deviceDataRepositoryV1;

    private final LocationVersionRepository locationVersionRepository;

    @Autowired
    public DataStatusService(final DataUpdatedRepository dataUpdatedRepository,
                             final MaintenanceTrackingRepository maintenanceTrackingRepository,
                             final Datex2Repository datex2Repository,
                             final TmsStationRepository tmsStationRepository,
                             final WeatherStationRepository weatherStationRepository,
                             final SensorValueRepository sensorValueRepository,
                             final ForecastSectionWeatherRepository forecastSectionWeatherRepository,
                             final DeviceRepositoryV1 deviceRepositoryV1,
                             final DeviceDataRepositoryV1 deviceDataRepositoryV1,
                             final LocationVersionRepository locationVersionRepository,
                             final TmsSensorConstantValueDtoV1Repository tmsSensorConstantValueDtoRepository,
                             final ForecastSectionRepository forecastSectionRepository) {
        this.dataUpdatedRepository = dataUpdatedRepository;
        this.maintenanceTrackingRepository = maintenanceTrackingRepository;
        this.datex2Repository = datex2Repository;
        this.tmsStationRepository = tmsStationRepository;
        this.weatherStationRepository = weatherStationRepository;
        this.sensorValueRepository = sensorValueRepository;
        this.forecastSectionWeatherRepository = forecastSectionWeatherRepository;
        this.deviceRepositoryV1 = deviceRepositoryV1;
        this.deviceDataRepositoryV1 = deviceDataRepositoryV1;
        this.locationVersionRepository = locationVersionRepository;
        this.tmsSensorConstantValueDtoRepository = tmsSensorConstantValueDtoRepository;
        this.forecastSectionRepository = forecastSectionRepository;
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType) {
        dataUpdatedRepository.upsertDataUpdated(dataType);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final String subtype) {
        dataUpdatedRepository.upsertDataUpdated(dataType, subtype);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final Instant updated) {
        dataUpdatedRepository.upsertDataUpdated(dataType, updated);
    }

    @Transactional(readOnly = true)
    public Instant findDataUpdatedTime(final DataType dataType) {
        return dataUpdatedRepository.findUpdatedTime(dataType);
    }

    @Transactional(readOnly = true)
    public Instant findDataUpdatedInstant(final DataType dataType) {
        return dataUpdatedRepository.findUpdatedTime(dataType);
    }

    @Transactional(readOnly = true)
    public Instant findDataUpdatedTime(final DataType dataType, final List<String> subtypes) {
        return dataUpdatedRepository.findUpdatedTime(dataType, subtypes);
    }

    @Transactional(readOnly = true)
    public Instant getTransactionStartTime() {
        return dataUpdatedRepository.getTransactionStartTime();
    }

    @Transactional(readOnly = true)
    public UpdateInfosDtoV1 getUpdatedInfos() {

        final List<UpdateInfoDtoV1> updatedInfos =
            Stream.of(
                    getMaintenanceUpdateInfos().stream(),
                    getTrafficMessageInfos().stream(),
                    getVariableSignInfos().stream(),
                    getCoungingSiteInfos().stream(),
                    getTmsInfos().stream(),
                    getWeatherInfos().stream(),
                    getWeathercamInfos().stream())
                .flatMap(Function.identity())
                .sorted(Comparator.comparing(o -> o.api))
                .collect(Collectors.toList());

        final Instant max =
            updatedInfos.stream()
                .map(updateInfoDtoV1 -> TimeUtil.getGreatest(updateInfoDtoV1.getDataUpdatedTime(), updateInfoDtoV1.dataCheckedTime))
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(Instant.EPOCH);
        return new UpdateInfosDtoV1(updatedInfos, max);
    }

    private List<UpdateInfoDtoV1> getMaintenanceUpdateInfos() {
        final List<MaintenanceTrackingDomainDtoV1> domains = maintenanceTrackingRepository.getDomains();
        final DataSourceInfoDtoV1 stateInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.MAINTENANCE_TRACKING);
        final DataSourceInfoDtoV1 municipalityInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.MAINTENANCE_TRACKING_MUNICIPALITY);
        return domains.stream().map(d -> {
            final String domain = d.getName();
            final String updateInterval = domain.contains("state") ? stateInfo.getUpdateInterval() : municipalityInfo.getUpdateInterval();
            final String recommendedFetchInterval =
                domain.contains("state") ? stateInfo.getRecommendedFetchInterval() : municipalityInfo.getRecommendedFetchInterval();
            final Instant updated = maintenanceTrackingRepository.findLastUpdatedForDomain(Collections.singleton(domain));
            final Instant checked = findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA_CHECKED, Collections.singletonList(domain));
            return new UpdateInfoDtoV1(MaintenanceTrackingControllerV1.API_MAINTENANCE_V1_TRACKING_ROUTES, d.getName(), updated, checked,
                updateInterval, recommendedFetchInterval);
        }).collect(Collectors.toList());
    }

    private List<UpdateInfoDtoV1> getTrafficMessageInfos() {
        final DataSourceInfoDtoV1 trafficMessageInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.TRAFFIC_MESSAGE);
        final List<UpdateInfoDtoV1> trafficMessageInfos =
            Arrays.stream(SituationType.values())
                .map(situationType -> {
                    final Instant updated = datex2Repository.getLastModified(situationType.name());
                    return new UpdateInfoDtoV1(TrafficMessageControllerV1.API_TRAFFIC_MESSAGE_V1_MESSAGES, situationType.name(), updated, null,
                        trafficMessageInfo.getUpdateInterval(), trafficMessageInfo.getRecommendedFetchInterval());
                })
                .collect(Collectors.toList());

        // /api/traffic-message/v1/area-geometries
        final DataSourceInfoDtoV1 areaInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.TRAFFIC_MESSAGE_AREA);
        final Instant regionsUpdated = findDataUpdatedInstant(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA);
        final Instant regionsChecked = findDataUpdatedInstant(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA_CHECK);
        trafficMessageInfos.add(
            new UpdateInfoDtoV1(TrafficMessageControllerV1.API_TRAFFIC_MESSAGE_V1 + TrafficMessageControllerV1.AREA_GEOMETRIES, regionsUpdated,
                regionsChecked,
                areaInfo.getUpdateInterval(), areaInfo.getRecommendedFetchInterval()));

        final DataSourceInfoDtoV1 locationInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.TRAFFIC_MESSAGE_LOCATION);
        final LocationVersion locationVersion = locationVersionRepository.findLatestVersion();
        final Instant locationUpdated = withoutMillis(locationVersion.getModified());

        trafficMessageInfos.add(
            new UpdateInfoDtoV1(TrafficMessageControllerV1.API_TRAFFIC_MESSAGE_V1 + TrafficMessageControllerV1.LOCATIONS, locationUpdated, null,
                locationInfo.getUpdateInterval(), locationInfo.getRecommendedFetchInterval()));

        return trafficMessageInfos;
    }

    private List<UpdateInfoDtoV1> getVariableSignInfos() {
        final DataSourceInfoDtoV1 signsInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.VARIABLE_SIGN_DATA);

        final Instant jsonDataUpdated = TimeUtil.getGreatest(deviceRepositoryV1.getLastUpdated(), deviceDataRepositoryV1.getLastUpdated());
        final Instant datex2DataUpdated = deviceDataRepositoryV1.getDatex2LastUpdated();
        final Instant codeDescriptionsUpdated = LocalDate.of(2019, 10, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return Arrays.asList(
            new UpdateInfoDtoV1(ApiConstants.API_VS_V1 + ApiConstants.API_SIGNS, jsonDataUpdated, signsInfo.getUpdateInterval(),
                signsInfo.getRecommendedFetchInterval()),
            new UpdateInfoDtoV1(ApiConstants.API_VS_V1 + ApiConstants.API_SIGNS_DATEX2, datex2DataUpdated,
                signsInfo.getUpdateInterval(), signsInfo.getRecommendedFetchInterval()),
            UpdateInfoDtoV1.staticData(ApiConstants.API_VS_V1 + ApiConstants.API_SIGNS_CODE_DESCRIPTIONS, codeDescriptionsUpdated)
        );
    }

    private List<UpdateInfoDtoV1> getCoungingSiteInfos() {
        // meta: user-types, domains, directions ja counters, user-types and directions are static
        // data: values
        final DataSourceInfoDtoV1 metadataInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.COUNTING_SITE);
        final DataSourceInfoDtoV1 dataInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.COUNTING_SITE_DATA);

        final Instant staticMetadataUpdated = LocalDate.of(2022, 3, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant countersUpdated = dataUpdatedRepository.getCountingSiteCounterLastUpdated();
        final Instant countersChecked = dataUpdatedRepository.findUpdatedTime(DataType.COUNTING_SITES_METADATA_CHECK);
        final Instant domainsUpdated = dataUpdatedRepository.getCountingSiteDomainLastUpdated();
        final Instant dataUpdated = dataUpdatedRepository.getCountingSiteDataLastUpdated();
        final Instant dataChecked = dataUpdatedRepository.findUpdatedTime(DataType.COUNTING_SITES_DATA);

        return Arrays.asList(
            new UpdateInfoDtoV1(ApiConstants.API_COUNTING_SITE_V1_COUNTERS, countersUpdated, countersChecked,
                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval()),
            UpdateInfoDtoV1.staticData(ApiConstants.API_COUNTING_SITE_V1_DIRECTIONS, staticMetadataUpdated),
            new UpdateInfoDtoV1(ApiConstants.API_COUNTING_SITE_V1_DOMAIN, domainsUpdated,
                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval()),
            UpdateInfoDtoV1.staticData(ApiConstants.API_COUNTING_SITE_V1_USER_TYPES, staticMetadataUpdated),
            new UpdateInfoDtoV1(ApiConstants.API_COUNTING_SITE_V1_VALUES, dataUpdated, dataChecked,
                dataInfo.getUpdateInterval(), dataInfo.getRecommendedFetchInterval())
        );
    }

    private List<UpdateInfoDtoV1> getTmsInfos() {
        final DataSourceInfoDtoV1 metadataInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.TMS_STATION);
        final DataSourceInfoDtoV1 dataInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.TMS_STATION_DATA);
        final UpdateInfoDtoV1 stationsInfo =
            new UpdateInfoDtoV1(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS,
                tmsStationRepository.getLastUpdated(),
                findDataUpdatedInstant(DataType.TMS_STATION_METADATA_CHECK),
                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 sensorsInfo =
            new UpdateInfoDtoV1(TmsControllerV1.API_TMS_V1 + TmsControllerV1.SENSORS,
                findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_METADATA),
                findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_METADATA_CHECK),
                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 sensorConstantsInfo =
            new UpdateInfoDtoV1(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.SENSOR_CONSTANTS,
                // muuta
                tmsSensorConstantValueDtoRepository.getTmsSensorConstantsLastUpdated(),
                findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_CONSTANT_METADATA_CHECK),
                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 stationsDatasInfo =
            new UpdateInfoDtoV1(TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.DATA,
                sensorValueRepository.getLastModified(RoadStationType.TMS_STATION),
                dataInfo.getUpdateInterval(), dataInfo.getRecommendedFetchInterval());

        return Arrays.asList(stationsInfo, sensorsInfo, sensorConstantsInfo, stationsDatasInfo);
    }

    private List<UpdateInfoDtoV1> getWeatherInfos() {
        final DataSourceInfoDtoV1 metadataInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.WEATHER_STATION);
        final DataSourceInfoDtoV1 dataInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.WEATHER_STATION_DATA);

        final UpdateInfoDtoV1 stationsInfo =
            new UpdateInfoDtoV1(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS,
                weatherStationRepository.getLastUpdated(), findDataUpdatedInstant(WEATHER_STATION_METADATA_CHECK),
                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 sensorsInfo =
            new UpdateInfoDtoV1(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.SENSORS,
                findDataUpdatedInstant(DataType.WEATHER_STATION_SENSOR_METADATA),
                findDataUpdatedInstant(DataType.WEATHER_STATION_SENSOR_METADATA_CHECK),
                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 stationsDatasInfo =
            new UpdateInfoDtoV1(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.STATIONS + WeatherControllerV1.DATA,
                sensorValueRepository.getLastModified(RoadStationType.WEATHER_STATION),
                dataInfo.getUpdateInterval(), dataInfo.getRecommendedFetchInterval());

        final DataSourceInfoDtoV1 forecastSectionInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.FORECAST_SECTION);
        final DataSourceInfoDtoV1 forecastSectionsForecastInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.FORECAST_SECTION_FORECAST);

        final UpdateInfoDtoV1 forecastSectionsSimpleInfo =
            new UpdateInfoDtoV1(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE,
                findDataUpdatedInstant(DataType.FORECAST_SECTION_METADATA),
                findDataUpdatedInstant(DataType.FORECAST_SECTION_METADATA_CHECK),
                forecastSectionInfo.getUpdateInterval(), forecastSectionInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 forecastSectionsSimpleForecastsInfo =
            new UpdateInfoDtoV1(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + WeatherControllerV1.FORECASTS,
                forecastSectionWeatherRepository.getLastModified(ForecastSectionApiVersion.V1.getVersion(), null, null),
                findDataUpdatedInstant(DataType.FORECAST_SECTION_WEATHER_DATA_CHECK),
                forecastSectionsForecastInfo.getUpdateInterval(), forecastSectionsForecastInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 forecastSectionsInfo =
            new UpdateInfoDtoV1(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS,
                forecastSectionRepository.getLastModified(ForecastSectionApiVersion.V2.getVersion()),
                findDataUpdatedInstant(DataType.FORECAST_SECTION_V2_METADATA_CHECK),
                forecastSectionInfo.getUpdateInterval(), forecastSectionInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 forecastSectionsForecastsInfo =
            new UpdateInfoDtoV1(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + WeatherControllerV1.FORECASTS,
                forecastSectionWeatherRepository.getLastModified(ForecastSectionApiVersion.V2.getVersion(), null, null),
                findDataUpdatedInstant(DataType.FORECAST_SECTION_V2_WEATHER_DATA_CHECK),
                forecastSectionsForecastInfo.getUpdateInterval(), forecastSectionsForecastInfo.getRecommendedFetchInterval());

        return Arrays.asList(stationsInfo, sensorsInfo, stationsDatasInfo,
            forecastSectionsSimpleInfo, forecastSectionsSimpleForecastsInfo,
            forecastSectionsInfo, forecastSectionsForecastsInfo);
    }

    private List<UpdateInfoDtoV1> getWeathercamInfos() {
        final DataSourceInfoDtoV1 metadataInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.WEATHERCAM_STATION);
        final DataSourceInfoDtoV1 dataInfo =
            dataUpdatedRepository.getDataSourceInfo(DataSource.WEATHERCAM_STATION_DATA);

        final Instant stationsUpdated = findDataUpdatedInstant(CAMERA_STATION_METADATA);
        final Instant stationsChecked = findDataUpdatedInstant(CAMERA_STATION_METADATA_CHECK);

        final UpdateInfoDtoV1 stationsInfo =
            new UpdateInfoDtoV1(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS,
                stationsUpdated, stationsChecked, metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval());

        final Instant dataUpdated = findDataUpdatedInstant(CAMERA_STATION_IMAGE_UPDATED);
        final UpdateInfoDtoV1 stationsDatasInfo =
            new UpdateInfoDtoV1(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + WeathercamControllerV1.DATA,
                dataUpdated, dataInfo.getUpdateInterval(), dataInfo.getRecommendedFetchInterval());

        return Arrays.asList(stationsInfo, stationsDatasInfo);
    }

    @Transactional(readOnly = true)
    public DataSourceInfoDtoV1 getDataSourceInfo(final DataSource dataSource) {
        return dataUpdatedRepository.getDataSourceInfo(dataSource);
    }

    @Transactional(readOnly = true)
    public String getDataSourceUpdateInterval(final DataSource dataSource) {
        return dataUpdatedRepository.getDataSourceUpdateInterval(dataSource);
    }
}
