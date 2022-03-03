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
import static fi.livi.digitraffic.tie.datex2.TrafficTrendTypeEnum.TRAFFIC_BUILDING_UP;
import static fi.livi.digitraffic.tie.datex2.TrafficTrendTypeEnum.TRAFFIC_EASING;
import static fi.livi.digitraffic.tie.datex2.TrafficTrendTypeEnum.TRAFFIC_STABLE;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum;

import fi.livi.digitraffic.tie.datex2.AbnormalTraffic;
import fi.livi.digitraffic.tie.datex2.Accident;
import fi.livi.digitraffic.tie.datex2.AnimalPresenceObstruction;
import fi.livi.digitraffic.tie.datex2.AuthorityOperation;
import fi.livi.digitraffic.tie.datex2.DisturbanceActivity;
import fi.livi.digitraffic.tie.datex2.EnvironmentalObstruction;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultExtensionType;
import fi.livi.digitraffic.tie.datex2.ExtendedEquipmentOrSystemFaultTypeEnum;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementExtensionType;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFault;
import fi.livi.digitraffic.tie.datex2.GeneralNetworkManagement;
import fi.livi.digitraffic.tie.datex2.GeneralObstruction;
import fi.livi.digitraffic.tie.datex2.InfrastructureDamageObstruction;
import fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.datex2.PoorEnvironmentConditions;
import fi.livi.digitraffic.tie.datex2.PublicEvent;
import fi.livi.digitraffic.tie.datex2.ReroutingManagement;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagement;
import fi.livi.digitraffic.tie.datex2.SpeedManagement;
import fi.livi.digitraffic.tie.datex2.TransitInformation;
import fi.livi.digitraffic.tie.datex2.VehicleObstruction;
import fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditions;

@Component
public class WazeDatex2MessageConverter {
    private static final Logger logger = LoggerFactory.getLogger(WazeDatex2JsonConverter.class);

    private final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;

    @Autowired
    public WazeDatex2MessageConverter(final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller) {
        this.datex2XmlStringToObjectMarshaller = datex2XmlStringToObjectMarshaller;
    }

