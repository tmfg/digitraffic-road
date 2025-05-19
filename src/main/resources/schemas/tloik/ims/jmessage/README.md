# How to update JSON Schema

1. Load new schema to `JSONMessage.json.original` and reformat JSON (Mac: Opt+CMD+L)
2. Compare content to previous version with git (Context-menu -> Git -> Show Diff) and
3. Take note: what has updated ie.

        "slowTrafficTimes": {
            "type": "array",
            "items": {
                "$ref": "#/definitions/weekdayTimePeriod"
            },
            "description": "Time periods when the road work is expected to cause slow moving traffic."
        },
        "queuingTrafficTimes": {
            "type": "array",
            "items": {
                "$ref": "#/definitions/weekdayTimePeriod"
            },
            "description": "Time periods when the road work is expected to cause queuing of the traffic."
        },
4. Copy contents of `JSONMessage.json.original` to `JSONMessage.json.original`
5. Compare content to previous version with git (Context-menu -> Git -> Show Diff) and
6. Add some additional lines from previous version:
   * `"javaType": "fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties",` as third line
   * `"city region", "travel region"` values to area-location item type enum
7. Run tests `mvn clean install` andf ix possible compilation problems (ie. ImsJsonMessageTestFactory might need some tune)
8. If test are fine it might bee good idea to make commit of the changes
9. Copy previous version `src/test/resources/tloik/ims/versions/{previous-versio}` -directory and paste as new version
10. Update copied JSONs with new data from schema changes ie. `ROAD_WORK.json` add:

        "slowTrafficTimes": [
            {
                "weekday": "Tuesday",
                "startTime": "09:30:00.000",
                "endTime": "15:00:00.000"
            }
        ],
        "queuingTrafficTimes": [
            {
                "weekday": "Wednesday",
                "startTime": "09:30:00.000",
                "endTime": "15:00:00.000"
            }
        ],
11. Add ned data also to `ImsJsonMessageTestFactory` when creating test objects
12. Modify/add changes also to API-objects at src/main/java/fi/livi/digitraffic/tie/dto/trafficmessage/v1
or if the changes are breaking then create new version. Remember to add new fields also to constructor.
Ie. `RoadWorkPhase.java`:

            @ApiModelProperty(value = "Time periods when the road work is expected to cause slow moving traffic.")
            public List<WorkingHour> slowTrafficTimes = new ArrayList<>();

            @ApiModelProperty(value = "Time periods when the road work is expected to cause queuing of the traffic.")
            public List<WorkingHour> queuingTrafficTimes = new ArrayList<>();

            public RoadWorkPhase(..., final List<WorkingHour> slowTrafficTimes, final List<WorkingHour> queuingTrafficTimes,...) {

    You can look/copy base for the changes from generated sources at `target/generated-sources/json/fi/livi/digitraffic/tie/external/tloik/ims/jmessage`
13. Add new version to `TrafficMessageTestHelper$ImsJsonVersion` ie. `V0_2_17(2.17, 217);`
14. Modify test `TrafficMessageJsonConverterTest_V1.java` to check new fields ie.

            private void assertRoadWorkPhases(final TrafficAnnouncement announcement,
                                              final ImsJsonVersion version) {
            ...
                if (version.version >= 2.17) {
                    assertEquals(WorkingHour.Weekday.TUESDAY, rwp.slowTrafficTimes.getFirst().weekday);
                    assertEquals(WorkingHour.Weekday.WEDNESDAY, rwp.queuingTrafficTimes.getFirst().weekday);
                    assertNotNull(rwp.slowTrafficTimes.getFirst().startTime);
                    assertNotNull(rwp.slowTrafficTimes.getFirst().endTime);
                    assertNotNull(rwp.queuingTrafficTimes.getFirst().startTime);
                    assertNotNull(rwp.queuingTrafficTimes.getFirst().endTime);
                }
            }
15. Modify test `TrafficMessagesControllerV1Test.java` to check new values ie.

            private void assertContentsMatch(...) {
            ...
                if (imsJsonVersion.version >= 2.17 && situationType.equals(SituationType.ROAD_WORK)) {
                    final RoadWorkPhase rwp =
                        feature.getProperties().announcements.getFirst().roadWorkPhases.getFirst();
                    assertEquals(WorkingHour.Weekday.TUESDAY, rwp.slowTrafficTimes.getFirst().weekday);
                    assertEquals(WorkingHour.Weekday.WEDNESDAY, rwp.queuingTrafficTimes.getFirst().weekday);
                }
            }
16. Run tests, commit, squash commits and push
