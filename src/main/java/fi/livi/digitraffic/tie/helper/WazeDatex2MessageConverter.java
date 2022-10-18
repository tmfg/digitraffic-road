package fi.livi.digitraffic.tie.helper;

import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.ACCIDENT_INVOLVING_BUSES;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.ACCIDENT_INVOLVING_HAZARDOUS_MATERIALS;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.ACCIDENT_INVOLVING_HEAVY_LORRIES;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.EARLIER_ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.MULTIVEHICLE_ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.OVERTURNED_HEAVY_LORRY;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.REAR_COLLISION;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.VEHICLE_SPUN_AROUND;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum.NOT_WORKING;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum.OUT_OF_SERVICE;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum.WORKING_INCORRECTLY;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum.WORKING_INTERMITTENTLY;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.LEVEL_CROSSING;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.TRAFFIC_LIGHT_SETS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.VARIABLE_MESSAGE_SIGNS;
import static fi.livi.digitraffic.tie.datex2.ExtendedRoadOrCarriagewayOrLaneManagementTypeEnum.ICE_ROAD_CLOSED;
import static fi.livi.digitraffic.tie.datex2.ExtendedRoadOrCarriagewayOrLaneManagementTypeEnum.ICE_ROAD_OPEN;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.CRANE_OPERATING;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.OBJECT_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.OBSTRUCTION_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.RESCUE_AND_RECOVERY_WORK;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.SEVERE_FROST_DAMAGED_ROADWAY;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.SHED_LOAD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.SPILLAGE_OCCURRING_FROM_MOVING_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.UNPROTECTED_ACCIDENT_AREA;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.FOLLOW_DIVERSION_SIGNS;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.FOLLOW_LOCAL_DIVERSION;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.CARRIAGEWAY_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.CONTRAFLOW;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.INTERMITTENT_SHORT_TERM_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.LANES_DEVIATED;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.LANE_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.NARROW_LANES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.NEW_ROADWORKS_LAYOUT;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.ROAD_CLOSED;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.SINGLE_ALTERNATE_LINE_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.TrafficTrendTypeEnum.TRAFFIC_BUILDING_UP;
import static fi.livi.digitraffic.tie.datex2.TrafficTrendTypeEnum.TRAFFIC_EASING;
import static fi.livi.digitraffic.tie.datex2.TrafficTrendTypeEnum.TRAFFIC_STABLE;
import static fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum.ABNORMAL_LOAD;
import static fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum.BROKEN_DOWN_HEAVY_LORRY;
import static fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum.BROKEN_DOWN_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum.DAMAGED_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum.MILITARY_CONVOY;
import static fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum.SLOW_MOVING_MAINTENANCE_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum.VEHICLE_ON_FIRE;
import static fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum.VEHICLE_ON_WRONG_CARRIAGEWAY;
import static fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum.VEHICLE_STUCK;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.BLACK_ICE;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.FREEZING_OF_WET_ROADS;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.FREEZING_PAVEMENTS;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.FREEZING_RAIN;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.FRESH_SNOW;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.ICE;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.ICE_BUILD_UP;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.ICY_PATCHES;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.NORMAL_WINTER_CONDITIONS_FOR_PEDESTRIANS;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.PACKED_SNOW;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.SLUSH_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.SNOW_ON_PAVEMENT;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.SNOW_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.SURFACE_WATER;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.WET_AND_ICY_ROAD;
import static fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum.WET_ICY_PAVEMENT;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.datex2.AbnormalTraffic;
import fi.livi.digitraffic.tie.datex2.Accident;
import fi.livi.digitraffic.tie.datex2.AccidentTypeEnum;
import fi.livi.digitraffic.tie.datex2.AnimalPresenceObstruction;
import fi.livi.digitraffic.tie.datex2.AuthorityOperation;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.DisturbanceActivity;
import fi.livi.digitraffic.tie.datex2.EnvironmentalObstruction;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFault;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultExtensionType;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum;
import fi.livi.digitraffic.tie.datex2.ExtendedEquipmentOrSystemFaultTypeEnum;
import fi.livi.digitraffic.tie.datex2.ExtendedRoadOrCarriagewayOrLaneManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.GeneralNetworkManagement;
import fi.livi.digitraffic.tie.datex2.GeneralObstruction;
import fi.livi.digitraffic.tie.datex2.InfrastructureDamageObstruction;
import fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.PoorEnvironmentConditions;
import fi.livi.digitraffic.tie.datex2.PublicEvent;
import fi.livi.digitraffic.tie.datex2.ReroutingManagement;
import fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagement;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementExtensionType;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.datex2.SpeedManagement;
import fi.livi.digitraffic.tie.datex2.TrafficTrendTypeEnum;
import fi.livi.digitraffic.tie.datex2.TransitInformation;
import fi.livi.digitraffic.tie.datex2.VehicleObstruction;
import fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum;
import fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;

