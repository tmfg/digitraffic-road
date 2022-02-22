package fi.livi.digitraffic.tie.helper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum;
import fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum;

import fi.livi.digitraffic.tie.datex2.AbnormalTraffic;
import fi.livi.digitraffic.tie.datex2.Accident;
import fi.livi.digitraffic.tie.datex2.AnimalPresenceObstruction;
import fi.livi.digitraffic.tie.datex2.AuthorityOperation;
import fi.livi.digitraffic.tie.datex2.DisturbanceActivity;
import fi.livi.digitraffic.tie.datex2.EnvironmentalObstruction;
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
        final D2LogicalModel d2LogicalModel = datex2XmlStringToObjectMarshaller.convertToObject(datex2Message);
        final StringJoiner sj = new StringJoiner(". ", "", ".");

        final SituationPublication situationPublication = (SituationPublication) d2LogicalModel.getPayloadPublication();
        if (situationPublication == null) {
            logger.info("method=export situation {} did not have a situation publication payload", situationId);
            return "";
        }

        final List<Situation> situations = situationPublication.getSituations();
        if (situations.size() == 0) {
            logger.info("method=export situation {} did not have any situation records", situationId);
            return "";
        }

        situations.stream()
            .map(Situation::getSituationRecords)
            .flatMap(Collection::stream)
            .flatMap(sr -> accept(sr).stream())
            .forEach(sj::add);
            // .collect(Collectors.toList());

        return sj.toString();
    }

    private String visitAbnormalTraffic(final AbnormalTraffic abnormalTraffic) {
        return null;
    }

    private String visitAccident(final Accident accident) {
        return null;
    }
    private String visitAnimalPresenceObstruction(final AnimalPresenceObstruction animalPresenceObstruction) {
        return null;
    }
    private String visitAuthorityOperation(final AuthorityOperation authorityOperation) {
        return null;
    }
    private String visitDisturbanceActivity(final DisturbanceActivity disturbanceActivity) {
        return null;
    }
    private String visitEnviromentalObstruction(final EnvironmentalObstruction environmentalObstruction) {
        return null;
    }
    private String visitEquipmentOrSystemFault(final EquipmentOrSystemFault equipmentOrSystemFault) {
        final StringBuilder sb = new StringBuilder();

        final EquipmentOrSystemTypeEnum equipmentOrSystemType = equipmentOrSystemFault.getFaultyEquipmentOrSystemType();
        final EquipmentOrSystemFaultTypeEnum faultType = equipmentOrSystemFault.getEquipmentOrSystemFaultType();

        switch (equipmentOrSystemType) {
        case TRAFFIC_LIGHT_SETS:
            sb.append("Traffic light sets");
            break;
        case VARIABLE_MESSAGE_SIGNS:
            sb.append("Variable message signs");
            break;
        case LEVEL_CROSSING:
            sb.append("Level crossing");
            break;
        }

        sb.append(" ");

        switch (faultType) {
        case NOT_WORKING:
            sb.append("not working");
            break;
        case OUT_OF_SERVICE:
            sb.append("out of service");
            break;
        case WORKING_INTERMITTENTLY:
            sb.append("working intermittently");
            break;
        case WORKING_INCORRECTLY:
            sb.append("working incorrectly");
            break;
        }

        return sb.toString();
    }
    private String visitGeneralNetworkManagement(final GeneralNetworkManagement generalNetworkManagement) {
        return null;
    }
    private String visitGeneralObstruction(final GeneralObstruction generalObstruction) {
        return null;
    }
    private String visitInfrastructureDamageObstruction(final InfrastructureDamageObstruction infrastructureDamageObstruction) {
        return null;
    }
    private String visitNonWeatherRelatedRoadConditions(final NonWeatherRelatedRoadConditions nonWeatherRelatedRoadConditions) {
        return null;
    }
    private String visitPoorEnvironmentConditions(final PoorEnvironmentConditions poorEnvironmentConditions) {
        return null;
    }
    private String visitPublicEvent(final PublicEvent publicEvent) {
        return null;
    }
    private String visitReroutingManagement(final ReroutingManagement reroutingManagement) {
        return null;
    }
    private String visitRoadOrCarriagewayOrLaneManagement(final RoadOrCarriagewayOrLaneManagement roadOrCarriagewayOrLaneManagement) {
        final StringBuilder sb = new StringBuilder();
        final RoadOrCarriagewayOrLaneManagementTypeEnum managementType = roadOrCarriagewayOrLaneManagement.getRoadOrCarriagewayOrLaneManagementType();

        switch (managementType) {
        case LANE_CLOSURES:
            sb.append("Lane closures");
            break;
        case NARROW_LANES:
            sb.append("Narrow lanes");
            break;
        case CONTRAFLOW:
            sb.append("Contraflow");
            break;
        case SINGLE_ALTERNATE_LINE_TRAFFIC:
            sb.append("Single alternate line traffic");
            break;
        case INTERMITTENT_SHORT_TERM_CLOSURES:
            sb.append("Intermittent short term closures");
            break;
        case NEW_ROADWORKS_LAYOUT:
            sb.append("New roadworks layout");
            break;
        case LANES_DEVIATED:
            sb.append("Lanes deviated");
            break;
        case ROAD_CLOSED:
            sb.append("Road closed");
            break;
        case CARRIAGEWAY_CLOSURES:
            sb.append("Carriageway closures");
            break;
        }

        // TODO roadOrCarriagewayOrLaneManagementExtension
        // TODO forVehiclesWithCharacteristicsOf

        return sb.toString();
    }
    private String visitSpeedManagement(final SpeedManagement speedManagement) {
        final StringBuilder sb = new StringBuilder();

        final int tempSpeedLimit = Math.round(speedManagement.getTemporarySpeedLimit());
        if (tempSpeedLimit > 0) {
            sb.append(String.format("Temporary speed limit of %d km/h", tempSpeedLimit));
        }

        return sb.toString();
    }
    private String visitTransitInformation(final TransitInformation transitInformation) {
        return null;
    }
    private String visitVehicleObstruction(final VehicleObstruction vehicleObstruction) {
        final StringBuilder sb = new StringBuilder("Vehicle obstruction: ");

        final VehicleObstructionTypeEnum obstructionType = vehicleObstruction.getVehicleObstructionType();

        // TODO mitä jos tulee uusi tyyppi?
        switch (obstructionType) {
        case VEHICLE_ON_WRONG_CARRIAGEWAY:
            sb.append("vehicle on wrong carriageway");
            break;
        case ABNORMAL_LOAD:
            sb.append("abnormal load");
            break;
        case VEHICLE_ON_FIRE:
            sb.append("vehicle on fire");
            break;
        case VEHICLE_STUCK:
            sb.append("vehicle stuck");
            break;
        case BROKEN_DOWN_VEHICLE:
            sb.append("broken down vehicle");
            break;
        case BROKEN_DOWN_HEAVY_LORRY:
            sb.append("broken down heavy lorry");
            break;
        case DAMAGED_VEHICLE:
            sb.append("damaged vehicle");
            break;
        case MILITARY_CONVOY:
            sb.append("military convoy");
            break;
        case SLOW_MOVING_MAINTENANCE_VEHICLE:
            sb.append("slow moving maintenance vehicle");
            break;
        }

        // TODO obstructingVehicle
        // TODO numberOfObstructions

        return sb.toString();
    }
    private String visitWeatherRelatedRoadConditions(final WeatherRelatedRoadConditions weatherRelatedRoadConditions) {
        return null;
    }

    private Optional<String> accept(final SituationRecord situationRecord) {
        if (situationRecord instanceof AbnormalTraffic) {
            return Optional.ofNullable(visitAbnormalTraffic((AbnormalTraffic) situationRecord));
        } else if (situationRecord instanceof Accident) {
            return Optional.ofNullable(visitAccident((Accident) situationRecord));
        } else if (situationRecord instanceof AnimalPresenceObstruction) {
            return Optional.ofNullable(visitAnimalPresenceObstruction((AnimalPresenceObstruction) situationRecord));
        } else if (situationRecord instanceof AuthorityOperation) {
            return Optional.ofNullable(visitAuthorityOperation((AuthorityOperation) situationRecord));
        } else if (situationRecord instanceof DisturbanceActivity) {
            return Optional.ofNullable(visitDisturbanceActivity((DisturbanceActivity) situationRecord));
        } else if (situationRecord instanceof EnvironmentalObstruction) {
            return Optional.ofNullable(visitEnviromentalObstruction((EnvironmentalObstruction) situationRecord));
        } else if (situationRecord instanceof EquipmentOrSystemFault) {
            return Optional.ofNullable(visitEquipmentOrSystemFault((EquipmentOrSystemFault) situationRecord));
        } else if (situationRecord instanceof GeneralNetworkManagement) {
            return Optional.ofNullable(visitGeneralNetworkManagement((GeneralNetworkManagement) situationRecord));
        } else if (situationRecord instanceof GeneralObstruction) {
            return Optional.ofNullable(visitGeneralObstruction((GeneralObstruction) situationRecord));
        } else if (situationRecord instanceof InfrastructureDamageObstruction) {
            return Optional.ofNullable(visitInfrastructureDamageObstruction((InfrastructureDamageObstruction) situationRecord));
        } else if (situationRecord instanceof NonWeatherRelatedRoadConditions) {
            return Optional.ofNullable(visitNonWeatherRelatedRoadConditions((NonWeatherRelatedRoadConditions) situationRecord));
        } else if (situationRecord instanceof PoorEnvironmentConditions) {
            return Optional.ofNullable(visitPoorEnvironmentConditions((PoorEnvironmentConditions) situationRecord));
        } else if (situationRecord instanceof PublicEvent) {
            return Optional.ofNullable(visitPublicEvent((PublicEvent) situationRecord));
        } else if (situationRecord instanceof ReroutingManagement) {
            return Optional.ofNullable(visitReroutingManagement((ReroutingManagement) situationRecord));
        } else if (situationRecord instanceof RoadOrCarriagewayOrLaneManagement) {
            return Optional.ofNullable(visitRoadOrCarriagewayOrLaneManagement((RoadOrCarriagewayOrLaneManagement) situationRecord));
        } else if (situationRecord instanceof SpeedManagement) {
            return Optional.ofNullable(visitSpeedManagement((SpeedManagement) situationRecord));
        } else if (situationRecord instanceof TransitInformation) {
            return Optional.ofNullable(visitTransitInformation((TransitInformation) situationRecord));
        } else if (situationRecord instanceof VehicleObstruction) {
            return Optional.ofNullable(visitVehicleObstruction((VehicleObstruction) situationRecord));
        } else if (situationRecord instanceof WeatherRelatedRoadConditions) {
            return Optional.ofNullable(visitWeatherRelatedRoadConditions((WeatherRelatedRoadConditions) situationRecord));
        }

        // TODO pitäiskö tässä olla tuo situation_id?
        logger.warn("method=accept unknown class {} in SITUATION_ID", situationRecord.getClass().getSimpleName());
        return Optional.empty();
    }
}