    public String export(final String situationId, final String datex2Message) {
        final D2LogicalModel d2LogicalModel;

        try {
            d2LogicalModel = datex2XmlStringToObjectMarshaller.convertToObject(datex2Message);
        } catch (UnmarshallingFailureException e) {
            logger.warn("method=export situation {} did not have a proper datex2 message", situationId, e);
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

    private Optional<String> accept(final String situationId, final AbnormalTraffic abnormalTraffic) {
        // TODO handle abnormalTrafficType, trafficFlowCharacteristics and abnormalTrafficExtension
        final Optional<String> result = Optional.ofNullable(abnormalTraffic.getTrafficTrendType())
            .map(x -> {
                switch (x) {
                case TRAFFIC_EASING:
                    return "Traffic easing";
                case TRAFFIC_BUILDING_UP:
                    return "Traffic building up";
                case TRAFFIC_STABLE:
                    return "Traffic stable";
                }
                return null;
            });

        result.ifPresentOrElse(
            (x) -> {},
            () -> logger.warn("method=accept unknown AbnormalTraffic in situation {}", situationId));

        return result;
    }

    private Optional<String> accept(final String situationId, final Accident accident) {
        final Optional<String> result = accident.getAccidentTypes()
            .stream()
            .findFirst()
            .map(x -> {
                switch (x) {
                case ACCIDENT:
                    return "Accident";
                case ACCIDENT_INVOLVING_BUSES:
                    return "Accident involving busses";
                case ACCIDENT_INVOLVING_HAZARDOUS_MATERIALS:
                    return "Accident involving hazardous materials";
                case ACCIDENT_INVOLVING_HEAVY_LORRIES:
                    return "Accident involving heavy lorries";
                case EARLIER_ACCIDENT:
                    return "Earlier accident";
                case MULTIVEHICLE_ACCIDENT:
                    return "Accident involving multiple vehicles";
                case OVERTURNED_HEAVY_LORRY:
                    return "Overturned heavy lorry";
                case REAR_COLLISION:
                    return "Rear collision";
                case VEHICLE_SPUN_AROUND:
                    return "Vehicle spun around";
                }
                return null;
            });

        result.ifPresentOrElse(
            (x) -> {},
            () -> logger.warn("method=accept unknown Accident in situation {}", situationId));

        return result;
    }

    private Optional<String> accept(final String situationId, final AnimalPresenceObstruction animalPresenceObstruction) {
        return Optional.empty();
    }
    private Optional<String> accept(final String situationId, final AuthorityOperation authorityOperation) {
        return Optional.empty();
    }
    private Optional<String> accept(final String situationId, final DisturbanceActivity disturbanceActivity) {
        return Optional.empty();
    }
    private Optional<String> accept(final String situationId, final EnvironmentalObstruction environmentalObstruction) {
        return Optional.empty();
    }

    private Optional<String> accept(final String situationId, final EquipmentOrSystemFault equipmentOrSystemFault) {
        final Optional<String> equipmentOrSystemType = Optional.ofNullable(equipmentOrSystemFault.getFaultyEquipmentOrSystemType())
            .map(x -> {
                switch (x) {
                case TRAFFIC_LIGHT_SETS:
                    return "Traffic light sets";
                case VARIABLE_MESSAGE_SIGNS:
                    return "Variable message signs";
                case LEVEL_CROSSING:
                    return "Level crossing";
                }
                return null;
            });

        final Optional<String> result = Optional.ofNullable(equipmentOrSystemFault.getEquipmentOrSystemFaultExtension())
            .map(EquipmentOrSystemFaultExtensionType::getEquipmentOrSystemFaultType)
            .map(x -> x == ExtendedEquipmentOrSystemFaultTypeEnum.REPAIRED ? "repaired" : null)
            .or(() -> Optional.ofNullable(equipmentOrSystemFault.getEquipmentOrSystemFaultType())
                .map(x -> {
                    switch (x) {
                    case NOT_WORKING:
                        return "not working";
                    case OUT_OF_SERVICE:
                        return "out of service";
                    case WORKING_INTERMITTENTLY:
                        return "working intermittently";
                    case WORKING_INCORRECTLY:
                        return "working incorrectly";
                    }
                    return null;
                }))
            .flatMap(x ->
                equipmentOrSystemType.map(y -> y + " " + x));

        result.ifPresentOrElse(
            (x) -> {},
            () -> logger.warn("method=accept unknown EquipmentOrSystemFault in situation {}", situationId));

        return result;

    }

    private Optional<String> accept(final String situationId, final GeneralNetworkManagement generalNetworkManagement) {
        return Optional.empty();
    }
    private Optional<String> accept(final String situationId, final GeneralObstruction generalObstruction) {
        final Optional<String> result = generalObstruction.getObstructionTypes()
            .stream()
            .findFirst()
            .map(x -> {
                switch (x) {
                case CRANE_OPERATING:
                    return "Crane operating";
                case OBJECT_ON_THE_ROAD:
                    return "Object on the road";
                case OBSTRUCTION_ON_THE_ROAD:
                    return "Obstruction on the road";
                case RESCUE_AND_RECOVERY_WORK:
                    return "Rescue and recovery work";
                case SEVERE_FROST_DAMAGED_ROADWAY:
                    return "Severe frost damaged roadway";
                case SHED_LOAD:
                    return "Shed load";
                case SPILLAGE_OCCURRING_FROM_MOVING_VEHICLE:
                    return "Spillage occurring from moving vehicle";
                case UNPROTECTED_ACCIDENT_AREA:
                    return "Unprotected accident area";
                }
                return null;
            });

        result.ifPresentOrElse(
            (x) -> {},
            () -> logger.warn("method=accept unknown GeneralObstruction in situation {}", situationId));

        return result;
    }
    private Optional<String> accept(final String situationId, final InfrastructureDamageObstruction infrastructureDamageObstruction) {
        return Optional.empty();
    }
    private Optional<String> accept(final String situationId, final NonWeatherRelatedRoadConditions nonWeatherRelatedRoadConditions) {
        return Optional.empty();
    }
    private Optional<String> accept(final String situationId, final PoorEnvironmentConditions poorEnvironmentConditions) {
        return Optional.empty();
    }
    private Optional<String> accept(final String situationId, final PublicEvent publicEvent) {
        return Optional.empty();
    }
    private Optional<String> accept(final String situationId, final ReroutingManagement reroutingManagement) {
        final Optional<String> result = reroutingManagement.getReroutingManagementTypes()
            .stream()
            .findFirst()
            .map(x -> {
                switch (x) {
                case FOLLOW_DIVERSION_SIGNS:
                    return "Follow diversion signs";
                case FOLLOW_LOCAL_DIVERSION:
                    return "Follow local diversion";
                }
                return null;
            });

        result.ifPresentOrElse(
            (x) -> {},
            () -> logger.warn("method=accept unknown ReroutingManagement in situation {}", situationId));

        return result;
    }

    private Optional<String> accept(final String situationId, final RoadOrCarriagewayOrLaneManagement roadOrCarriagewayOrLaneManagement) {
        final Optional<RoadOrCarriagewayOrLaneManagementTypeEnum> optionalManagementType = Optional.ofNullable(roadOrCarriagewayOrLaneManagement.getRoadOrCarriagewayOrLaneManagementType());
        final Optional<RoadOrCarriagewayOrLaneManagementExtensionType> optionalManagementExtension = Optional.ofNullable(roadOrCarriagewayOrLaneManagement.getRoadOrCarriagewayOrLaneManagementExtension());

        final Optional<String> result = optionalManagementExtension
            .map(RoadOrCarriagewayOrLaneManagementExtensionType::getRoadOrCarriagewayOrLaneManagementType)
            .map(x -> {
                switch (x) {
                case ICE_ROAD_OPEN:
                    return "Ice road open";
                case ICE_ROAD_CLOSED:
                    return "Ice road closed";
                }
                return null;
            })
            .or(() -> optionalManagementType
                .map((x) -> {
                    switch (x) {
                    case LANE_CLOSURES:
                        return "Lane closures";
                    case NARROW_LANES:
                        return "Narrow lanes";
                    case CONTRAFLOW:
                        return "Contraflow";
                    case SINGLE_ALTERNATE_LINE_TRAFFIC:
                        return "Single alternate line traffic";
                    case INTERMITTENT_SHORT_TERM_CLOSURES:
                        return "Intermittent short term closures";
                    case NEW_ROADWORKS_LAYOUT:
                        return "New roadworks layout";
                    case LANES_DEVIATED:
                        return "Lanes deviated";
                    case ROAD_CLOSED:
                        return "Road closed";
                    case CARRIAGEWAY_CLOSURES:
                        return "Carriageway closures";
                    }
                    return null;
                }));

        result.ifPresentOrElse(
            (x) -> {},
            () -> logger.warn("method=accept unknown RoadOrCarriagewayOrLaneManegement in situation {}", situationId));

        return result;
    }

    private Optional<String> accept(final String situationId, final SpeedManagement speedManagement) {
        final Optional<String> result = Optional.ofNullable(speedManagement.getTemporarySpeedLimit())
            .map(Math::round)
            .map(x -> x > 0 ? String.format("Temporary speed limit of %d km/h", x) : null);

        result.ifPresentOrElse(
            (x) -> {},
            () -> logger.warn("method=accept unknown SpeedManagement in situation {}", situationId));

        return result;
    }
    private Optional<String> accept(final String situationId, final TransitInformation transitInformation) {
        return Optional.empty();
    }
    private Optional<String> accept(final String situationId, final VehicleObstruction vehicleObstruction) {
        final Optional<String> result = Optional.ofNullable(vehicleObstruction.getVehicleObstructionType())
            .map(x -> {
                switch (x) {
                case VEHICLE_ON_WRONG_CARRIAGEWAY:
                    return "vehicle on wrong carriageway";
                case ABNORMAL_LOAD:
                    return "abnormal load";
                case VEHICLE_ON_FIRE:
                    return "vehicle on fire";
                case VEHICLE_STUCK:
                    return "vehicle stuck";
                case BROKEN_DOWN_VEHICLE:
                    return "broken down vehicle";
                case BROKEN_DOWN_HEAVY_LORRY:
                    return "broken down heavy lorry";
                case DAMAGED_VEHICLE:
                    return "damaged vehicle";
                case MILITARY_CONVOY:
                    return "military convoy";
                case SLOW_MOVING_MAINTENANCE_VEHICLE:
                    return "slow moving maintenance vehicle";
                }

                return null;
            })
            .map(x -> "Vehicle obstruction: " + x);

        result.ifPresentOrElse(
            (x) -> {},
            () -> logger.warn("method=accept unknown VehicleObstruction in situation {}", situationId));

        // skip obstructingVehicle and numberOfObstructions

        return result;
    }
    private Optional<String> accept(final String situationId, final WeatherRelatedRoadConditions weatherRelatedRoadConditions) {
        final Optional<String> result = weatherRelatedRoadConditions.getWeatherRelatedRoadConditionTypes()
            .stream()
            .findFirst()
            .map(x -> {
                switch (x) {
                case BLACK_ICE:
                    return "Black ice";
                case FREEZING_OF_WET_ROADS:
                    return "Freezing of wet roads";
                case FREEZING_PAVEMENTS:
                    return "Freezing pavements";
                case FREEZING_RAIN:
                    return "Freezing rain";
                case FRESH_SNOW:
                    return "Fresh snow";
                case ICE:
                    return "Ice";
                case ICE_BUILD_UP:
                    return "Ice build up";
                case ICY_PATCHES:
                    return "Icy patches";
                case NORMAL_WINTER_CONDITIONS_FOR_PEDESTRIANS:
                    return "Normal winter conditions for pedestrians";
                case PACKED_SNOW:
                    return "Packed snow";
                case SLUSH_ON_ROAD:
                    return "Slush on road";
                case SNOW_ON_PAVEMENT:
                    return "Snow on pavement";
                case SNOW_ON_THE_ROAD:
                    return "Snow on the road";
                case SURFACE_WATER:
                    return "Surface water";
                case WET_AND_ICY_ROAD:
                    return "Wet and icy road";
                case WET_ICY_PAVEMENT:
                    return "Wet icy pavement";
                }
                return null;
            });

        result.ifPresentOrElse(
            (x) -> {},
            () -> logger.warn("method=accept unknown WeatherRelatedConditions in situation {}", situationId));

        return result;
    }

    private Optional<String> accept(final String situationId, final SituationRecord situationRecord) {
        if (situationRecord instanceof AbnormalTraffic) {
            return accept(situationId, (AbnormalTraffic) situationRecord);
        } else if (situationRecord instanceof Accident) {
            return accept(situationId, (Accident) situationRecord);
        } else if (situationRecord instanceof AnimalPresenceObstruction) {
            return accept(situationId, (AnimalPresenceObstruction) situationRecord);
        } else if (situationRecord instanceof AuthorityOperation) {
            return accept(situationId, (AuthorityOperation) situationRecord);
        } else if (situationRecord instanceof DisturbanceActivity) {
            return accept(situationId, (DisturbanceActivity) situationRecord);
        } else if (situationRecord instanceof EnvironmentalObstruction) {
            return accept(situationId, (EnvironmentalObstruction) situationRecord);
        } else if (situationRecord instanceof EquipmentOrSystemFault) {
            return accept(situationId, (EquipmentOrSystemFault) situationRecord);
        } else if (situationRecord instanceof GeneralNetworkManagement) {
            return accept(situationId, (GeneralNetworkManagement) situationRecord);
        } else if (situationRecord instanceof GeneralObstruction) {
            return accept(situationId, (GeneralObstruction) situationRecord);
        } else if (situationRecord instanceof InfrastructureDamageObstruction) {
            return accept(situationId, (InfrastructureDamageObstruction) situationRecord);
        } else if (situationRecord instanceof NonWeatherRelatedRoadConditions) {
            return accept(situationId, (NonWeatherRelatedRoadConditions) situationRecord);
        } else if (situationRecord instanceof PoorEnvironmentConditions) {
            return accept(situationId, (PoorEnvironmentConditions) situationRecord);
        } else if (situationRecord instanceof PublicEvent) {
            return accept(situationId, (PublicEvent) situationRecord);
        } else if (situationRecord instanceof ReroutingManagement) {
            return accept(situationId, (ReroutingManagement) situationRecord);
        } else if (situationRecord instanceof RoadOrCarriagewayOrLaneManagement) {
            return accept(situationId, (RoadOrCarriagewayOrLaneManagement) situationRecord);
        } else if (situationRecord instanceof SpeedManagement) {
            return accept(situationId, (SpeedManagement) situationRecord);
        } else if (situationRecord instanceof TransitInformation) {
            return accept(situationId, (TransitInformation) situationRecord);
        } else if (situationRecord instanceof VehicleObstruction) {
            return accept(situationId, (VehicleObstruction) situationRecord);
        } else if (situationRecord instanceof WeatherRelatedRoadConditions) {
            return accept(situationId, (WeatherRelatedRoadConditions) situationRecord);
        }

        logger.error("method=accept unknown class {} in {}", situationRecord.getClass().getSimpleName(), situationId);
        return Optional.empty();
    }
}