@Component
public class WazeDatex2MessageConverter {
    private static final Logger logger = LoggerFactory.getLogger(WazeDatex2JsonConverter.class);

    private final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;

    private final Map<TrafficTrendTypeEnum, String> abnormalTrafficTypeMap = new HashMap<>();
    private final Map<AccidentTypeEnum, String> accidentTypeMap = new HashMap<>();
    private final Map<EquipmentOrSystemTypeEnum, String> equipmentOrSystemTypeMap = new HashMap<>();
    private final Map<EquipmentOrSystemFaultTypeEnum, String> equipmentOrSystemFaultTypeMap = new HashMap<>();
    private final Map<ObstructionTypeEnum, String> obstructionTypeMap = new HashMap<>();
    private final Map<ReroutingManagementTypeEnum, String> reroutingManagementTypeMap = new HashMap<>();
    private final Map<ExtendedRoadOrCarriagewayOrLaneManagementTypeEnum, String> extendedRoadOrCarriagewayOrLaneManagementTypeMap = new HashMap<>();
    private final Map<RoadOrCarriagewayOrLaneManagementTypeEnum, String> roadOrCarriagewayOrLaneManagementTypeMap = new HashMap<>();
    private final Map<VehicleObstructionTypeEnum, String> vehicleObstructionTypeMap = new HashMap<>();
    private final Map<WeatherRelatedRoadConditionTypeEnum, String> weatherRelatedRoadConditionTypeMap = new HashMap<>();

    @Autowired
    public WazeDatex2MessageConverter(final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller) {
        this.datex2XmlStringToObjectMarshaller = datex2XmlStringToObjectMarshaller;

        constructMaps();
    }

