package fi.livi.digitraffic.tie.helper;

import static fi.livi.digitraffic.tie.datex2.AbnormalTrafficTypeEnum.HEAVY_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.AbnormalTrafficTypeEnum.QUEUING_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.AbnormalTrafficTypeEnum.SLOW_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.AbnormalTrafficTypeEnum.STATIONARY_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.AbnormalTrafficTypeEnum.UNSPECIFIED_ABNORMAL_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.ACCIDENT_INVOLVING_BUSES;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.ACCIDENT_INVOLVING_HAZARDOUS_MATERIALS;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.ACCIDENT_INVOLVING_HEAVY_LORRIES;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.EARLIER_ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.MULTIVEHICLE_ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.OVERTURNED_HEAVY_LORRY;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.REAR_COLLISION;
import static fi.livi.digitraffic.tie.datex2.AccidentTypeEnum.VEHICLE_SPUN_AROUND;
import static fi.livi.digitraffic.tie.datex2.AnimalPresenceTypeEnum.ANIMALS_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.AnimalPresenceTypeEnum.HERD_OF_ANIMALS_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.AnimalPresenceTypeEnum.LARGE_ANIMALS_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.AVALANCHES;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.EARTHQUAKE_DAMAGE;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.FALLEN_TREES;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.FALLING_ICE;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.FALLING_LIGHT_ICE_OR_SNOW;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.FLASH_FLOODS;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.FLOODING;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.FOREST_FIRE;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.GRASS_FIRE;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.LANDSLIPS;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.MUD_SLIDE;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.ROCKFALLS;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.SERIOUS_FIRE;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.SEWER_OVERFLOW;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.SMOKE_OR_FUMES;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.STORM_DAMAGE;
import static fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum.SUBSIDENCE;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum.NOT_WORKING;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum.OUT_OF_SERVICE;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum.WORKING_INCORRECTLY;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum.WORKING_INTERMITTENTLY;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.ANPR_CAMERAS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.AUTOMATED_TOLL_SYSTEM;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.CCTV_CAMERAS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.EMERGENCY_ROADSIDE_TELEPHONES;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.GALLERY_LIGHTS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.LANE_CONTROL_SIGNS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.LEVEL_CROSSING;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.MATRIX_SIGNS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.RAMP_CONTROLS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.ROADSIDE_COMMUNICATIONS_SYSTEM;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.ROADSIDE_POWER_SYSTEM;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.SPEED_CONTROL_SIGNS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.TOLL_GATES;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.TRAFFIC_LIGHT_SETS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.TRAFFIC_SIGNALS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.TUNNEL_LIGHTS;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.TUNNEL_VENTILATION;
import static fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum.VARIABLE_MESSAGE_SIGNS;
import static fi.livi.digitraffic.tie.datex2.ExtendedRoadOrCarriagewayOrLaneManagementTypeEnum.ICE_ROAD_CLOSED;
import static fi.livi.digitraffic.tie.datex2.ExtendedRoadOrCarriagewayOrLaneManagementTypeEnum.ICE_ROAD_OPEN;
import static fi.livi.digitraffic.tie.datex2.GeneralNetworkManagementTypeEnum.BRIDGE_SWING_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.GeneralNetworkManagementTypeEnum.CONVOY_SERVICE;
import static fi.livi.digitraffic.tie.datex2.GeneralNetworkManagementTypeEnum.OBSTACLE_SIGNALLING;
import static fi.livi.digitraffic.tie.datex2.GeneralNetworkManagementTypeEnum.RAMP_METERING_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.GeneralNetworkManagementTypeEnum.TEMPORARY_TRAFFIC_LIGHTS;
import static fi.livi.digitraffic.tie.datex2.GeneralNetworkManagementTypeEnum.TOLL_GATES_OPEN;
import static fi.livi.digitraffic.tie.datex2.GeneralNetworkManagementTypeEnum.TRAFFIC_BEING_MANUALLY_DIRECTED;
import static fi.livi.digitraffic.tie.datex2.GeneralNetworkManagementTypeEnum.TRAFFIC_HELD;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.BURST_PIPE;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.BURST_WATER_MAIN;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.COLLAPSED_SEWER;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.DAMAGED_BRIDGE;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.DAMAGED_CRASH_BARRIER;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.DAMAGED_FLYOVER;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.DAMAGED_GALLERY;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.DAMAGED_GANTRY;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.DAMAGED_ROAD_SURFACE;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.DAMAGED_TUNNEL;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.DAMAGED_VIADUCT;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.FALLEN_POWER_CABLES;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.GAS_LEAK;
import static fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum.WEAK_BRIDGE;
import static fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum.DIESEL_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum.LEAVES_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum.LOOSE_CHIPPINGS;
import static fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum.LOOSE_SAND_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum.MUD_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum.OIL_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum.PETROL_ON_ROAD;
import static fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum.ROAD_SURFACE_IN_POOR_CONDITION;
import static fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum.SLIPPERY_ROAD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.AIR_CRASH;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.CHILDREN_ON_ROADWAY;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.CLEARANCE_WORK;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.CRANE_OPERATING;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.CYCLISTS_ON_ROADWAY;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.DEBRIS;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.EXPLOSION;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.EXPLOSION_HAZARD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.HAZARDS_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.HIGH_SPEED_CHASE;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.HOUSE_FIRE;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.INCIDENT;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.INDUSTRIAL_ACCIDENT;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.OBJECTS_FALLING_FROM_MOVING_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.OBJECT_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.OBSTRUCTION_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.PEOPLE_ON_ROADWAY;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.RAIL_CRASH;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.RECKLESS_DRIVER;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.RESCUE_AND_RECOVERY_WORK;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.SEVERE_FROST_DAMAGED_ROADWAY;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.SHED_LOAD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.SNOW_AND_ICE_DEBRIS;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.SPILLAGE_OCCURRING_FROM_MOVING_VEHICLE;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.SPILLAGE_ON_THE_ROAD;
import static fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum.UNPROTECTED_ACCIDENT_AREA;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.AGRICULTURAL_SHOW;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.AIR_SHOW;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.ATHLETICS_MEETING;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.BALL_GAME;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.BASEBALL_GAME;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.BASKETBALL_GAME;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.BICYCLE_RACE;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.BOAT_RACE;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.BOAT_SHOW;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.BOXING_TOURNAMENT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.BULL_FIGHT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.CEREMONIAL_EVENT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.COMMERCIAL_EVENT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.CONCERT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.CRICKET_MATCH;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.CULTURAL_EVENT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.EXHIBITION;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.FAIR;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.FESTIVAL;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.FILM_TV_MAKING;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.FOOTBALL_MATCH;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.FUNFAIR;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.GARDENING_OR_FLOWER_SHOW;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.GOLF_TOURNAMENT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.HOCKEY_GAME;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.HORSE_RACE_MEETING;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.INTERNATIONAL_SPORTS_MEETING;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.MAJOR_EVENT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.MARATHON;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.MARKET;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.MATCH;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.MOTOR_SHOW;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.MOTOR_SPORT_RACE_MEETING;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.PARADE;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.PROCESSION;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.RACE_MEETING;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.RUGBY_MATCH;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.SEVERAL_MAJOR_EVENTS;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.SHOW;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.SHOW_JUMPING;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.SPORTS_MEETING;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.STATE_OCCASION;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.TENNIS_TOURNAMENT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.TOURNAMENT;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.TRADE_FAIR;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.WATER_SPORTS_MEETING;
import static fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum.WINTER_SPORTS_MEETING;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.DO_NOT_FOLLOW_DIVERSION_SIGNS;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.DO_NOT_USE_ENTRY;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.DO_NOT_USE_EXIT;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.DO_NOT_USE_INTERSECTION_OR_JUNCTION;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.FOLLOW_DIVERSION_SIGNS;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.FOLLOW_LOCAL_DIVERSION;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.FOLLOW_SPECIAL_MARKERS;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.USE_ENTRY;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.USE_EXIT;
import static fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum.USE_INTERSECTION_OR_JUNCTION;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.CARRIAGEWAY_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.CAR_POOL_LANE_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.CLEAR_A_LANE_FOR_EMERGENCY_VEHICLES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.CLEAR_A_LANE_FOR_SNOWPLOUGHS_AND_GRITTING_VEHICLES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.CLOSED_PERMANENTLY_FOR_THE_WINTER;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.CONTRAFLOW;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.DO_NOT_USE_SPECIFIED_LANES_OR_CARRIAGEWAYS;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.HARD_SHOULDER_RUNNING_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.HEIGHT_RESTRICTION_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.INTERMITTENT_SHORT_TERM_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.KEEP_TO_THE_LEFT;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.KEEP_TO_THE_RIGHT;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.LANES_DEVIATED;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.LANE_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.NARROW_LANES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.NEW_ROADWORKS_LAYOUT;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.OVERNIGHT_CLOSURES;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.ROAD_CLEARED;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.ROAD_CLOSED;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.ROLLING_ROAD_BLOCK;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.RUSH_HOUR_LANE_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.SINGLE_ALTERNATE_LINE_TRAFFIC;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.TIDAL_FLOW_LANE_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.TURN_AROUND_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.USE_OF_SPECIFIED_LANES_OR_CARRIAGEWAYS_ALLOWED;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.USE_SPECIFIED_LANES_OR_CARRIAGEWAYS;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.VEHICLE_STORAGE_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum.WEIGHT_RESTRICTION_IN_OPERATION;
import static fi.livi.digitraffic.tie.datex2.TrafficFlowCharacteristicsEnum.ERRATIC_FLOW;
import static fi.livi.digitraffic.tie.datex2.TrafficFlowCharacteristicsEnum.SMOOTH_FLOW;
import static fi.livi.digitraffic.tie.datex2.TrafficFlowCharacteristicsEnum.STOP_AND_GO;
import static fi.livi.digitraffic.tie.datex2.TrafficFlowCharacteristicsEnum.TRAFFIC_BLOCKED;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.stereotype.Component;

