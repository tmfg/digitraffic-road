package fi.livi.digitraffic.tie.controller;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Road station state", name = "RoadStationState", enumAsRef = true, defaultValue = "ACTIVE")
public enum RoadStationState {
    ALL, REMOVED, ACTIVE
}
