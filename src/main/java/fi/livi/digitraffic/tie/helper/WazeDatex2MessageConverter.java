package fi.livi.digitraffic.tie.helper;

import static fi.livi.digitraffic.tie.datex2.v3_7.AbnormalTrafficTypeEnum.HEAVY_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.v3_7.AbnormalTrafficTypeEnum.QUEUING_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.v3_7.AbnormalTrafficTypeEnum.SLOW_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.v3_7.AbnormalTrafficTypeEnum.STATIONARY_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.v3_7.AbnormalTrafficTypeEnum.UNSPECIFIED_ABNORMAL_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.v3_7.AccidentTypeEnum.ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.AccidentTypeEnum.ACCIDENT_INVOLVING_MASS_TRANSIT_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.v3_7.AccidentTypeEnum.ACCIDENT_INVOLVING_HAZARDOUS_MATERIALS;
import static fi.livi.digitraffic.tie.datex2.v3_7.AccidentTypeEnum.ACCIDENT_INVOLVING_HEAVY_LORRIES;
import static fi.livi.digitraffic.tie.datex2.v3_7.AccidentTypeEnum.SECONDARY_ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.AccidentTypeEnum.MULTIPLE_VEHICLE_ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.AnimalPresenceTypeEnum.ANIMALS_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.AnimalPresenceTypeEnum.HERD_OF_ANIMALS_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.AnimalPresenceTypeEnum.LARGE_ANIMALS_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.DisturbanceActivityTypeEnum.CROWD;
import static fi.livi.digitraffic.tie.datex2.v3_7.DisturbanceActivityTypeEnum.DEMONSTRATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.DisturbanceActivityTypeEnum.MARCH;
import static fi.livi.digitraffic.tie.datex2.v3_7.DisturbanceActivityTypeEnum.PUBLIC_DISTURBANCE;
import static fi.livi.digitraffic.tie.datex2.v3_7.DisturbanceActivityTypeEnum.RIOT;
import static fi.livi.digitraffic.tie.datex2.v3_7.DisturbanceActivityTypeEnum.STRIKE;
import static fi.livi.digitraffic.tie.datex2.v3_7.DisturbanceActivityTypeEnum.UNSPECIFIED_ALERT;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.AVALANCHES;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.EARTHQUAKE_DAMAGE;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.FALLEN_TREES;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.FALLING_ICE;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.FALLING_LIGHT_ICE_OR_SNOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.FLASH_FLOODS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.FLOODING;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.FOREST_FIRE;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.GRASS_FIRE;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.LANDSLIPS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.MUD_SLIDE;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.ROCKFALLS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.SERIOUS_FIRE;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.SEWER_OVERFLOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.SMOKE_OR_FUMES;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.STORM_DAMAGE;
import static fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum.SUBSIDENCE;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemFaultTypeEnum.NOT_WORKING;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemFaultTypeEnum.OUT_OF_SERVICE;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemFaultTypeEnum.WORKING_INCORRECTLY;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemFaultTypeEnum.WORKING_INTERMITTENTLY;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.ANPR_CAMERAS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.AUTOMATED_TOLL_SYSTEM;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.CCTV_CAMERAS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.EMERGENCY_ROADSIDE_TELEPHONES;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.GALLERY_LIGHTS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.LANE_CONTROL_SIGNS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.LEVEL_CROSSING;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.MATRIX_SIGNS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.RAMP_CONTROLS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.ROADSIDE_COMMUNICATIONS_SYSTEM;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.ROADSIDE_POWER_SYSTEM;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.SPEED_CONTROL_SIGNS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.TOLL_GATES;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.TRAFFIC_LIGHT_SETS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.TRAFFIC_SIGNALS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.TUNNEL_LIGHTS;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.TUNNEL_VENTILATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum.VARIABLE_MESSAGE_SIGNS;
import static fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagementTypeEnum.BRIDGE_SWING_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagementTypeEnum.CONVOY_SERVICE;
import static fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagementTypeEnum.OBSTACLE_SIGNALLING;
import static fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagementTypeEnum.RAMP_METERING_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagementTypeEnum.TEMPORARY_TRAFFIC_LIGHTS;
import static fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagementTypeEnum.TOLL_GATES_OPEN;
import static fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagementTypeEnum.TRAFFIC_BEING_MANUALLY_DIRECTED;
import static fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagementTypeEnum.TRAFFIC_HELD;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.BURST_PIPE;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.BURST_WATER_MAIN;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.COLLAPSED_SEWER;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.DAMAGED_BRIDGE;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.DAMAGED_CRASH_BARRIER;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.DAMAGED_FLYOVER;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.DAMAGED_GALLERY;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.DAMAGED_GANTRY;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.DAMAGED_ROAD_SURFACE;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.DAMAGED_TUNNEL;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.DAMAGED_VIADUCT;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.FALLEN_POWER_CABLES;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.GAS_LEAK;
import static fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum.WEAK_BRIDGE;
import static fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum.DIESEL_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum.LEAVES_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum.LOOSE_CHIPPINGS;
import static fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum.LOOSE_SAND_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum.MUD_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum.OIL_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum.PETROL_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum.ROAD_SURFACE_IN_POOR_CONDITION;
import static fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum.SLIPPERY_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.AIR_CRASH;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.CHILDREN_ON_ROADWAY;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.CLEARANCE_WORK;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.CRANE_OPERATING;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.CYCLISTS_ON_ROADWAY;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.DEBRIS;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.EXPLOSION;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.EXPLOSION_HAZARD;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.HAZARDS_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.INCIDENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.INDUSTRIAL_ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.OBJECTS_FALLING_FROM_MOVING_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.OBJECT_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.OBSTRUCTION_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.PEOPLE_ON_ROADWAY;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.RAIL_CRASH;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.RESCUE_AND_RECOVERY_WORK;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.SEVERE_FROST_DAMAGED_ROADWAY;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.SHED_LOAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.SNOW_AND_ICE_DEBRIS;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.SPILLAGE_OCCURRING_FROM_MOVING_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.SPILLAGE_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum.UNPROTECTED_ACCIDENT_AREA;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.AGRICULTURAL_SHOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.AIR_SHOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.ATHLETICS_MEETING;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.BALL_GAME;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.BASEBALL_GAME;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.BASKETBALL_GAME;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.BICYCLE_RACE;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.BOAT_RACE;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.BOAT_SHOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.BOXING_TOURNAMENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.BULL_FIGHT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.CEREMONIAL_EVENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.COMMERCIAL_EVENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.CONCERT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.CRICKET_MATCH;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.CULTURAL_EVENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.EXHIBITION;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.FAIR;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.FESTIVAL;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.FILM_TV_MAKING;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.FOOTBALL_MATCH;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.FUNFAIR;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.GARDENING_OR_FLOWER_SHOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.GOLF_TOURNAMENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.HOCKEY_GAME;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.HORSE_RACE_MEETING;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.INTERNATIONAL_SPORTS_MEETING;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.MAJOR_EVENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.MARATHON;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.MARKET;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.MATCH;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.MOTOR_SHOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.MOTOR_SPORT_RACE_MEETING;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.PARADE;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.PROCESSION;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.RACE_MEETING;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.RUGBY_MATCH;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.SEVERAL_MAJOR_EVENTS;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.SHOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.SHOW_JUMPING;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.SPORTS_MEETING;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.STATE_OCCASION;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.TENNIS_TOURNAMENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.TOURNAMENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.TRADE_FAIR;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.WATER_SPORTS_MEETING;
import static fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum.WINTER_SPORTS_MEETING;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.DO_NOT_FOLLOW_DIVERSION_SIGNS;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.DO_NOT_USE_ENTRY;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.DO_NOT_USE_EXIT;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.DO_NOT_USE_INTERSECTION_OR_JUNCTION;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.FOLLOW_DIVERSION_SIGNS;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.FOLLOW_LOCAL_DIVERSION;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.FOLLOW_SPECIAL_MARKERS;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.USE_ENTRY;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.USE_EXIT;
import static fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum.USE_INTERSECTION_OR_JUNCTION;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum.GRASS_CUTTING_WORK;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum.MAINTENANCE_WORK;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum.OTHER;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum.RESURFACING_WORK;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum.ROADSIDE_WORK;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum.ROADWORKS;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum.ROADWORKS_CLEARANCE;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum.ROAD_MARKING_WORK;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum.TREE_AND_VEGETATION_CUTTING_WORK;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.CARRIAGEWAY_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.CAR_POOL_LANE_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.CLEAR_A_LANE_FOR_EMERGENCY_VEHICLES;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.CLEAR_A_LANE_FOR_SNOWPLOUGHS_AND_GRITTING_VEHICLES;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.CLOSED_PERMANENTLY_FOR_THE_WINTER;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.CONTRAFLOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.DO_NOT_USE_SPECIFIED_LANES_OR_CARRIAGEWAYS;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.HARD_SHOULDER_RUNNING_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.HEIGHT_RESTRICTION_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.INTERMITTENT_SHORT_TERM_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.KEEP_TO_THE_LEFT;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.KEEP_TO_THE_RIGHT;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.LANES_DEVIATED;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.LANE_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.NARROW_LANES;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.NEW_ROADWORKS_LAYOUT;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.OVERNIGHT_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.ROAD_CLEARED;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.ROAD_CLOSED;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.ROLLING_ROAD_BLOCK;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.RUSH_HOUR_LANE_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.SINGLE_ALTERNATE_LINE_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.TIDAL_FLOW_LANE_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.TURN_AROUND_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.USE_OF_SPECIFIED_LANES_OR_CARRIAGEWAYS_ALLOWED;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.USE_SPECIFIED_LANES_OR_CARRIAGEWAYS;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.VEHICLE_STORAGE_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum.WEIGHT_RESTRICTION_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.v3_7.TrafficFlowCharacteristicsEnum.ERRATIC_FLOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.TrafficFlowCharacteristicsEnum.SMOOTH_FLOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.TrafficFlowCharacteristicsEnum.STOP_AND_GO;
import static fi.livi.digitraffic.tie.datex2.v3_7.TrafficFlowCharacteristicsEnum.TRAFFIC_BLOCKED;
import static fi.livi.digitraffic.tie.datex2.v3_7.TrafficTrendTypeEnum.TRAFFIC_BUILDING_UP;
import static fi.livi.digitraffic.tie.datex2.v3_7.TrafficTrendTypeEnum.TRAFFIC_EASING;
import static fi.livi.digitraffic.tie.datex2.v3_7.TrafficTrendTypeEnum.TRAFFIC_STABLE;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.ABNORMAL_LOAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.BROKEN_DOWN_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.DAMAGED_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.HIGH_SPEED_CHASE;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.MILITARY_CONVOY;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.RECKLESS_DRIVER;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.VEHICLE_ON_FIRE;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.VEHICLE_ON_WRONG_CARRIAGEWAY;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.VEHICLE_STUCK;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.VEHICLE_WITH_OVERHEIGHT_LOAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum.VEHICLE_WITH_OVERWIDE_LOAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.BLACK_ICE;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.FREEZING_OF_WET_ROADS;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.FREEZING_PAVEMENTS;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.FREEZING_RAIN;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.FRESH_SNOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.ICE;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.ICE_BUILD_UP;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.ICY_PATCHES;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.NORMAL_WINTER_CONDITIONS_FOR_PEDESTRIANS;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.PACKED_SNOW;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.SLUSH_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.SNOW_ON_PAVEMENT;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.SNOW_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.SURFACE_WATER;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.WET_AND_ICY_ROAD;
import static fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum.WET_ICY_PAVEMENT;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.waze.WazeDatex2Converter;
import fi.livi.digitraffic.tie.datex2.v3_7.AbnormalTraffic;
import fi.livi.digitraffic.tie.datex2.v3_7.AbnormalTrafficTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.Accident;
import fi.livi.digitraffic.tie.datex2.v3_7.AccidentTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.CollisionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.AnimalPresenceObstruction;
import fi.livi.digitraffic.tie.datex2.v3_7.AnimalPresenceTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.AuthorityOperation;
import fi.livi.digitraffic.tie.datex2.v3_7.ConstructionWorkTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.ConstructionWorks;
import fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication;
import fi.livi.digitraffic.tie.datex2.v3_7.DisturbanceActivity;
import fi.livi.digitraffic.tie.datex2.v3_7.DisturbanceActivityTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstruction;
import fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemFault;
import fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemFaultTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.EquipmentOrSystemTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagement;
import fi.livi.digitraffic.tie.datex2.v3_7.GeneralObstruction;
import fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageObstruction;
import fi.livi.digitraffic.tie.datex2.v3_7.MaintenanceWorks;
import fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.NonWeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.datex2.v3_7.ObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.PoorEnvironmentConditions;
import fi.livi.digitraffic.tie.datex2.v3_7.PoorEnvironmentTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.PublicEvent;
import fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagement;
import fi.livi.digitraffic.tie.datex2.v3_7.ReroutingManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.RoadMaintenanceTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagement;
import fi.livi.digitraffic.tie.datex2.v3_7.RoadOrCarriagewayOrLaneManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.Situation;
import fi.livi.digitraffic.tie.datex2.v3_7.SituationRecord;
import fi.livi.digitraffic.tie.datex2.v3_7.SpeedManagement;
import fi.livi.digitraffic.tie.datex2.v3_7.TrafficFlowCharacteristicsEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.TrafficTrendTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.TransitInformation;
import fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstruction;
import fi.livi.digitraffic.tie.datex2.v3_7.VehicleObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.VehicleStatusEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.VehicleTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.WeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII37XmlMarshaller;
import fi.livi.digitraffic.tie.datex2.v3_7._AbnormalTrafficTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._AccidentTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._AnimalPresenceTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._CollisionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._ConstructionWorkTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._DisturbanceActivityTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._EnvironmentalObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._EquipmentOrSystemFaultTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._EquipmentOrSystemTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._GeneralNetworkManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._InfrastructureDamageTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._NonWeatherRelatedRoadConditionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._ObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._PoorEnvironmentTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._PublicEventTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._ReroutingManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._RoadMaintenanceTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._RoadOrCarriagewayOrLaneManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._TrafficFlowCharacteristicsEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._TrafficTrendTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._TransitServiceInformationEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._TransitServiceTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._VehicleObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._VehicleStatusEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._VehicleTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7._WeatherRelatedRoadConditionTypeEnum;
import java.util.function.Function;
import fi.livi.digitraffic.tie.datex2.v3_7.EnvironmentalObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.GeneralNetworkManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.InfrastructureDamageTypeEnum;
import fi.livi.digitraffic.tie.datex2.v3_7.PublicEventTypeEnum;