import com.sun.xml.ws.util.StringUtils;

import fi.livi.digitraffic.tie.converter.WazeDatex2Converter;
import fi.livi.digitraffic.tie.datex2.AbnormalTraffic;
import fi.livi.digitraffic.tie.datex2.AbnormalTrafficExtensionType;
import fi.livi.digitraffic.tie.datex2.AbnormalTrafficTypeEnum;
import fi.livi.digitraffic.tie.datex2.Accident;
import fi.livi.digitraffic.tie.datex2.AccidentTypeEnum;
import fi.livi.digitraffic.tie.datex2.AnimalPresenceObstruction;
import fi.livi.digitraffic.tie.datex2.AnimalPresenceTypeEnum;
import fi.livi.digitraffic.tie.datex2.AuthorityOperation;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.DisturbanceActivity;
import fi.livi.digitraffic.tie.datex2.EnvironmentalObstruction;
import fi.livi.digitraffic.tie.datex2.EnvironmentalObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFault;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultExtensionType;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFaultTypeEnum;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemTypeEnum;
import fi.livi.digitraffic.tie.datex2.ExtendedEquipmentOrSystemFaultTypeEnum;
import fi.livi.digitraffic.tie.datex2.ExtendedRoadOrCarriagewayOrLaneManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.ExtendedTrafficTrendTypeEnum;
import fi.livi.digitraffic.tie.datex2.GeneralNetworkManagement;
import fi.livi.digitraffic.tie.datex2.GeneralNetworkManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.GeneralObstruction;
import fi.livi.digitraffic.tie.datex2.InfrastructureDamageObstruction;
import fi.livi.digitraffic.tie.datex2.InfrastructureDamageTypeEnum;
import fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditionTypeEnum;
import fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.datex2.ObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.PoorEnvironmentConditions;
import fi.livi.digitraffic.tie.datex2.PoorEnvironmentTypeEnum;
import fi.livi.digitraffic.tie.datex2.PublicEvent;
import fi.livi.digitraffic.tie.datex2.PublicEventTypeEnum;
import fi.livi.digitraffic.tie.datex2.ReroutingManagement;
import fi.livi.digitraffic.tie.datex2.ReroutingManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagement;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementExtensionType;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagementTypeEnum;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.datex2.SpeedManagement;
import fi.livi.digitraffic.tie.datex2.TrafficFlowCharacteristicsEnum;
import fi.livi.digitraffic.tie.datex2.TrafficTrendTypeEnum;
import fi.livi.digitraffic.tie.datex2.TransitInformation;
import fi.livi.digitraffic.tie.datex2.VehicleObstruction;
import fi.livi.digitraffic.tie.datex2.VehicleObstructionTypeEnum;
import fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditionTypeEnum;
import fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;