    private void constructMaps() {
        abnormalTrafficTypeMap.put(TRAFFIC_EASING, "Traffic easing");
        abnormalTrafficTypeMap.put(TRAFFIC_BUILDING_UP, "Traffic building up");
        abnormalTrafficTypeMap.put(TRAFFIC_STABLE, "Traffic stable");

        accidentTypeMap.put(ACCIDENT, "Accident");
        accidentTypeMap.put(ACCIDENT_INVOLVING_BUSES, "Accident involving busses");
        accidentTypeMap.put(ACCIDENT_INVOLVING_HAZARDOUS_MATERIALS, "Accident involving hazardous materials");
        accidentTypeMap.put(ACCIDENT_INVOLVING_HEAVY_LORRIES, "Accident involving heavy lorries");
        accidentTypeMap.put(EARLIER_ACCIDENT, "Earlier accident");
        accidentTypeMap.put(MULTIVEHICLE_ACCIDENT, "Accident involving multiple vehicles");
        accidentTypeMap.put(OVERTURNED_HEAVY_LORRY, "Overturned heavy lorry");
        accidentTypeMap.put(REAR_COLLISION, "Rear collision");
        accidentTypeMap.put(VEHICLE_SPUN_AROUND, "Vehicle spun around");

        equipmentOrSystemTypeMap.put(TRAFFIC_LIGHT_SETS, "Traffic light sets");
        equipmentOrSystemTypeMap.put(VARIABLE_MESSAGE_SIGNS, "Variable message signs");
        equipmentOrSystemTypeMap.put(LEVEL_CROSSING, "Level crossing");

        equipmentOrSystemFaultTypeMap.put(NOT_WORKING, "not working");
        equipmentOrSystemFaultTypeMap.put(OUT_OF_SERVICE, "out of service");
        equipmentOrSystemFaultTypeMap.put(WORKING_INTERMITTENTLY, "working intermittently");
        equipmentOrSystemFaultTypeMap.put(WORKING_INCORRECTLY, "working incorrectly");

        obstructionTypeMap.put(CRANE_OPERATING, "Crane operating");
        obstructionTypeMap.put(OBJECT_ON_THE_ROAD, "Object on the road");
        obstructionTypeMap.put(OBSTRUCTION_ON_THE_ROAD, "Obstruction on the road");
        obstructionTypeMap.put(RESCUE_AND_RECOVERY_WORK, "Rescue and recovery work");
        obstructionTypeMap.put(SEVERE_FROST_DAMAGED_ROADWAY, "Severe frost damaged roadway");
        obstructionTypeMap.put(SHED_LOAD, "Shed load");
        obstructionTypeMap.put(SPILLAGE_OCCURRING_FROM_MOVING_VEHICLE, "Spillage occurring from moving vehicle");
        obstructionTypeMap.put(UNPROTECTED_ACCIDENT_AREA, "Unprotected accident area");

        reroutingManagementTypeMap.put(FOLLOW_DIVERSION_SIGNS, "Follow diversion signs");
        reroutingManagementTypeMap.put(FOLLOW_LOCAL_DIVERSION, "Follow local diversion");

        extendedRoadOrCarriagewayOrLaneManagementTypeMap.put(ICE_ROAD_OPEN, "Ice road open");
        extendedRoadOrCarriagewayOrLaneManagementTypeMap.put(ICE_ROAD_CLOSED, "Ice road closed");

        roadOrCarriagewayOrLaneManagementTypeMap.put(LANE_CLOSURES, "Lane closures");
        roadOrCarriagewayOrLaneManagementTypeMap.put(NARROW_LANES, "Narrow lanes");
        roadOrCarriagewayOrLaneManagementTypeMap.put(CONTRAFLOW, "Contraflow");
        roadOrCarriagewayOrLaneManagementTypeMap.put(SINGLE_ALTERNATE_LINE_TRAFFIC, "Single alternate line traffic");
        roadOrCarriagewayOrLaneManagementTypeMap.put(INTERMITTENT_SHORT_TERM_CLOSURES, "Intermittent short term closures");
        roadOrCarriagewayOrLaneManagementTypeMap.put(NEW_ROADWORKS_LAYOUT, "New roadworks layout");
        roadOrCarriagewayOrLaneManagementTypeMap.put(LANES_DEVIATED, "Lanes deviated");
        roadOrCarriagewayOrLaneManagementTypeMap.put(ROAD_CLOSED, "Road closed");
        roadOrCarriagewayOrLaneManagementTypeMap.put(CARRIAGEWAY_CLOSURES, "Carriageway closures");

        vehicleObstructionTypeMap.put(VEHICLE_ON_WRONG_CARRIAGEWAY, "vehicle on wrong carriageway");
        vehicleObstructionTypeMap.put(ABNORMAL_LOAD, "abnormal load");
        vehicleObstructionTypeMap.put(VEHICLE_ON_FIRE, "vehicle on fire");
        vehicleObstructionTypeMap.put(VEHICLE_STUCK, "vehicle stuck");
        vehicleObstructionTypeMap.put(BROKEN_DOWN_VEHICLE, "broken down vehicle");
        vehicleObstructionTypeMap.put(BROKEN_DOWN_HEAVY_LORRY, "broken down heavy lorry");
        vehicleObstructionTypeMap.put(DAMAGED_VEHICLE, "damaged vehicle");
        vehicleObstructionTypeMap.put(MILITARY_CONVOY, "military convoy");
        vehicleObstructionTypeMap.put(SLOW_MOVING_MAINTENANCE_VEHICLE, "slow moving maintenance vehicle");

        weatherRelatedRoadConditionTypeMap.put(BLACK_ICE, "Black ice");
        weatherRelatedRoadConditionTypeMap.put(FREEZING_OF_WET_ROADS, "Freezing of wet roads");
        weatherRelatedRoadConditionTypeMap.put(FREEZING_PAVEMENTS, "Freezing pavements");
        weatherRelatedRoadConditionTypeMap.put(FREEZING_RAIN, "Freezing rain");
        weatherRelatedRoadConditionTypeMap.put(FRESH_SNOW, "Fresh snow");
        weatherRelatedRoadConditionTypeMap.put(ICE, "Ice");
        weatherRelatedRoadConditionTypeMap.put(ICE_BUILD_UP, "Ice build up");
        weatherRelatedRoadConditionTypeMap.put(ICY_PATCHES, "Icy patches");
        weatherRelatedRoadConditionTypeMap.put(NORMAL_WINTER_CONDITIONS_FOR_PEDESTRIANS, "Normal winter conditions for pedestrians");
        weatherRelatedRoadConditionTypeMap.put(PACKED_SNOW, "Packed snow");
        weatherRelatedRoadConditionTypeMap.put(SLUSH_ON_ROAD, "Slush on road");
        weatherRelatedRoadConditionTypeMap.put(SNOW_ON_PAVEMENT, "Snow on pavement");
        weatherRelatedRoadConditionTypeMap.put(SNOW_ON_THE_ROAD, "Snow on the road");
        weatherRelatedRoadConditionTypeMap.put(SURFACE_WATER, "Surface water");
        weatherRelatedRoadConditionTypeMap.put(WET_AND_ICY_ROAD, "Wet and icy road");
        weatherRelatedRoadConditionTypeMap.put(WET_ICY_PAVEMENT, "Wet icy pavement");
    }