@Component
public class WazeDatex2MessageConverter {
    private static final Logger logger = LoggerFactory.getLogger(WazeDatex2MessageConverter.class);

    // use this string, when you want to skip some type-enum without error
    private static final String SKIP_SUBTYPE = "SKIP_SUBTYPE-5O7jh8ytZjQLKCurmFwYowFBaghhMk3U-SKIP_SUBTYPE";

    private final DatexII37XmlMarshaller datex2XmlStringToObjectMarshaller;

    private final Map<AbnormalTrafficTypeEnum, String> abnormalTrafficTypeEnumStringMap = new HashMap<>();
    private final Map<AccidentTypeEnum, String> accidentTypeMap = new HashMap<>();
    private final Map<CollisionTypeEnum, String> collisionTypeMap = new HashMap<>();
    private final Map<VehicleStatusEnum, String> vehicleStatusMap = new HashMap<>();
    private final Map<AnimalPresenceTypeEnum, String> animalPresenceTypeEnumStringMap = new HashMap<>();
    private final Map<DisturbanceActivityTypeEnum, String> disturbanceActivityTypeEnumStringMap = new HashMap<>();
    private final Map<EnvironmentalObstructionTypeEnum, String> environmentalObstructionTypeEnumStringMap = new HashMap<>();
    private final Map<EquipmentOrSystemFaultTypeEnum, String> equipmentOrSystemFaultTypeMap = new HashMap<>();
    private final Map<EquipmentOrSystemTypeEnum, String> equipmentOrSystemTypeMap = new HashMap<>();
    private final Map<GeneralNetworkManagementTypeEnum, String> generalNetworkManagementTypeEnumStringMap = new HashMap<>();
    private final Map<InfrastructureDamageTypeEnum, String> infrastructureDamageTypeEnumStringMap = new HashMap<>();
    private final Map<NonWeatherRelatedRoadConditionTypeEnum, String> nonWeatherRelatedRoadConditionTypeEnumStringMap = new HashMap<>();
    private final Map<ObstructionTypeEnum, String> obstructionTypeMap = new HashMap<>();
    private final Map<PoorEnvironmentTypeEnum, String> poorEnvironmentTypeEnumStringMap = new HashMap<>();
    private final Map<PublicEventTypeEnum, String> publicEventTypeEnumStringMap = new HashMap<>();
    private final Map<ReroutingManagementTypeEnum, String> reroutingManagementTypeMap = new HashMap<>();
    private final Map<RoadOrCarriagewayOrLaneManagementTypeEnum, String> roadOrCarriagewayOrLaneManagementTypeMap = new HashMap<>();
    private final Map<TrafficFlowCharacteristicsEnum, String> trafficFlowCharacteristicsEnumStringMap = new HashMap<>();
    private final Map<TrafficTrendTypeEnum, String> trafficTrendTypeEnumMap = new HashMap<>();
    private final Map<VehicleObstructionTypeEnum, String> vehicleObstructionTypeMap = new HashMap<>();
    private final Map<VehicleTypeEnum, String> brokenDownVehicleTypeMap = new HashMap<>();
    private final Map<WeatherRelatedRoadConditionTypeEnum, String> weatherRelatedRoadConditionTypeMap = new HashMap<>();
    private final Map<ConstructionWorkTypeEnum, String> constructionWorksTypeMap = new HashMap<>();
    private final Map<RoadMaintenanceTypeEnum, String> maintenanceWorksTypeMap = new HashMap<>();


