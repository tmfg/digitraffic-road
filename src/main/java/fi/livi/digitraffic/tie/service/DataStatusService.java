package fi.livi.digitraffic.tie.service;

import static fi.livi.digitraffic.tie.helper.DateHelper.withoutMillis;
import static fi.livi.digitraffic.tie.model.DataType.CAMERA_STATION_IMAGE_UPDATED;
import static fi.livi.digitraffic.tie.model.DataType.CAMERA_STATION_METADATA;
import static fi.livi.digitraffic.tie.model.DataType.CAMERA_STATION_METADATA_CHECK;
import static fi.livi.digitraffic.tie.model.DataType.WEATHER_STATION_METADATA_CHECK;

import java.time.Instant;
import java.time.ZonedDateTime;
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

import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.controller.maintenance.MaintenanceTrackingControllerV1;
import fi.livi.digitraffic.tie.controller.tms.TmsControllerV1;
import fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV1;
import fi.livi.digitraffic.tie.controller.weather.WeatherControllerV1;
import fi.livi.digitraffic.tie.controller.weathercam.WeathercamControllerV1;
import fi.livi.digitraffic.tie.dao.v1.DataUpdatedRepository;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.dao.v1.ForecastSectionWeatherRepository;
import fi.livi.digitraffic.tie.dao.v1.SensorValueRepository;
import fi.livi.digitraffic.tie.dao.v1.WeatherStationRepository;
import fi.livi.digitraffic.tie.dao.v1.location.LocationVersionRepository;
import fi.livi.digitraffic.tie.dao.v1.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.dao.v2.V2DeviceDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2DeviceRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.v3.V3CodeDescriptionRepository;
import fi.livi.digitraffic.tie.dto.info.v1.DataSourceInfoDtoV1;
import fi.livi.digitraffic.tie.dto.info.v1.UpdateInfoDtoV1;
import fi.livi.digitraffic.tie.dto.info.v1.UpdateInfosDtoV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingDomainDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.DataSource;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.location.LocationVersion;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;

@Service
public class DataStatusService {

    private final DataUpdatedRepository dataUpdatedRepository;
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    private final Datex2Repository datex2Repository;

    private final TmsStationRepository tmsStationRepository;
    private final WeatherStationRepository weatherStationRepository;
    private final SensorValueRepository sensorValueRepository;
    private final ForecastSectionWeatherRepository forecastSectionWeatherRepository;
    private final V2DeviceRepository v2DeviceRepository;
    private final V2DeviceDataRepository v2DeviceDataRepository;
    private final V3CodeDescriptionRepository v3CodeDescriptionRepository;

    private final LocationVersionRepository locationVersionRepository;