    public String export(final String situationId, final String datex2Message) {
        final D2LogicalModel d2LogicalModel;

        try {
            d2LogicalModel = datex2XmlStringToObjectMarshaller.convertToObject(datex2Message);
        } catch (UnmarshallingFailureException e) {
            logger.warn("method=export situation {} did not have a proper datex2 message, error: {}", situationId, e.getMessage());
            return "";
        }

        final SituationPublication situationPublication = (SituationPublication) d2LogicalModel.getPayloadPublication();
        if (situationPublication == null) {
            logger.info("method=export situation {} did not have a situation publication payload", situationId);
            return "";
        }

        final List<Situation> situations = situationPublication.getSituations();
        if (situations.isEmpty()) {
            logger.info("method=export situation {} did not have any situation records", situationId);
            return "";
        }

        return situations.stream()
            .map(Situation::getSituationRecords)
            .flatMap(Collection::stream)
            .flatMap(sr -> accept(situationId, sr).stream())
            .distinct()
            .collect(Collectors.joining(". ", "", "."));
    }

    private Optional<String> accept(final AbnormalTraffic abnormalTraffic) {
        // TODO handle abnormalTrafficType, trafficFlowCharacteristics and abnormalTrafficExtension

        return Optional.ofNullable(abnormalTraffic.getTrafficTrendType())
            .map(x -> abnormalTrafficTypeMap.getOrDefault(x, null));
    }

    private Optional<String> accept(final Accident accident) {

        return accident.getAccidentTypes()
            .stream()
            .findFirst()
            .map(x -> accidentTypeMap.getOrDefault(x, null));
    }

    private Optional<String> accept(final AnimalPresenceObstruction animalPresenceObstruction) {
        return Optional.empty();
    }
    private Optional<String> accept(final AuthorityOperation authorityOperation) {
        return Optional.empty();
    }
    private Optional<String> accept(final DisturbanceActivity disturbanceActivity) {
        return Optional.empty();
    }
    private Optional<String> accept(final EnvironmentalObstruction environmentalObstruction) {
        return Optional.empty();
    }