@Component
public class WazeDatex2MessageConverter {
    private static final Logger logger = LoggerFactory.getLogger(WazeDatex2MessageConverter.class);

    private final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;

    private final Map<AbnormalTrafficTypeEnum, String> abnormalTrafficTypeEnumStringMap = new HashMap<>();
    private final Map<AccidentTypeEnum, String> accidentTypeMap = new HashMap<>();
    private final Map<AnimalPresenceTypeEnum, String> animalPresenceTypeEnumStringMap = new HashMap<>();
    private final Map<EnvironmentalObstructionTypeEnum, String> environmentalObstructionTypeEnumStringMap = new HashMap<>();
    private final Map<EquipmentOrSystemFaultTypeEnum, String> equipmentOrSystemFaultTypeMap = new HashMap<>();
    private final Map<EquipmentOrSystemTypeEnum, String> equipmentOrSystemTypeMap = new HashMap<>();
    private final Map<ExtendedRoadOrCarriagewayOrLaneManagementTypeEnum, String> extendedRoadOrCarriagewayOrLaneManagementTypeMap = new HashMap<>();
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
    private final Map<WeatherRelatedRoadConditionTypeEnum, String> weatherRelatedRoadConditionTypeMap = new HashMap<>();

    @Autowired
    public WazeDatex2MessageConverter(final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller) {
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
        accidentTypeMap.put(ACCIDENT_INVOLVING_BUSES, "Accident involving busses");
        accidentTypeMap.put(ACCIDENT_INVOLVING_HAZARDOUS_MATERIALS, "Accident involving hazardous materials");
        accidentTypeMap.put(ACCIDENT_INVOLVING_HEAVY_LORRIES, "Accident involving heavy lorries");
        accidentTypeMap.put(EARLIER_ACCIDENT, "Earlier accident");
        accidentTypeMap.put(MULTIVEHICLE_ACCIDENT, "Accident involving multiple vehicles");
        accidentTypeMap.put(OVERTURNED_HEAVY_LORRY, "Overturned heavy lorry");
        accidentTypeMap.put(REAR_COLLISION, "Rear collision");
        accidentTypeMap.put(VEHICLE_SPUN_AROUND, "Vehicle spun around");
        accidentTypeMap.put(AccidentTypeEnum.OTHER, "Accident");

        animalPresenceTypeEnumStringMap.put(ANIMALS_ON_THE_ROAD, "Animals on the road");
        animalPresenceTypeEnumStringMap.put(HERD_OF_ANIMALS_ON_THE_ROAD, "Herd of animals on the road");
        animalPresenceTypeEnumStringMap.put(LARGE_ANIMALS_ON_THE_ROAD, "Large animals on the road");

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

        extendedRoadOrCarriagewayOrLaneManagementTypeMap.put(ICE_ROAD_OPEN, "Ice road open");
        extendedRoadOrCarriagewayOrLaneManagementTypeMap.put(ICE_ROAD_CLOSED, "Ice road closed");

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
        obstructionTypeMap.put(HIGH_SPEED_CHASE, "High speed chase");
        obstructionTypeMap.put(HOUSE_FIRE, "House on fire");
        obstructionTypeMap.put(INCIDENT, "Incident on roadway");
        obstructionTypeMap.put(INDUSTRIAL_ACCIDENT, "Industrial accident");
        obstructionTypeMap.put(OBJECT_ON_THE_ROAD, "Object on the road");
        obstructionTypeMap.put(OBJECTS_FALLING_FROM_MOVING_VEHICLE, "Objects falling from moving vehicle");
        obstructionTypeMap.put(OBSTRUCTION_ON_THE_ROAD, "Obstruction on the road");
        obstructionTypeMap.put(PEOPLE_ON_ROADWAY, "People on roadway");
        obstructionTypeMap.put(RAIL_CRASH, "Rail crash");
        obstructionTypeMap.put(RECKLESS_DRIVER, "Recless driver");
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
        publicEventTypeEnumStringMap.put(PublicEventTypeEnum.OTHER, "other");

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
        vehicleObstructionTypeMap.put(BROKEN_DOWN_HEAVY_LORRY, "Broken down heavy lorry");
        vehicleObstructionTypeMap.put(DAMAGED_VEHICLE, "Damaged vehicle");
        vehicleObstructionTypeMap.put(MILITARY_CONVOY, "Military convoy");
        vehicleObstructionTypeMap.put(SLOW_MOVING_MAINTENANCE_VEHICLE, "Slow moving maintenance vehicle");

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
            logger.error("method=export situation {} did not have a proper datex2 message, error: {}", situationId, e.getMessage());
            return "";
        }