    @Autowired
    public DataStatusService(final DataUpdatedRepository dataUpdatedRepository,
                             final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository,
                             final Datex2Repository datex2Repository,
                             final TmsStationRepository tmsStationRepository,
                             final WeatherStationRepository weatherStationRepository,
                             final SensorValueRepository sensorValueRepository,
                             final ForecastSectionWeatherRepository forecastSectionWeatherRepository,
                             final V2DeviceRepository v2DeviceRepository,
                             final V2DeviceDataRepository v2DeviceDataRepository,
                             final V3CodeDescriptionRepository v3CodeDescriptionRepository,
                             final LocationVersionRepository locationVersionRepository) {
        this.dataUpdatedRepository = dataUpdatedRepository;
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
        this.datex2Repository = datex2Repository;
        this.tmsStationRepository = tmsStationRepository;
        this.weatherStationRepository = weatherStationRepository;
        this.sensorValueRepository = sensorValueRepository;
        this.forecastSectionWeatherRepository = forecastSectionWeatherRepository;
        this.v2DeviceRepository = v2DeviceRepository;
        this.v2DeviceDataRepository = v2DeviceDataRepository;
        this.v3CodeDescriptionRepository = v3CodeDescriptionRepository;
        this.locationVersionRepository = locationVersionRepository;
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
    public ZonedDateTime findDataUpdatedTime(final DataType dataType) {
        return DateHelper.toZonedDateTimeAtUtc(dataUpdatedRepository.findUpdatedTime(dataType));
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
                .map(updateInfoDtoV1 -> DateHelper.getGreatest(updateInfoDtoV1.getDataUpdatedTime(), updateInfoDtoV1.dataCheckedTime))
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(Instant.EPOCH);
        return new UpdateInfosDtoV1(updatedInfos, max);
    }

    private List<UpdateInfoDtoV1> getMaintenanceUpdateInfos() {
        final List<MaintenanceTrackingDomainDtoV1> domains = v2MaintenanceTrackingRepository.getDomains();
        final DataSourceInfoDtoV1 stateInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.MAINTENANCE_TRACKING);
        final DataSourceInfoDtoV1 municipalityInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.MAINTENANCE_TRACKING_MUNICIPALITY);
        return domains.stream().map(d -> {
                final String domain = d.getName();
                final String updateInterval = domain.contains("state") ? stateInfo.getUpdateInterval() : municipalityInfo.getUpdateInterval();
                final String recommendedFetchInterval = domain.contains("state") ? stateInfo.getRecommendedFetchInterval() : municipalityInfo.getRecommendedFetchInterval();
                final Instant updated = v2MaintenanceTrackingRepository.findLastUpdatedForDomain(Collections.singletonList(domain));
                final Instant checked = findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA_CHECKED, Collections.singletonList(domain));
                return new UpdateInfoDtoV1(MaintenanceTrackingControllerV1.API_MAINTENANCE_V1_TRACKING_ROUTES, d.getName(), updated, checked, updateInterval, recommendedFetchInterval);
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
        trafficMessageInfos.add(new UpdateInfoDtoV1(TrafficMessageControllerV1.API_TRAFFIC_MESSAGE_V1 + TrafficMessageControllerV1.AREA_GEOMETRIES, regionsUpdated, regionsChecked,
                                                    areaInfo.getUpdateInterval(), areaInfo.getRecommendedFetchInterval()));

        final DataSourceInfoDtoV1 locationInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.TRAFFIC_MESSAGE_LOCATION);
        final LocationVersion locationVersion = locationVersionRepository.findLatestVersion();
        final Instant locationUpdated = withoutMillis(locationVersion.getModified());

        trafficMessageInfos.add(new UpdateInfoDtoV1(TrafficMessageControllerV1.API_TRAFFIC_MESSAGE_V1 + TrafficMessageControllerV1.LOCATIONS, locationUpdated, null,
                                                    locationInfo.getUpdateInterval(), locationInfo.getRecommendedFetchInterval()));