    private Optional<String> accept(final EquipmentOrSystemFault equipmentOrSystemFault) {
        final Optional<String> equipmentOrSystemType = Optional.ofNullable(equipmentOrSystemFault.getFaultyEquipmentOrSystemType())
            .map(x -> equipmentOrSystemTypeMap.getOrDefault(x, null));

        return Optional.ofNullable(equipmentOrSystemFault.getEquipmentOrSystemFaultExtension())
            .map(EquipmentOrSystemFaultExtensionType::getEquipmentOrSystemFaultType)
            .map(x -> x == ExtendedEquipmentOrSystemFaultTypeEnum.REPAIRED ? "repaired" : null)
            .or(() -> Optional.ofNullable(equipmentOrSystemFault.getEquipmentOrSystemFaultType())
                .map(x -> equipmentOrSystemFaultTypeMap.getOrDefault(x, null)))
            .flatMap(x ->
                equipmentOrSystemType.map(y -> y + " " + x));

    }

    private Optional<String> accept(final GeneralNetworkManagement generalNetworkManagement) {
        return Optional.empty();
    }
    private Optional<String> accept(final GeneralObstruction generalObstruction) {

        return generalObstruction.getObstructionTypes()
            .stream()
            .findFirst()
            .map(x -> obstructionTypeMap.getOrDefault(x, null));
    }
    private Optional<String> accept(final InfrastructureDamageObstruction infrastructureDamageObstruction) {
        return Optional.empty();
    }
    private Optional<String> accept(final NonWeatherRelatedRoadConditions nonWeatherRelatedRoadConditions) {
        return Optional.empty();
    }
    private Optional<String> accept(final PoorEnvironmentConditions poorEnvironmentConditions) {
        return Optional.empty();
    }
    private Optional<String> accept(final PublicEvent publicEvent) {
        return Optional.empty();
    }
    private Optional<String> accept(final ReroutingManagement reroutingManagement) {

        return reroutingManagement.getReroutingManagementTypes()
            .stream()
            .findFirst()
            .map(x -> reroutingManagementTypeMap.getOrDefault(x, null));
    }

    private Optional<String> accept(final RoadOrCarriagewayOrLaneManagement roadOrCarriagewayOrLaneManagement) {
        final Optional<RoadOrCarriagewayOrLaneManagementTypeEnum> optionalManagementType = Optional.ofNullable(roadOrCarriagewayOrLaneManagement.getRoadOrCarriagewayOrLaneManagementType());
        final Optional<RoadOrCarriagewayOrLaneManagementExtensionType> optionalManagementExtension = Optional.ofNullable(roadOrCarriagewayOrLaneManagement.getRoadOrCarriagewayOrLaneManagementExtension());

        return optionalManagementExtension
            .map(RoadOrCarriagewayOrLaneManagementExtensionType::getRoadOrCarriagewayOrLaneManagementType)
            .map(x -> extendedRoadOrCarriagewayOrLaneManagementTypeMap.getOrDefault(x, null))
            .or(() -> optionalManagementType
                .map((x) -> roadOrCarriagewayOrLaneManagementTypeMap.getOrDefault(x, null)));
    }

    private Optional<String> accept(final SpeedManagement speedManagement) {

        return Optional.ofNullable(speedManagement.getTemporarySpeedLimit())
            .map(Math::round)
            .map(x -> x > 0 ? String.format("Temporary speed limit of %d km/h", x) : null);
    }
    private Optional<String> accept(final TransitInformation transitInformation) {
        return Optional.ofNullable(transitInformation)
            .map((transitInformation1) -> {
                final String type = transitInformation1.getTransitServiceType().toString().toLowerCase().replace("_", " ");
                final String info = transitInformation1.getTransitServiceInformation().toString().toLowerCase().replace("_", " ");
                return String.format("%s: %s", type, info);
            })
            .map((s -> {
                final String firstLetter = s.substring(0, 1);
                final String rest = s.substring(1);
                return firstLetter.toUpperCase() + rest;
            }));
    }
    private Optional<String> accept(final VehicleObstruction vehicleObstruction) {

        // skip obstructingVehicle and numberOfObstructions
        return Optional.ofNullable(vehicleObstruction.getVehicleObstructionType())
            .map(x -> vehicleObstructionTypeMap.getOrDefault(x, null))
            .map(x -> "Vehicle obstruction: " + x);
    }
    private Optional<String> accept(final WeatherRelatedRoadConditions weatherRelatedRoadConditions) {
        return weatherRelatedRoadConditions.getWeatherRelatedRoadConditionTypes()
            .stream()
            .findFirst()
            .map(x -> weatherRelatedRoadConditionTypeMap.getOrDefault(x, null));
    }