    /**
     * Unwraps a Datex II 3.7 {@code _XxxEnum} wrapper to the enum constant and returns its
     * {@code name()}. The 2.2.3 model exposed the enum directly, so {@code toString()} was enough;
     * in 3.7 it would yield the wrapper's default {@code Object.toString()}.
     */
    private static <W, E extends Enum<E>> String enumName(final W wrapper, final Function<W, E> unwrap) {
        final E value = unwrap(wrapper, unwrap);
        return value != null ? value.name() : null;
    }

    /**
     * Looks up a description for a Datex II 3.7 {@code _XxxEnum} wrapper. Keeping the maps keyed by
     * the enum type means a value from the wrong enum is a compile error rather than a silent miss.
     */
    private static <W, E extends Enum<E>> String lookup(final Map<E, String> map, final W wrapper, final Function<W, E> unwrap) {
        final E key = unwrap(wrapper, unwrap);
        return key != null ? map.get(key) : null;
    }

    private static <W, E extends Enum<E>> E unwrap(final W wrapper, final Function<W, E> unwrap) {
        return wrapper != null ? unwrap.apply(wrapper) : null;
    }

    @Autowired
    public WazeDatex2MessageConverter(final DatexII37XmlMarshaller datex2XmlStringToObjectMarshaller) {
        this.datex2XmlStringToObjectMarshaller = datex2XmlStringToObjectMarshaller;

        constructMaps();
    }

