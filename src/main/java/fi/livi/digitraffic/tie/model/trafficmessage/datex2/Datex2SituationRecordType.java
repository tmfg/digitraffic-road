package fi.livi.digitraffic.tie.model.trafficmessage.datex2;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.AbnormalTraffic;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.Accident;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.AnimalPresenceObstruction;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.AuthorityOperation;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.CarParks;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.Conditions;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.ConstructionWorks;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.DisturbanceActivity;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.EnvironmentalObstruction;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.EquipmentOrSystemFault;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.GeneralInstructionOrMessageToRoadUsers;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.GeneralNetworkManagement;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.GeneralObstruction;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.GenericSituationRecord;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.InfrastructureDamageObstruction;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.MaintenanceWorks;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.NonWeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.PoorEnvironmentConditions;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.PublicEvent;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.ReroutingManagement;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.RoadConditions;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.RoadOperatorServiceDisruption;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.RoadOrCarriagewayOrLaneManagement;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.RoadsideAssistance;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.RoadsideServiceDisruption;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SignSetting;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationRecord;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SpeedManagement;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.TransitInformation;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.VehicleObstruction;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.WeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.WinterDrivingManagement;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SituationRecordType", description = "Datex2 situation record type record")
public enum Datex2SituationRecordType {

    // GenericSituationRecord
    GENERIC_SITUATION_RECORD(GenericSituationRecord.class),
    // NonRoadEventInformation
    NON_ROADEVENT_INFORMATION_CAR_PARKS(CarParks.class),
    NON_ROADEVENT_INFORMATION_ROAD_OPERATOR_SERVICE_DISRUPTION(RoadOperatorServiceDisruption.class),
    NON_ROADEVENT_INFORMATION_ROADSIDE_SERVICE_DISRUPTION(RoadsideServiceDisruption.class),
    NON_ROADEVENT_INFORMATION_TRANSIT_INFORMATION(TransitInformation.class),
    // TrafficElement/
    TRAFFIC_ELEMENT_ABNORMAL_TRAFFIC(AbnormalTraffic.class),
    TRAFFIC_ELEMENT_ACCIDENT(Accident.class),
    TRAFFIC_ELEMENT_EQUIPMENT_OR_SYSTEMFAULT(EquipmentOrSystemFault.class),
    TRAFFIC_ELEMENT_ACTIVITY_DISTURBANCE_ACTIVITY(DisturbanceActivity.class),
    TRAFFIC_ELEMENT_ACTIVITY_AUTHORITY_OPERATION(AuthorityOperation.class),
    TRAFFIC_ELEMENT_ACTIVITY_PUBLIC_EVENT(PublicEvent.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_ANIMAL_PRESENCE_OBSTRUCTION(AnimalPresenceObstruction.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_ENVIRONMENTAL_OBSTRUCTION(EnvironmentalObstruction.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_GENERAL_OBSTRUCTION(GeneralObstruction.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_INFRASTRUCTURE_DAMAGEOBSTRUCTION(InfrastructureDamageObstruction.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_VEHICLE_OBSTRUCTION(VehicleObstruction.class),
    TRAFFIC_ELEMENT_CONDITIONS(Conditions.class),
    TRAFFIC_ELEMENT_CONDITIONS_POOR_ENVIRONMENT_CONDITIONS(PoorEnvironmentConditions.class),
    TRAFFIC_ELEMENT_CONDITIONS_ROADCONDITIONS(RoadConditions.class),
    TRAFFIC_ELEMENT_CONDITIONS_NON_WEATHER_RELATED_ROAD_CONDITIONS(NonWeatherRelatedRoadConditions.class), // TRAFFIC_ELEMENT_CONDITIONS_NONWEATHERRELATEDROADCONDITIONS
    TRAFFIC_ELEMENT_CONDITIONS_WEATHER_RELATED_ROAD_CONDITIONS(WeatherRelatedRoadConditions.class), // TRAFFIC_ELEMENT_CONDITIONS_WEATHERRELATEDROADCONDITIONS
    // OperatorAction
    OPERATOR_ACTION_ROADSIDE_ASSISTANCE(RoadsideAssistance.class),
    OPERATOR_ACTION_ROADWORKS_CONSTRUCTION_WORKS(ConstructionWorks.class),
    OPERATOR_ACTION_ROADWORKS_MAINTENANCE_WORKS(MaintenanceWorks.class),
    OPERATOR_ACTION_SIGN_SETTING(SignSetting.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_GENERAL_INSTRUCTION_OR_MESSAGE_TO_ROAD_USERS(GeneralInstructionOrMessageToRoadUsers.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_GENERAL_NETWORK_MANAGEMENT(GeneralNetworkManagement.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_REROUTING_MANAGEMENT(ReroutingManagement.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_ROAD_OR_CARRIAGEWAY_OR_LANE_MANAGEMENT(RoadOrCarriagewayOrLaneManagement.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_SPEED_MANAGEMENT(SpeedManagement.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_WINTER_DRIVING_MANAGEMENT(WinterDrivingManagement.class),
    ;

    private final Class<? extends SituationRecord> situationRecordClass;

    private static final Map<Class<? extends SituationRecord>, Datex2SituationRecordType> lookup = new HashMap<>();

    static{
        for (final Datex2SituationRecordType type : EnumSet.allOf(Datex2SituationRecordType.class)) {
            lookup.put(type.getSituationRecordClass(), type);
        }
    }

    Datex2SituationRecordType(final Class<? extends SituationRecord> situationRecordClass) {
        this.situationRecordClass = situationRecordClass;
    }

    public Class<? extends SituationRecord> getSituationRecordClass() {
        return situationRecordClass;
    }

    public static Datex2SituationRecordType fromRecord(final Class<? extends SituationRecord> situationRecordClass) {
        final Datex2SituationRecordType type = lookup.get(situationRecordClass);
        if (type == null) {
            throw new IllegalArgumentException("Type " + situationRecordClass.getName() + " not suported!");
        }
        return type;
    }

}