    private Optional<String> accept(final String situationId, final SituationRecord situationRecord) {
        final Optional<String> result;
        final String situationRecordType;

        if (situationRecord instanceof AbnormalTraffic) {
            result = accept((AbnormalTraffic) situationRecord);
            situationRecordType = "AbnormalTraffic";
        } else if (situationRecord instanceof Accident) {
            result = accept((Accident) situationRecord);
            situationRecordType = "Accident";
        } else if (situationRecord instanceof AnimalPresenceObstruction) {
            result = accept((AnimalPresenceObstruction) situationRecord);
            situationRecordType = "AnimalPresenceObstruction";
        } else if (situationRecord instanceof AuthorityOperation) {
            result = accept((AuthorityOperation) situationRecord);
            situationRecordType = "AuthorityOperation";
        } else if (situationRecord instanceof DisturbanceActivity) {
            result = accept((DisturbanceActivity) situationRecord);
            situationRecordType = "DisturbanceActivity";
        } else if (situationRecord instanceof EnvironmentalObstruction) {
            result = accept((EnvironmentalObstruction) situationRecord);
            situationRecordType = "EnvironmentalObstruction";
        } else if (situationRecord instanceof EquipmentOrSystemFault) {
            result = accept((EquipmentOrSystemFault) situationRecord);
            situationRecordType = "EquipmentOrSystemFault";
        } else if (situationRecord instanceof GeneralNetworkManagement) {
            result = accept((GeneralNetworkManagement) situationRecord);
            situationRecordType = "GeneralNetworkManagement";
        } else if (situationRecord instanceof GeneralObstruction) {
            result = accept((GeneralObstruction) situationRecord);
            situationRecordType = "GeneralObstruction";
        } else if (situationRecord instanceof InfrastructureDamageObstruction) {
            result = accept((InfrastructureDamageObstruction) situationRecord);
            situationRecordType = "InfrastructureDamageObstruction";
        } else if (situationRecord instanceof NonWeatherRelatedRoadConditions) {
            result = accept((NonWeatherRelatedRoadConditions) situationRecord);
            situationRecordType = "NonWeatherRelatedRoadConditions";
        } else if (situationRecord instanceof PoorEnvironmentConditions) {
            result = accept((PoorEnvironmentConditions) situationRecord);
            situationRecordType = "PoorEnvironmentConditions";
        } else if (situationRecord instanceof PublicEvent) {
            result = accept((PublicEvent) situationRecord);
            situationRecordType = "PublicEvent";
        } else if (situationRecord instanceof ReroutingManagement) {
            result = accept((ReroutingManagement) situationRecord);
            situationRecordType = "ReroutingManagement";
        } else if (situationRecord instanceof RoadOrCarriagewayOrLaneManagement) {
            result = accept((RoadOrCarriagewayOrLaneManagement) situationRecord);
            situationRecordType = "RoadOrCarriagewayOrLaneManagement";
        } else if (situationRecord instanceof SpeedManagement) {
            result = accept((SpeedManagement) situationRecord);
            situationRecordType = "SpeedManagement";
        } else if (situationRecord instanceof TransitInformation) {
            result = accept((TransitInformation) situationRecord);
            situationRecordType = "TransitInformation";
        } else if (situationRecord instanceof VehicleObstruction) {
            result = accept((VehicleObstruction) situationRecord);
            situationRecordType = "VehicleObstruction";
        } else if (situationRecord instanceof WeatherRelatedRoadConditions) {
            result = accept((WeatherRelatedRoadConditions) situationRecord);
            situationRecordType = "WeatherRelatedRoadConditions";
        } else {
            logger.error("method=accept unknown class {} in {}", situationRecord.getClass().getSimpleName(), situationId);
            return Optional.empty();
        }

        if (result.isEmpty()) {
            logger.error("method=accept unknown {} record in situation {}", situationRecordType, situationId);
        }

        return result;
    }
}