    private void constructMaps() {
        abnormalTrafficTypeEnumStringMap.put(STATIONARY_TRAFFIC, "Stationary traffic");
        abnormalTrafficTypeEnumStringMap.put(QUEUING_TRAFFIC, "Queuing traffic");
        abnormalTrafficTypeEnumStringMap.put(SLOW_TRAFFIC, "Slow traffic");
        abnormalTrafficTypeEnumStringMap.put(HEAVY_TRAFFIC, "Heavy traffic");
        abnormalTrafficTypeEnumStringMap.put(UNSPECIFIED_ABNORMAL_TRAFFIC, "Abnormal traffic");
        abnormalTrafficTypeEnumStringMap.put(AbnormalTrafficTypeEnum.OTHER, "Abnormal traffic");

        accidentTypeMap.put(ACCIDENT, "Accident");
        accidentTypeMap.put(ACCIDENT_INVOLVING_MASS_TRANSIT_VEHICLE, "Accident involving busses");
        accidentTypeMap.put(ACCIDENT_INVOLVING_HAZARDOUS_MATERIALS, "Accident involving hazardous materials");
        accidentTypeMap.put(ACCIDENT_INVOLVING_HEAVY_LORRIES, "Accident involving heavy lorries");
        accidentTypeMap.put(SECONDARY_ACCIDENT, "Earlier accident");
        accidentTypeMap.put(MULTIPLE_VEHICLE_ACCIDENT, "Accident involving multiple vehicles");
        accidentTypeMap.put(AccidentTypeEnum.OTHER, "Accident");

        // Datex II 3.7 moved these from accidentType to Accident.collisionType
        collisionTypeMap.put(CollisionTypeEnum.REAR_COLLISION, "Rear collision");

        // Datex II 3.7 moved these from accidentType to Vehicle.vehicleStatus
        vehicleStatusMap.put(VehicleStatusEnum.OVERTURNED, "Overturned vehicle");
        vehicleStatusMap.put(VehicleStatusEnum.SPUN_AROUND, "Vehicle spun around");

        animalPresenceTypeEnumStringMap.put(ANIMALS_ON_THE_ROAD, "Animals on the road");
        animalPresenceTypeEnumStringMap.put(HERD_OF_ANIMALS_ON_THE_ROAD, "Herd of animals on the road");
        animalPresenceTypeEnumStringMap.put(LARGE_ANIMALS_ON_THE_ROAD, "Large animals on the road");

        disturbanceActivityTypeEnumStringMap.put(CROWD, "Crowd");
        disturbanceActivityTypeEnumStringMap.put(DEMONSTRATION, "Demonstration");
        disturbanceActivityTypeEnumStringMap.put(MARCH, "March");
        disturbanceActivityTypeEnumStringMap.put(PUBLIC_DISTURBANCE, "Public disturbance");
        disturbanceActivityTypeEnumStringMap.put(RIOT, "Riot");
        disturbanceActivityTypeEnumStringMap.put(STRIKE, "Strike");
        disturbanceActivityTypeEnumStringMap.put(UNSPECIFIED_ALERT, "Unspecified alert");

        environmentalObstructionTypeEnumStringMap.put(AVALANCHES, "Avalanches");
        environmentalObstructionTypeEnumStringMap.put(EARTHQUAKE_DAMAGE, "Earthquake damage");
        environmentalObstructionTypeEnumStringMap.put(FALLEN_TREES, "Fallen trees");
        environmentalObstructionTypeEnumStringMap.put(FALLING_ICE, "Falling ice");
        environmentalObstructionTypeEnumStringMap.put(FALLING_LIGHT_ICE_OR_SNOW, "Falling light ice or snow");
        environmentalObstructionTypeEnumStringMap.put(FLASH_FLOODS, "Flash floods");
        environmentalObstructionTypeEnumStringMap.put(FLOODING, "Flooding");
        environmentalObstructionTypeEnumStringMap.put(FOREST_FIRE, "Forest fire");
        environmentalObstructionTypeEnumStringMap.put(GRASS_FIRE, "Grassfire");
        environmentalObstructionTypeEnumStringMap.put(LANDSLIPS, "Landslips");
        environmentalObstructionTypeEnumStringMap.put(MUD_SLIDE, "Mud slide");
        environmentalObstructionTypeEnumStringMap.put(SEWER_OVERFLOW, "Sewer overflow");
        environmentalObstructionTypeEnumStringMap.put(ROCKFALLS, "Rockfalls");
        environmentalObstructionTypeEnumStringMap.put(SERIOUS_FIRE, "Serious fire");
        environmentalObstructionTypeEnumStringMap.put(SMOKE_OR_FUMES, "Smoke or fumes");
        environmentalObstructionTypeEnumStringMap.put(STORM_DAMAGE, "Storm damage");
        environmentalObstructionTypeEnumStringMap.put(SUBSIDENCE, "Subsidence");
        environmentalObstructionTypeEnumStringMap.put(EnvironmentalObstructionTypeEnum.OTHER, "Environmental Obstruction");

        equipmentOrSystemTypeMap.put(ANPR_CAMERAS, "ANPR Cameras");
        equipmentOrSystemTypeMap.put(AUTOMATED_TOLL_SYSTEM, "Automated toll system");
        equipmentOrSystemTypeMap.put(CCTV_CAMERAS, "CCTV Cameras");
        equipmentOrSystemTypeMap.put(EMERGENCY_ROADSIDE_TELEPHONES, "Emergency roadside telephones");
        equipmentOrSystemTypeMap.put(GALLERY_LIGHTS, "Gallery lights");
        equipmentOrSystemTypeMap.put(LANE_CONTROL_SIGNS, "Lane control signs");
        equipmentOrSystemTypeMap.put(LEVEL_CROSSING, "Level crossing");
        equipmentOrSystemTypeMap.put(MATRIX_SIGNS, "Matrix signs");
        equipmentOrSystemTypeMap.put(RAMP_CONTROLS, "Ramp controls");
        equipmentOrSystemTypeMap.put(ROADSIDE_COMMUNICATIONS_SYSTEM, "Roadside communications system");
        equipmentOrSystemTypeMap.put(ROADSIDE_POWER_SYSTEM, "Roadside power system");
        equipmentOrSystemTypeMap.put(SPEED_CONTROL_SIGNS, "Speed control signs");
        equipmentOrSystemTypeMap.put(EquipmentOrSystemTypeEnum.TEMPORARY_TRAFFIC_LIGHTS, "Temporary traffic lights");
        equipmentOrSystemTypeMap.put(TOLL_GATES, "Toll gates");
        equipmentOrSystemTypeMap.put(TRAFFIC_LIGHT_SETS, "Traffic light sets");
        equipmentOrSystemTypeMap.put(TRAFFIC_SIGNALS, "Traffic signals");
        equipmentOrSystemTypeMap.put(TUNNEL_LIGHTS, "Tunnel lights");
        equipmentOrSystemTypeMap.put(TUNNEL_VENTILATION, "Tunnel ventilation");
        equipmentOrSystemTypeMap.put(VARIABLE_MESSAGE_SIGNS, "Variable message signs");
        equipmentOrSystemTypeMap.put(EquipmentOrSystemTypeEnum.OTHER, "Equipment or system");

        equipmentOrSystemFaultTypeMap.put(NOT_WORKING, "not working");
        equipmentOrSystemFaultTypeMap.put(OUT_OF_SERVICE, "out of service");
        equipmentOrSystemFaultTypeMap.put(WORKING_INTERMITTENTLY, "working intermittently");
        equipmentOrSystemFaultTypeMap.put(WORKING_INCORRECTLY, "working incorrectly");

        generalNetworkManagementTypeEnumStringMap.put(BRIDGE_SWING_IN_OPERATION, "Bridge swing in operation");
        generalNetworkManagementTypeEnumStringMap.put(CONVOY_SERVICE, "Convoy service");
        generalNetworkManagementTypeEnumStringMap.put(OBSTACLE_SIGNALLING, "Obstacle signaling");
        generalNetworkManagementTypeEnumStringMap.put(RAMP_METERING_IN_OPERATION, "Ramp metering in operation");
        generalNetworkManagementTypeEnumStringMap.put(TEMPORARY_TRAFFIC_LIGHTS, "Temporary traffic lights");
        generalNetworkManagementTypeEnumStringMap.put(TOLL_GATES_OPEN, "Toll gates open");
        generalNetworkManagementTypeEnumStringMap.put(TRAFFIC_BEING_MANUALLY_DIRECTED, "Traffic being manually directed");
        generalNetworkManagementTypeEnumStringMap.put(TRAFFIC_HELD, "Traffic held");
        generalNetworkManagementTypeEnumStringMap.put(GeneralNetworkManagementTypeEnum.OTHER, "Network Management");

        infrastructureDamageTypeEnumStringMap.put(BURST_PIPE, "Burst pipe");
        infrastructureDamageTypeEnumStringMap.put(BURST_WATER_MAIN, "Burst water main");
        infrastructureDamageTypeEnumStringMap.put(COLLAPSED_SEWER, "Collapsed sewer");
        infrastructureDamageTypeEnumStringMap.put(DAMAGED_BRIDGE, "Damaged bridge");
        infrastructureDamageTypeEnumStringMap.put(DAMAGED_CRASH_BARRIER, "Damaged crash barrier");
        infrastructureDamageTypeEnumStringMap.put(DAMAGED_FLYOVER, "Damaged flyover");
        infrastructureDamageTypeEnumStringMap.put(DAMAGED_GALLERY, "Damaged gallery");
        infrastructureDamageTypeEnumStringMap.put(DAMAGED_GANTRY, "Damaged gantry");
        infrastructureDamageTypeEnumStringMap.put(DAMAGED_ROAD_SURFACE, "Damaged road surface");
        infrastructureDamageTypeEnumStringMap.put(DAMAGED_TUNNEL, "Damaged tunnel");
        infrastructureDamageTypeEnumStringMap.put(DAMAGED_VIADUCT, "Damaged viaduct");
        infrastructureDamageTypeEnumStringMap.put(FALLEN_POWER_CABLES, "Fallen power cables");
        infrastructureDamageTypeEnumStringMap.put(GAS_LEAK, "Gas leak");
        infrastructureDamageTypeEnumStringMap.put(WEAK_BRIDGE, "Weak bridge");
        infrastructureDamageTypeEnumStringMap.put(InfrastructureDamageTypeEnum.OTHER, "Damage on infrastructure");

        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(DIESEL_ON_ROAD, "Diesel on road");
        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(LEAVES_ON_ROAD, "Leaves on road");
        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(LOOSE_CHIPPINGS, "Loose chippings");
        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(LOOSE_SAND_ON_ROAD, "Loose sand on road");
        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(MUD_ON_ROAD, "Mud on road");
        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(OIL_ON_ROAD, "Oil on road");
        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(PETROL_ON_ROAD, "Petrol on road");
        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(ROAD_SURFACE_IN_POOR_CONDITION, "Road surface in poor condition");
        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(SLIPPERY_ROAD, "Slippery road");
        nonWeatherRelatedRoadConditionTypeEnumStringMap.put(NonWeatherRelatedRoadConditionTypeEnum.OTHER, "Non weather related road condition");

        obstructionTypeMap.put(AIR_CRASH, "Air crash");
        obstructionTypeMap.put(CHILDREN_ON_ROADWAY, "Children on roadway");
        obstructionTypeMap.put(CLEARANCE_WORK, "Clearance work");
        obstructionTypeMap.put(CRANE_OPERATING, "Crane operating");
        obstructionTypeMap.put(CYCLISTS_ON_ROADWAY, "Cyclists on roadway");
        obstructionTypeMap.put(DEBRIS, "Debris on roadway");
        obstructionTypeMap.put(EXPLOSION, "Explosion");
        obstructionTypeMap.put(EXPLOSION_HAZARD, "Explosion hazard");
        obstructionTypeMap.put(HAZARDS_ON_THE_ROAD, "Hazards on the road");
        obstructionTypeMap.put(INCIDENT, "Incident on roadway");
        obstructionTypeMap.put(INDUSTRIAL_ACCIDENT, "Industrial accident");
        obstructionTypeMap.put(OBJECT_ON_THE_ROAD, "Object on the road");
        obstructionTypeMap.put(OBJECTS_FALLING_FROM_MOVING_VEHICLE, "Objects falling from moving vehicle");
        obstructionTypeMap.put(OBSTRUCTION_ON_THE_ROAD, "Obstruction on the road");
        obstructionTypeMap.put(PEOPLE_ON_ROADWAY, "People on roadway");
        obstructionTypeMap.put(RAIL_CRASH, "Rail crash");
        obstructionTypeMap.put(RESCUE_AND_RECOVERY_WORK, "Rescue and recovery work");
        obstructionTypeMap.put(SEVERE_FROST_DAMAGED_ROADWAY, "Severe frost damaged roadway");
        obstructionTypeMap.put(SHED_LOAD, "Shed load");
        obstructionTypeMap.put(SNOW_AND_ICE_DEBRIS, "Snow and ice debris");
        obstructionTypeMap.put(SPILLAGE_OCCURRING_FROM_MOVING_VEHICLE, "Spillage occurring from moving vehicle");
        obstructionTypeMap.put(SPILLAGE_ON_THE_ROAD, "Spillage on the road");
        obstructionTypeMap.put(UNPROTECTED_ACCIDENT_AREA, "Unprotected accident area");
        obstructionTypeMap.put(ObstructionTypeEnum.OTHER, "Obstruction on roadway");

        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.BAD_WEATHER, "Bad weather");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.BLIZZARD, "Blizzard");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.BLOWING_DUST, "Blowing dust");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.BLOWING_SNOW, "Blowing snow");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.CROSSWINDS, "Crosswinds");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.DAMAGING_HAIL, "Damaging hail");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.DENSE_FOG, "Dense fog");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.ECLIPSE, "Eclipse");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.EXTREME_COLD, "Extreme cold");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.EXTREME_HEAT, "Extreme heat");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.FOG, "Fog");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.FREEZING_FOG, "Freezing fog");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.FROST, "Frost");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.GALES, "Gales");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.GUSTY_WINDS, "Gusty winds");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.HAIL, "Hail");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.HEAVY_FROST, "Heavy frost");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.HEAVY_RAIN, "Heavy rain");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.HEAVY_SNOWFALL, "Heavy snowfall");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.HURRICANE_FORCE_WINDS, "Hurricane force winds");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.LOW_SUN_GLARE, "Low sun glare");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.MODERATE_FOG, "Moderate fog");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.OZONE_POLLUTION, "Ozone pollution");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.POLLUTION, "Pollution");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.PATCHY_FOG, "Patchy fog");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.PRECIPITATION_IN_THE_AREA, "Precipitation in the area");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.RAIN, "Rain");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.RAIN_CHANGING_TO_SNOW, "Rain changing to snow");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SAND_STORMS, "Sand storms");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SEVERE_EXHAUST_POLLUTION, "Severe exhaust pollution");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SEVERE_SMOG, "Severe smog");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SHOWERS, "Showers");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SLEET, "Sleet");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SMOG_ALERT, "Smog alert");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SMOKE_HAZARD, "Smoke hazard");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SNOW_CHANGING_TO_RAIN, "Snow changing to rain");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SNOWFALL, "Snowfall");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SPRAY_HAZARD, "Spray hazard");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.STORM_FORCE_WINDS, "Storm force winds");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.STRONG_GUSTS_OF_WIND, "Strong gusts of wind");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.STRONG_WINDS, "Strong winds");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.SWARMS_OF_INSECTS, "Swarms of insects");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.TEMPERATURE_FALLING, "Temperature falling");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.THUNDERSTORMS, "Thunderstorms");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.TORNADOES, "Tornadoes");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.VERY_STRONG_GUSTS_OF_WIND, "Very strong gusts of wind");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.VISIBILITY_REDUCED, "Visibility reduced");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.WHITE_OUT, "White out");
        poorEnvironmentTypeEnumStringMap.put(PoorEnvironmentTypeEnum.WINTER_STORM, "Winter storm");

        publicEventTypeEnumStringMap.put(AGRICULTURAL_SHOW, "agricultural show");
        publicEventTypeEnumStringMap.put(AIR_SHOW, "air show");
        publicEventTypeEnumStringMap.put(ATHLETICS_MEETING, "athletics meeting");
        publicEventTypeEnumStringMap.put(COMMERCIAL_EVENT, "commercial event");
        publicEventTypeEnumStringMap.put(CULTURAL_EVENT, "cultural event");
        publicEventTypeEnumStringMap.put(BALL_GAME, "ball game");
        publicEventTypeEnumStringMap.put(BASEBALL_GAME, "baseball game");
        publicEventTypeEnumStringMap.put(BASKETBALL_GAME, "basketball game");
        publicEventTypeEnumStringMap.put(BICYCLE_RACE, "bicycle race");
        publicEventTypeEnumStringMap.put(BOAT_RACE, "boat race");
        publicEventTypeEnumStringMap.put(BOAT_SHOW, "boat show");
        publicEventTypeEnumStringMap.put(BOXING_TOURNAMENT, "boxing tournament");
        publicEventTypeEnumStringMap.put(BULL_FIGHT, "bull fight");
        publicEventTypeEnumStringMap.put(CEREMONIAL_EVENT, "ceremonial event");
        publicEventTypeEnumStringMap.put(CONCERT, "concert");
        publicEventTypeEnumStringMap.put(CRICKET_MATCH, "cricket match");
        publicEventTypeEnumStringMap.put(EXHIBITION, "exhibition");
        publicEventTypeEnumStringMap.put(FAIR, "fair");
        publicEventTypeEnumStringMap.put(FESTIVAL, "festival");
        publicEventTypeEnumStringMap.put(FILM_TV_MAKING, "film or tv making");
        publicEventTypeEnumStringMap.put(FOOTBALL_MATCH, "football match");
        publicEventTypeEnumStringMap.put(FUNFAIR, "funfair");
        publicEventTypeEnumStringMap.put(GARDENING_OR_FLOWER_SHOW, "gardening or flower show");
        publicEventTypeEnumStringMap.put(GOLF_TOURNAMENT, "golf tournament");
        publicEventTypeEnumStringMap.put(HOCKEY_GAME, "hockey game");
        publicEventTypeEnumStringMap.put(HORSE_RACE_MEETING, "horse race meeting");
        publicEventTypeEnumStringMap.put(INTERNATIONAL_SPORTS_MEETING, "international sports meeting");
        publicEventTypeEnumStringMap.put(MAJOR_EVENT, "major event");
        publicEventTypeEnumStringMap.put(MARATHON, "marathon");
        publicEventTypeEnumStringMap.put(MARKET, "market");
        publicEventTypeEnumStringMap.put(MATCH, "match");
        publicEventTypeEnumStringMap.put(MOTOR_SHOW, "motor show");
        publicEventTypeEnumStringMap.put(MOTOR_SPORT_RACE_MEETING, "motor sport race meeting");
        publicEventTypeEnumStringMap.put(PARADE, "parade");
        publicEventTypeEnumStringMap.put(PROCESSION, "procession");
        publicEventTypeEnumStringMap.put(RACE_MEETING, "race meeting");
        publicEventTypeEnumStringMap.put(RUGBY_MATCH, "rugby match");
        publicEventTypeEnumStringMap.put(SEVERAL_MAJOR_EVENTS, "several major events");
        publicEventTypeEnumStringMap.put(SHOW, "show");
        publicEventTypeEnumStringMap.put(SHOW_JUMPING, "show jumping");
        publicEventTypeEnumStringMap.put(SPORTS_MEETING, "sports meeting");
        publicEventTypeEnumStringMap.put(STATE_OCCASION, "state occasion");
        publicEventTypeEnumStringMap.put(TENNIS_TOURNAMENT, "tennis tournament");
        publicEventTypeEnumStringMap.put(TOURNAMENT, "tournament");
        publicEventTypeEnumStringMap.put(TRADE_FAIR, "trade fair");
        publicEventTypeEnumStringMap.put(WATER_SPORTS_MEETING, "water sports meeting");
        publicEventTypeEnumStringMap.put(WINTER_SPORTS_MEETING, "winter sports meeting");

        reroutingManagementTypeMap.put(DO_NOT_FOLLOW_DIVERSION_SIGNS, "Do not follow diversion signs");
        reroutingManagementTypeMap.put(DO_NOT_USE_ENTRY, "Do not use entry");
        reroutingManagementTypeMap.put(DO_NOT_USE_EXIT, "Do not use exit");
        reroutingManagementTypeMap.put(DO_NOT_USE_INTERSECTION_OR_JUNCTION, "Do not use intersection or junction");
        reroutingManagementTypeMap.put(FOLLOW_DIVERSION_SIGNS, "Follow diversion signs");
        reroutingManagementTypeMap.put(FOLLOW_LOCAL_DIVERSION, "Follow local diversion");
        reroutingManagementTypeMap.put(FOLLOW_SPECIAL_MARKERS, "Follow special markers");
        reroutingManagementTypeMap.put(USE_ENTRY, "Use entry");
        reroutingManagementTypeMap.put(USE_EXIT, "Use exit");
        reroutingManagementTypeMap.put(USE_INTERSECTION_OR_JUNCTION, "Use intersection or junction");

        roadOrCarriagewayOrLaneManagementTypeMap.put(CARRIAGEWAY_CLOSURES, "Carriageway closures");
        roadOrCarriagewayOrLaneManagementTypeMap.put(CAR_POOL_LANE_IN_OPERATION, "Car pool lane in operation");
        roadOrCarriagewayOrLaneManagementTypeMap.put(CLEAR_A_LANE_FOR_EMERGENCY_VEHICLES, "Clear a lane for emergency vehicles");
        roadOrCarriagewayOrLaneManagementTypeMap.put(CLEAR_A_LANE_FOR_SNOWPLOUGHS_AND_GRITTING_VEHICLES, "Clear a lane for snowploughs and gritting vehicles");
        roadOrCarriagewayOrLaneManagementTypeMap.put(CLOSED_PERMANENTLY_FOR_THE_WINTER, "Closed permanently for the winter");
        roadOrCarriagewayOrLaneManagementTypeMap.put(CONTRAFLOW, "Contraflow");
        roadOrCarriagewayOrLaneManagementTypeMap.put(DO_NOT_USE_SPECIFIED_LANES_OR_CARRIAGEWAYS, "Do not use specified lanes or carriageways");
        roadOrCarriagewayOrLaneManagementTypeMap.put(HARD_SHOULDER_RUNNING_IN_OPERATION, "Hard shoulder running in operation");
        roadOrCarriagewayOrLaneManagementTypeMap.put(HEIGHT_RESTRICTION_IN_OPERATION, "Height restriction in operation");
        roadOrCarriagewayOrLaneManagementTypeMap.put(INTERMITTENT_SHORT_TERM_CLOSURES, "Intermittent short term closures");
        roadOrCarriagewayOrLaneManagementTypeMap.put(KEEP_TO_THE_LEFT, "Keep to the left");
        roadOrCarriagewayOrLaneManagementTypeMap.put(KEEP_TO_THE_RIGHT, "Keep to the Right");
        roadOrCarriagewayOrLaneManagementTypeMap.put(LANES_DEVIATED, "Lanes deviated");
        roadOrCarriagewayOrLaneManagementTypeMap.put(LANE_CLOSURES, "Lane closures");
        roadOrCarriagewayOrLaneManagementTypeMap.put(NARROW_LANES, "Narrow lanes");
        roadOrCarriagewayOrLaneManagementTypeMap.put(NEW_ROADWORKS_LAYOUT, "New roadworks layout");
        roadOrCarriagewayOrLaneManagementTypeMap.put(OVERNIGHT_CLOSURES, "Overnight closures");
        roadOrCarriagewayOrLaneManagementTypeMap.put(ROAD_CLEARED, "Road cleared");
        roadOrCarriagewayOrLaneManagementTypeMap.put(ROAD_CLOSED, "Road closed");
        roadOrCarriagewayOrLaneManagementTypeMap.put(ROLLING_ROAD_BLOCK, "Rolling road block");
        roadOrCarriagewayOrLaneManagementTypeMap.put(RUSH_HOUR_LANE_IN_OPERATION, "Rush hour lane in operation");
        roadOrCarriagewayOrLaneManagementTypeMap.put(SINGLE_ALTERNATE_LINE_TRAFFIC, "Single alternate line traffic");
        roadOrCarriagewayOrLaneManagementTypeMap.put(TIDAL_FLOW_LANE_IN_OPERATION, "Tidal flow lane in operation");
        roadOrCarriagewayOrLaneManagementTypeMap.put(TURN_AROUND_IN_OPERATION, "Turn around in operation");
        roadOrCarriagewayOrLaneManagementTypeMap.put(USE_OF_SPECIFIED_LANES_OR_CARRIAGEWAYS_ALLOWED, "Use of specified lanes or carriageways");
        roadOrCarriagewayOrLaneManagementTypeMap.put(USE_SPECIFIED_LANES_OR_CARRIAGEWAYS, "Use specified lanes or carriageways");
        roadOrCarriagewayOrLaneManagementTypeMap.put(VEHICLE_STORAGE_IN_OPERATION, "Vehicle storage in operation");
        roadOrCarriagewayOrLaneManagementTypeMap.put(WEIGHT_RESTRICTION_IN_OPERATION, "Weight restriction in operation");
        roadOrCarriagewayOrLaneManagementTypeMap.put(RoadOrCarriagewayOrLaneManagementTypeEnum.OTHER, "Road or carriageway or lane management");

        trafficFlowCharacteristicsEnumStringMap.put(ERRATIC_FLOW, "Traffic flow is erratic");
        trafficFlowCharacteristicsEnumStringMap.put(SMOOTH_FLOW, "Traffic flow is smooth");
        trafficFlowCharacteristicsEnumStringMap.put(STOP_AND_GO, "Traffic flow is stop and go");
        trafficFlowCharacteristicsEnumStringMap.put(TRAFFIC_BLOCKED, "Traffic flow is blocked");

        trafficTrendTypeEnumMap.put(TRAFFIC_EASING, "Traffic easing");
        trafficTrendTypeEnumMap.put(TRAFFIC_BUILDING_UP, "Traffic building up");
        trafficTrendTypeEnumMap.put(TRAFFIC_STABLE, "Traffic stable");

        vehicleObstructionTypeMap.put(VEHICLE_ON_WRONG_CARRIAGEWAY, "Vehicle on wrong carriageway");
        vehicleObstructionTypeMap.put(ABNORMAL_LOAD, "Abnormal load on vehicle");
        vehicleObstructionTypeMap.put(VEHICLE_ON_FIRE, "Vehicle on fire");
        vehicleObstructionTypeMap.put(VEHICLE_STUCK, "Vehicle stuck");
        vehicleObstructionTypeMap.put(BROKEN_DOWN_VEHICLE, "Broken down vehicle");
        vehicleObstructionTypeMap.put(DAMAGED_VEHICLE, "Damaged vehicle");
        vehicleObstructionTypeMap.put(HIGH_SPEED_CHASE, "High speed chase");
        vehicleObstructionTypeMap.put(MILITARY_CONVOY, "Military convoy");
        vehicleObstructionTypeMap.put(RECKLESS_DRIVER, "Reckless driver");
        vehicleObstructionTypeMap.put(VEHICLE_WITH_OVERHEIGHT_LOAD, "Vehicle with oversize load");
        vehicleObstructionTypeMap.put(VEHICLE_WITH_OVERWIDE_LOAD, "Vehicle with oversize load");
        vehicleObstructionTypeMap.put(VehicleObstructionTypeEnum.OTHER, "Vehicle obstruction");

        // Datex II 2.2.3 had its own value for a broken down heavy lorry. In 3.7 the type of the
        // vehicle moved to the separate involvedVehicleType field, so it has to be read together
        // with the obstruction type to keep the description as accurate as it was.
        brokenDownVehicleTypeMap.put(VehicleTypeEnum.HEAVY_VEHICLE, "Broken down heavy lorry");

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
        weatherRelatedRoadConditionTypeMap.put(WeatherRelatedRoadConditionTypeEnum.OTHER, SKIP_SUBTYPE);

        maintenanceWorksTypeMap.put(RESURFACING_WORK, "Resurfacing work");
        maintenanceWorksTypeMap.put(MAINTENANCE_WORK, "Maintenance work");
        maintenanceWorksTypeMap.put(ROADSIDE_WORK, "Roadside work");
        maintenanceWorksTypeMap.put(ROADWORKS, "Roadworks");
        maintenanceWorksTypeMap.put(ROADWORKS_CLEARANCE, "Roadworks clearance");
        maintenanceWorksTypeMap.put(ROAD_MARKING_WORK, "Road marking work");
        maintenanceWorksTypeMap.put(GRASS_CUTTING_WORK, "Grass cutting work");
        maintenanceWorksTypeMap.put(TREE_AND_VEGETATION_CUTTING_WORK, "Tree and vegetation cutting work");
        maintenanceWorksTypeMap.put(OTHER, SKIP_SUBTYPE); // skip other, too vague

        constructionWorksTypeMap.put(ConstructionWorkTypeEnum.CONSTRUCTION_WORK, "Construction work");
        constructionWorksTypeMap.put(ConstructionWorkTypeEnum.BLASTING_WORK, "Blasting work");
    }

    public String export(final String situationId, final String datex2Message) {
        final SituationPublication d2LogicalModel;

        try {
            d2LogicalModel = datex2XmlStringToObjectMarshaller.convertToObject(null, datex2Message);
        } catch (final UnmarshallingFailureException e) {
            logger.error("method=export situation {} did not have a proper datex2 message, error: {}", situationId, e.getMessage());
            return "";
        }

        return export(situationId, d2LogicalModel);
    }

    public String exportPhase(final String situationId, final String phaseId, final SituationPublication d2logicalModel) {
        return getDescriptions(situationId, d2logicalModel)
                .filter(p -> isSamePhase(phaseId, p.getLeft()))
                .findFirst()
                .map(Pair::getRight)
                .orElse("");
    }

    /// phase id is from simple json, and candidate is situationRecord id from datex2-xml
    /// situationRecord id might have 01, 02... added to its end
    private boolean isSamePhase(final String phaseId, final String candidate) {
        return candidate.startsWith(phaseId);
    }

    public String export(final String situationId, final SituationPublication d2LogicalModel) {
        return getDescriptions(situationId, d2LogicalModel)
                .map(Pair::getRight)
                .collect(Collectors.joining(". ", "", "."));
    }

    private Stream<Pair<String, String>> getDescriptions(final String situationId, final SituationPublication d2logicalModel) {
        final SituationPublication situationPublication = d2logicalModel;
        if (situationPublication == null) {
            logger.info("method=export situation {} did not have a situation publication payload", situationId);
            return Stream.empty();
        }

        final List<Situation> situations = situationPublication.getSituations();
        if (situations.isEmpty()) {
            logger.info("method=export situation {} did not have any situation records", situationId);
            return Stream.empty();
        }

        return situations.stream()
                .map(Situation::getSituationRecords)
                .flatMap(Collection::stream)
                .filter(WazeDatex2Converter::isActiveSituationRecord)
                .flatMap(sr -> accept(situationId, sr).stream())
                .distinct();
    }

    private Optional<String> accept(final AbnormalTraffic abnormalTraffic) {
        final Optional<String> trafficTrendTypeOptional = Optional.ofNullable(abnormalTraffic.getTrafficTrendType())
            .map(x -> lookup(trafficTrendTypeEnumMap, x, _TrafficTrendTypeEnum::getValue));

        final Optional<String> abnormalTrafficTypeOptional = Optional.ofNullable(abnormalTraffic.getAbnormalTrafficType())
            .map(x -> lookup(abnormalTrafficTypeEnumStringMap, x, _AbnormalTrafficTypeEnum::getValue));

        final Optional<String> trafficFlowCharacteristicsOptional = Optional.ofNullable(abnormalTraffic.getTrafficFlowCharacteristics())
            .map(x -> lookup(trafficFlowCharacteristicsEnumStringMap, x, _TrafficFlowCharacteristicsEnum::getValue));

        return Stream.of(trafficTrendTypeOptional, abnormalTrafficTypeOptional, trafficFlowCharacteristicsOptional)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<String> accept(final Accident accident) {
        return accident.getAccidentTypes()
            .stream()
            .findFirst()
            .map(x -> lookup(accidentTypeMap, x, _AccidentTypeEnum::getValue))
            .or(() -> Optional.ofNullable(lookup(collisionTypeMap, accident.getCollisionType(), _CollisionTypeEnum::getValue)))
            .or(() -> accident.getVehicleInvolveds()
                .stream()
                .map(v -> lookup(vehicleStatusMap, v.getVehicleStatus(), _VehicleStatusEnum::getValue))
                .filter(Objects::nonNull)
                .findFirst());
    }

    private Optional<String> accept(final AnimalPresenceObstruction animalPresenceObstruction) {
        return Optional.of(lookup(animalPresenceTypeEnumStringMap, animalPresenceObstruction.getAnimalPresenceType(), _AnimalPresenceTypeEnum::getValue));
    }

    private Optional<String> accept(final AuthorityOperation authorityOperation) {
        return Optional.of(SKIP_SUBTYPE);
    }
    private Optional<String> accept(final DisturbanceActivity disturbanceActivity) {
        return Optional.of(lookup(disturbanceActivityTypeEnumStringMap, disturbanceActivity.getDisturbanceActivityType(), _DisturbanceActivityTypeEnum::getValue));
    }
    private Optional<String> accept(final EnvironmentalObstruction environmentalObstruction) {
        final var environmentalObstructionTypeEnumOptional = Optional.ofNullable(environmentalObstruction.getEnvironmentalObstructionType());
        return environmentalObstructionTypeEnumOptional.map(x -> lookup(environmentalObstructionTypeEnumStringMap, x, _EnvironmentalObstructionTypeEnum::getValue));
    }

    private Optional<String> accept(final EquipmentOrSystemFault equipmentOrSystemFault) {
        final Optional<String> equipmentOrSystemType = Optional.ofNullable(equipmentOrSystemFault.getFaultyEquipmentOrSystemType())
            .map(x -> lookup(equipmentOrSystemTypeMap, x, _EquipmentOrSystemTypeEnum::getValue));

        // A repaired equipment still carries its original fault type, so the free text has to be
        // read first. Otherwise the description would state the fault that was just fixed.
        final Optional<String> fault = WazeDatex2Converter.isRepairedRecord(equipmentOrSystemFault)
            ? Optional.of("repaired")
            : Optional.ofNullable(equipmentOrSystemFault.getEquipmentOrSystemFaultType())
                .map(x -> lookup(equipmentOrSystemFaultTypeMap, x, _EquipmentOrSystemFaultTypeEnum::getValue));

        return fault.flatMap(x -> equipmentOrSystemType.map(y -> y + " " + x));
    }

    private Optional<String> accept(final GeneralNetworkManagement generalNetworkManagement) {
        final var generalNetworkManagementTypeEnumOptional = Optional.ofNullable(generalNetworkManagement.getGeneralNetworkManagementType());

        return generalNetworkManagementTypeEnumOptional
            .map(x -> lookup(generalNetworkManagementTypeEnumStringMap, x, _GeneralNetworkManagementTypeEnum::getValue));
    }

    private Optional<String> accept(final GeneralObstruction generalObstruction) {
        return generalObstruction.getObstructionTypes()
            .stream()
            .findFirst()
            .map(x -> lookup(obstructionTypeMap, x, _ObstructionTypeEnum::getValue));
    }

    private Optional<String> accept(final InfrastructureDamageObstruction infrastructureDamageObstruction) {
        final var damageType = infrastructureDamageObstruction.getInfrastructureDamageType();
        return Optional.ofNullable(lookup(infrastructureDamageTypeEnumStringMap, damageType, _InfrastructureDamageTypeEnum::getValue));
    }
    private Optional<String> accept(final NonWeatherRelatedRoadConditions nonWeatherRelatedRoadConditions) {
        final String nonweatherRelatedRoadConditionTypes = nonWeatherRelatedRoadConditions.getNonWeatherRelatedRoadConditionTypes().stream()
            .map(x -> lookup(nonWeatherRelatedRoadConditionTypeEnumStringMap, x, _NonWeatherRelatedRoadConditionTypeEnum::getValue))
            .collect(Collectors.joining(". "));

        if (nonweatherRelatedRoadConditionTypes.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(nonweatherRelatedRoadConditionTypes);
    }

    private Optional<String> accept(final PoorEnvironmentConditions poorEnvironmentConditions) {
        final var environmentConditions = poorEnvironmentConditions.getPoorEnvironmentTypes().stream()
            .map(x -> lookup(poorEnvironmentTypeEnumStringMap, x, _PoorEnvironmentTypeEnum::getValue))
            .filter(Objects::nonNull)
            .collect(Collectors.joining(". "));

        return Optional.of(environmentConditions);
    }

    private Optional<String> accept(final PublicEvent publicEvent) {
        final var publicEventOptional = Optional.ofNullable(publicEvent.getPublicEventType());
        return publicEventOptional
            .map(x -> lookup(publicEventTypeEnumStringMap, x, _PublicEventTypeEnum::getValue))
            .map(x -> String.format("Public event: %s", x));
    }

    private Optional<String> accept(final ReroutingManagement reroutingManagement) {
        return reroutingManagement.getReroutingManagementTypes()
            .stream()
            .findFirst()
            .map(x -> lookup(reroutingManagementTypeMap, x, _ReroutingManagementTypeEnum::getValue));
    }

    private Optional<String> accept(final RoadOrCarriagewayOrLaneManagement roadOrCarriagewayOrLaneManagement) {
        final var managementType = roadOrCarriagewayOrLaneManagement.getRoadOrCarriagewayOrLaneManagementType();
        return iceRoadDescription(roadOrCarriagewayOrLaneManagement)
            .or(() -> Optional.ofNullable(lookup(roadOrCarriagewayOrLaneManagementTypeMap, managementType, _RoadOrCarriagewayOrLaneManagementTypeEnum::getValue)));
    }

    /**
     * Datex II 3.7 has no coded value for an ice road, so TLOIK sends {@code other} and states the
     * status only in the Finnish free text. Without this the record would fall back to the generic
     * "Road or carriageway or lane management" description.
     */
    private static Optional<String> iceRoadDescription(final RoadOrCarriagewayOrLaneManagement management) {
        if (WazeDatex2Converter.isIceRoadOpenRecord(management)) {
            return Optional.of("Ice road open");
        }
        if (WazeDatex2Converter.isIceRoadClosedRecord(management)) {
            return Optional.of("Ice road closed");
        }
        return Optional.empty();
    }

    private Optional<String> accept(final SpeedManagement speedManagement) {

        return Optional.ofNullable(speedManagement.getTemporarySpeedLimit())
            .map(Math::round)
            .map(x -> x > 0 ? String.format("Temporary speed limit of %d km/h", x) : null);
    }

    private Optional<String> accept(final TransitInformation transitInformation) {
        final String type = enumName(transitInformation.getTransitServiceType(), _TransitServiceTypeEnum::getValue);
        final String info = enumName(transitInformation.getTransitServiceInformation(), _TransitServiceInformationEnum::getValue);

        if (type == null || info == null) {
            return Optional.empty();
        }

        return Optional.of(StringUtils.capitalize(String.format("%s: %s", humanize(type), humanize(info))));
    }

    private static String humanize(final String enumName) {
        return enumName.toLowerCase().replace("_", " ");
    }

    private Optional<String> accept(final VehicleObstruction vehicleObstruction) {

        // skip obstructingVehicle and numberOfObstructions
        if (unwrap(vehicleObstruction.getVehicleObstructionType(), _VehicleObstructionTypeEnum::getValue)
            == VehicleObstructionTypeEnum.BROKEN_DOWN_VEHICLE) {
            final String byVehicleType =
                lookup(brokenDownVehicleTypeMap, vehicleObstruction.getInvolvedVehicleType(), _VehicleTypeEnum::getValue);
            if (byVehicleType != null) {
                return Optional.of(byVehicleType);
            }
        }

        return Optional.ofNullable(vehicleObstruction.getVehicleObstructionType())
            .map(x -> lookup(vehicleObstructionTypeMap, x, _VehicleObstructionTypeEnum::getValue));
    }

    private Optional<String> accept(final WeatherRelatedRoadConditions weatherRelatedRoadConditions) {
        return weatherRelatedRoadConditions.getWeatherRelatedRoadConditionTypes()
            .stream()
            .findFirst()
            .map(x -> lookup(weatherRelatedRoadConditionTypeMap, x, _WeatherRelatedRoadConditionTypeEnum::getValue));
    }

    private Optional<String> accept(final ConstructionWorks constructionWorks) {
        return Optional.ofNullable(constructionWorks.getConstructionWorkType())
            .map(x -> lookup(constructionWorksTypeMap, x, _ConstructionWorkTypeEnum::getValue));
    }

    private Optional<String> accept(final MaintenanceWorks maintenanceWorks) {
        return maintenanceWorks.getRoadMaintenanceTypes().stream()
            .findFirst()
            .map(x -> lookup(maintenanceWorksTypeMap, x, _RoadMaintenanceTypeEnum::getValue));
    }

    private Optional<Pair<String, String>> accept(final String situationId, final SituationRecord situationRecord) {
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
        } else if (situationRecord instanceof ConstructionWorks) {
            result = accept((ConstructionWorks) situationRecord);
            situationRecordType = "ConstructionWorks";
        } else if (situationRecord instanceof MaintenanceWorks) {
            result = accept((MaintenanceWorks) situationRecord);
            situationRecordType = "MaintenanceWorks";
        } else {
            logger.error("method=accept unknown class {} in {}", situationRecord.getClass().getSimpleName(), situationId);
            return Optional.empty();
        }

        if (result.isEmpty()) {
            logger.error("method=accept unknown {} record in situation {}", situationRecordType, situationId);
        }

        // SKIP_SUBTYPE means, that we want to skip this event subtype, but it's ok and no need to log error
        // (for example OTHER subtype for MaintenanceWorks)
        return result
                .filter(v -> !v.equals(SKIP_SUBTYPE))
                .map(r -> Pair.of(situationRecord.getId(), r));
    }
}