        return export(situationId, d2LogicalModel);
    }

    public String export(final String situationId, final D2LogicalModel d2LogicalModel) {
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
            .filter(WazeDatex2Converter::isActiveSituationRecord)
            .flatMap(sr -> accept(situationId, sr).stream())
            .distinct()
            .collect(Collectors.joining(". ", "", "."));
    }

    private Optional<String> accept(final AbnormalTraffic abnormalTraffic) {
        final Optional<String> trafficTrendTypeOptional = Optional.ofNullable(abnormalTraffic.getTrafficTrendType())
            .map(x -> trafficTrendTypeEnumMap.getOrDefault(x, null));

        final Optional<String> abnormalTrafficTypeOptional = Optional.ofNullable(abnormalTraffic.getAbnormalTrafficType())
            .map(x -> abnormalTrafficTypeEnumStringMap.getOrDefault(x, null));

        final Optional<String> extendedTrafficTrendType = Optional.ofNullable(abnormalTraffic.getAbnormalTrafficExtension())
                .map(AbnormalTrafficExtensionType::getTrafficTrendType)
                .map(x -> x.equals(ExtendedTrafficTrendTypeEnum.TRAFFIC_MAY_BUILD_UP) ? "Traffic may build up" : null);

        final Optional<String> trafficFlowCharacteristicsOptional = Optional.ofNullable(abnormalTraffic.getTrafficFlowCharacteristics())
            .map(x -> trafficFlowCharacteristicsEnumStringMap.getOrDefault(x, null));

        return Stream.of(trafficTrendTypeOptional, abnormalTrafficTypeOptional, extendedTrafficTrendType, trafficFlowCharacteristicsOptional)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<String> accept(final Accident accident) {

        return accident.getAccidentTypes()
            .stream()
            .findFirst()
            .map(x -> accidentTypeMap.getOrDefault(x, null));
    }

    private Optional<String> accept(final AnimalPresenceObstruction animalPresenceObstruction) {
        return Optional.of(animalPresenceTypeEnumStringMap.getOrDefault(animalPresenceObstruction.getAnimalPresenceType(), null));
    }

    private Optional<String> accept(final AuthorityOperation authorityOperation) {
        return Optional.empty();
    }
    private Optional<String> accept(final DisturbanceActivity disturbanceActivity) {
        return Optional.empty();
    }
    private Optional<String> accept(final EnvironmentalObstruction environmentalObstruction) {
        final Optional<EnvironmentalObstructionTypeEnum> environmentalObstructionTypeEnumOptional = Optional.ofNullable(environmentalObstruction.getEnvironmentalObstructionType());
        return environmentalObstructionTypeEnumOptional.map(x -> environmentalObstructionTypeEnumStringMap.getOrDefault(x, null));
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
        final Optional<GeneralNetworkManagementTypeEnum> generalNetworkManagementTypeEnumOptional = Optional.ofNullable(generalNetworkManagement.getGeneralNetworkManagementType());

        return generalNetworkManagementTypeEnumOptional
            .map(x -> generalNetworkManagementTypeEnumStringMap.getOrDefault(x, null));
    }

    private Optional<String> accept(final GeneralObstruction generalObstruction) {
        return generalObstruction.getObstructionTypes()
            .stream()
            .findFirst()
            .map(x -> obstructionTypeMap.getOrDefault(x, null));
    }

    private Optional<String> accept(final InfrastructureDamageObstruction infrastructureDamageObstruction) {
        final InfrastructureDamageTypeEnum damageType = infrastructureDamageObstruction.getInfrastructureDamageType();
        return Optional.ofNullable(infrastructureDamageTypeEnumStringMap.getOrDefault(damageType, null));
    }
    private Optional<String> accept(final NonWeatherRelatedRoadConditions nonWeatherRelatedRoadConditions) {
        final String nonweatherRelatedRoadConditionTypes = nonWeatherRelatedRoadConditions.getNonWeatherRelatedRoadConditionTypes().stream()
            .map(x -> nonWeatherRelatedRoadConditionTypeEnumStringMap.getOrDefault(x, null))
            .collect(Collectors.joining(". "));

        if (nonweatherRelatedRoadConditionTypes.equals("")) {
            return Optional.empty();
        }

        return Optional.of(nonweatherRelatedRoadConditionTypes);
    }

    private Optional<String> accept(final PoorEnvironmentConditions poorEnvironmentConditions) {
        final var environmentConditions = poorEnvironmentConditions.getPoorEnvironmentTypes().stream()
            .map(x -> poorEnvironmentTypeEnumStringMap.getOrDefault(x, null))
            .filter(Objects::nonNull)
            .collect(Collectors.joining(". "));

        return Optional.of(environmentConditions);
    }

    private Optional<String> accept(final PublicEvent publicEvent) {
        final Optional<PublicEventTypeEnum> publicEventOptional = Optional.ofNullable(publicEvent.getPublicEventType());
        return publicEventOptional
            .map(x -> publicEventTypeEnumStringMap.getOrDefault(x, null))
            .map(x -> String.format("Public event: %s", x));
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
            .map(StringUtils::capitalize);
    }

    private Optional<String> accept(final VehicleObstruction vehicleObstruction) {

        // skip obstructingVehicle and numberOfObstructions
        return Optional.ofNullable(vehicleObstruction.getVehicleObstructionType())
            .map(x -> vehicleObstructionTypeMap.getOrDefault(x, null));
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