        return trafficMessageInfos;
    }

    private List<UpdateInfoDtoV1> getVariableSignInfos() {
        final DataSourceInfoDtoV1 metadataInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.VARIABLE_SIGN);
        final DataSourceInfoDtoV1 singsInfo = dataUpdatedRepository.getDataSourceInfo(DataSource.VARIABLE_SIGN_DATA);
        final Instant jsonDataUpdated = DateHelper.getGreatest(v2DeviceRepository.getLastUpdated(), v2DeviceDataRepository.getLastUpdated());
        final Instant datex2DataUpdated = v2DeviceDataRepository.getDatex2LastUpdated();
        final Instant codeDescriptionDataUpdated = v3CodeDescriptionRepository.getLastUpdated();
        return Arrays.asList(
            new UpdateInfoDtoV1(ApiConstants.API_VS_V1 + ApiConstants.API_SIGNS, jsonDataUpdated, singsInfo.getUpdateInterval(),
                                singsInfo.getRecommendedFetchInterval()),
            new UpdateInfoDtoV1(ApiConstants.API_VS_V1 + ApiConstants.API_SIGNS_DATEX2, datex2DataUpdated,
                                singsInfo.getUpdateInterval(), singsInfo.getRecommendedFetchInterval()),
            new UpdateInfoDtoV1(ApiConstants.API_VS_V1 + ApiConstants.API_SIGNS_CODE_DESCRIPTIONS, codeDescriptionDataUpdated,
                                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval()));
    }

    private List<UpdateInfoDtoV1> getCoungingSiteInfos() {
        // meta: user-types, domains, directions ja counters, user-types and directions are static
        // data: values
        final DataSourceInfoDtoV1 metadataInfo  =
            dataUpdatedRepository.getDataSourceInfo(DataSource.COUNTING_SITE);
        final DataSourceInfoDtoV1 dataInfo  =
            dataUpdatedRepository.getDataSourceInfo(DataSource.COUNTING_SITE_DATA);

        return Arrays.asList(
            new UpdateInfoDtoV1(ApiConstants.API_COUNTING_SITE_V1_COUNTERS, dataUpdatedRepository.getCountingSiteCounterLastUpdated(),
                                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval()),
            new UpdateInfoDtoV1(ApiConstants.API_COUNTING_SITE_V1_DIRECTIONS, findDataUpdatedInstant(DataType.COUNTING_SITE_DIRECTION_DATA),
                                null, metadataInfo.getRecommendedFetchInterval()),
            new UpdateInfoDtoV1(ApiConstants.API_COUNTING_SITE_V1_DOMAIN, dataUpdatedRepository.getCountingSiteDomainLastUpdated(),
                                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval()),
            new UpdateInfoDtoV1(ApiConstants.API_COUNTING_SITE_V1_USER_TYPES, findDataUpdatedInstant(DataType.COUNTING_SITE_USER_TYPE_DATA),
                null, metadataInfo.getRecommendedFetchInterval()),
            new UpdateInfoDtoV1(ApiConstants.API_COUNTING_SITE_V1_VALUES, dataUpdatedRepository.getCountingSiteDataLastUpdated(),
                dataInfo.getUpdateInterval(), dataInfo.getRecommendedFetchInterval())
        );
    }


    private List<UpdateInfoDtoV1> getTmsInfos() {
        final DataSourceInfoDtoV1 metadataInfo  =
            dataUpdatedRepository.getDataSourceInfo(DataSource.TMS_STATION);
        final DataSourceInfoDtoV1 dataInfo  =
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
            new UpdateInfoDtoV1( TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.SENSOR_CONSTANTS,
                                findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_CONSTANT_METADATA),
                                findDataUpdatedInstant(DataType.TMS_STATION_SENSOR_CONSTANT_METADATA_CHECK),
                                metadataInfo.getUpdateInterval(), metadataInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 stationsDatasInfo =
            new UpdateInfoDtoV1( TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS + TmsControllerV1.DATA,
                                sensorValueRepository.getLastModified(RoadStationType.TMS_STATION),
                                dataInfo.getUpdateInterval(), dataInfo.getRecommendedFetchInterval());

        return Arrays.asList(stationsInfo, sensorsInfo, sensorConstantsInfo, stationsDatasInfo);
    }

    private List<UpdateInfoDtoV1> getWeatherInfos() {
        final DataSourceInfoDtoV1 metadataInfo  =
            dataUpdatedRepository.getDataSourceInfo(DataSource.WEATHER_STATION);
        final DataSourceInfoDtoV1 dataInfo  =
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


        final DataSourceInfoDtoV1 forecastSectionInfo  =
            dataUpdatedRepository.getDataSourceInfo(DataSource.FORECAST_SECTION);
        final DataSourceInfoDtoV1 forecastSectionsForecastInfo  =
            dataUpdatedRepository.getDataSourceInfo(DataSource.FORECAST_SECTION_FORECAST);


        final UpdateInfoDtoV1 forecastSectionsSimpleInfo =
            new UpdateInfoDtoV1(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE,
                                findDataUpdatedInstant(DataType.FORECAST_SECTION_METADATA),
                                findDataUpdatedInstant(DataType.FORECAST_SECTION_METADATA_CHECK),
                                forecastSectionInfo.getUpdateInterval(), forecastSectionInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 forecastSectionsSimpleForecastsInfo =
            new UpdateInfoDtoV1( WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + WeatherControllerV1.FORECASTS,
                                forecastSectionWeatherRepository.getLastModified(ForecastSectionApiVersion.V1.getVersion(), null, null),
                                findDataUpdatedInstant(DataType.FORECAST_SECTION_WEATHER_DATA_CHECK),
                                forecastSectionsForecastInfo.getUpdateInterval(), forecastSectionsForecastInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 forecastSectionsInfo =
            new UpdateInfoDtoV1(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS,
                                findDataUpdatedInstant(DataType.FORECAST_SECTION_V2_METADATA),
                                findDataUpdatedInstant(DataType.FORECAST_SECTION_V2_METADATA_CHECK),
                                forecastSectionInfo.getUpdateInterval(), forecastSectionInfo.getRecommendedFetchInterval());

        final UpdateInfoDtoV1 forecastSectionsForecastsInfo =
            new UpdateInfoDtoV1( WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + WeatherControllerV1.FORECASTS,
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
            new UpdateInfoDtoV1( WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + WeathercamControllerV1.DATA,
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
