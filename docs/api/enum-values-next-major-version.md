# Enum values to correct in the next major API version

Changing the spelling of an enum value breaks every client that compares against it, so
none of the items below can be fixed in a released version. They are collected here so
that they can be done together the next time a major version is opened.

Everything listed was checked against the document actually published at
`https://tie.digitraffic.fi/swagger/openapi.json`, not only against the source, so every
item is something a client can see in production today.

## The convention

Enum values are written in `UPPER_SNAKE_CASE`. This follows Zalando's REST guideline
[#240](https://opensource.zalando.com/restful-api-guidelines/#240) and matches what the
rest of the road API already does.

There is one exception, also from the same guideline: when the value is defined outside
our own API, it keeps the spelling the outside definition gives it. Examples in this API:

- language codes are ISO 639-1, so `fi` in lower case is correct
- GeoJSON member names are RFC 7946, so `Feature`, `FeatureCollection`, `Point` are correct
- time intervals are ISO 8601 durations, so `PT15M` and `P1D` are correct
- country codes are ISO 3166-1, so `FI` and `SE` are correct

The same goes for the values of paging and sorting parameters, which are conventionally
written in lower case (`?sort=name,asc`). This API has no such parameters today; its only
enum query parameter is `taskId` on the maintenance tracking routes, which is already
written the right way.

An external spelling used *inside* our own message is not automatically an exception. The
traffic message values below are copied from the schema TLOIK sends us, but the shape of
our own responses is still ours to decide. The v1 endpoints already prove the point: they
carry the same data and publish it in `UPPER_SNAKE_CASE`.

## 1. Traffic message v2 publishes the source system's spelling

This is the largest item. The v1 and v2 traffic message endpoints describe the same things
with the same Java enums, but they serialize them differently.

The v1 classes return the constant name:

```java
// dto/trafficmessage/v1/Restriction.java:102
@JsonValue
public String value() {
    return this.name();
}
```

The v2 classes return the string the source system uses:

```java
// dto/trafficmessage/v2/Restriction.java:102
@JsonValue
public String value() {
    return this.value;
}
```

So the same concept is published twice with two spellings:

| Schema | v1 publishes | v2 publishes |
|---|---|---|
| `SituationType` | `TRAFFIC_ANNOUNCEMENT` | `traffic announcement` |
| `Restriction.type` | `SPEED_LIMIT` | `speed limit` |
| `WorkType.type` | `ROAD_CONSTRUCTION` | `road construction` |
| `TrafficAnnouncementProperties.trafficAnnouncementType` | `PRELIMINARY_ACCIDENT_REPORT` | `preliminary accident report` |
| `AreaType` | `REGIONAL_STATE_ADMINISTRATIVE_AGENCY` | `regional state administrative agency` |
| `RoadWorkPhase.severity` | `LOW`, `HIGH`, `HIGHEST` | `low`, `high`, `highest` |
| `RoadAddressLocation.direction` | `UNKNOWN`, `POS`, `NEG`, `BOTH` | `unknown`, `pos`, `neg`, `both` |
| `TrafficAnnouncement.earlyClosing` | `CLOSED`, `CANCELED` | `closed`, `canceled` |
| `WeekdayTimePeriod.weekday` | `MONDAY` | `Monday` |

The v2 classes that need changing, all under
`src/main/java/fi/livi/digitraffic/tie/dto/trafficmessage/v2/`:

| File | Line | Returns |
|---|---|---|
| `AreaType.java` | 45 | `value` |
| `Restriction.java` | 102 | `this.value` |
| `RoadAddressLocation.java` | 61 | `value` |
| `RoadWorkPhase.java` | 114 | `value` |
| `SituationType.java` | 37 | `value` |
| `TrafficAnnouncement.java` | 122 | `value` |
| `TrafficAnnouncementType.java` | 34 | `value` |
| `WeekdayTimePeriod.java` | 104 | `this.values[0]` |
| `WorkType.java` | 81 | `value` |

`TrafficAnnouncement.java:122` is the language field. Its v2 value `fi` is correct and
should stay lower case, because it is an ISO 639-1 code. The v1 spelling `FI` is the one
that does not match its own description, which says the field is "a subset of ISO 639-1".

### 1.1 One value in `AreaType` is already written the other way

```java
// dto/trafficmessage/v2/AreaType.java:15-23
MUNICIPALITY("municipality"),
...
METROPOLITAN_AREA("metropolitan area"),
UNKNOWN("UNKNOWN");
```

Eight values are lower case with spaces and the ninth is upper case. A client reading
`AreaTypeV2` has to handle both styles in one field.

### 1.2 The same situation type is spelled two ways depending on the channel

The situation type is stored as the constant name:

```java
// model/data/DataDatex2Situation.java:31
@Enumerated(EnumType.STRING)
private TrafficAnnouncementProperties.SituationType situationType;
```

The MQTT topic is built from that stored column, so a subscriber sees
`traffic-message-v3/simple/TRAFFIC_ANNOUNCEMENT`, while the JSON body delivered on that
very topic says `"situationType": "traffic announcement"`. Whatever spelling is chosen,
the topic and the body should agree.

### 1.3 Input accepts spellings that are never published

Several enums accept old values through `@JsonCreator` that the API has never published:
`SituationType` accepts `special transport` as well as `exempted transport`, and
`WeekdayTimePeriod.Weekday` accepts the Finnish abbreviations `ma`, `ti` and so on. These
can be dropped at the same time.

## 2. Misspelled values that are published today

Two of these are our own invention and one is not, which decides how each can be fixed.

| Value published | Should be | Declared at | Seen in |
|---|---|---|---|
| `GARBAGE_OLLECTION` | `GARBAGE_COLLECTION` | `model/maintenance/MaintenanceTrackingTask.java:99` | maintenance tracking responses, the `/tasks` list, the `tasks` query parameter, and the MQTT maintenance tracking messages |
| `NARROW_OR_CLOSED_PEDESTRIAN_AND_BICYLE_PATH` | `..._BICYCLE_PATH` | `dto/trafficmessage/v1/Restriction.java:68`, same line in `v2` | `RestrictionV1`, and as `narrow or closed pedestrian and bicyle path` in `RestrictionV2` |
| `SPEEDLIMIT` | `SPEED_LIMIT` | `dto/variablesigns/v1/VariableSignPropertiesV1.java:89` | `VariableSignPropertiesV1.type` |

**`GARBAGE_OLLECTION` is ours alone.** Harja sends `ROSKIEN_KERUU`, and the incoming value
is matched on that, not on our constant name:

```java
// model/maintenance/MaintenanceTrackingTask.java:114
this.harjaEnumName = harjaEnum != null ? harjaEnum.name() : null;
```

So reading Harja's data is not affected and renaming the constant changes only what we
publish. The English description beside it already says "Garbage collection", so the value
is the only thing that is wrong. It appears four times in the published document.

**`SPEEDLIMIT` is also ours.** The source system sends `NOPEUSRAJOITUS`, which is matched
separately, so the published spelling is free to change. `SPEED_LIMIT` is already used in
the traffic message schemas, so the variable sign API is the only place that fuses the two
words.

**The `bicyle` typo is not ours.** It is in the schema TLOIK sends us:

```
// src/main/resources/schemas/tloik/ims/jmessage/JSONMessage.json:547
"narrow or closed pedestrian and bicyle path",
```

The same file spells the word correctly on line 488 for a different value, so this is a
mistake in that one value rather than a convention. Incoming messages carry the misspelled
string and will keep doing so until TLOIK corrects the schema, so we have to go on
accepting it. What we publish is a separate decision: once the v2 values are changed to
`UPPER_SNAKE_CASE` as described above, the published value stops being a copy of the
incoming string and can be spelled correctly. Worth reporting to TLOIK as well.

## 3. Constants whose name differs from their stored value

This is the shape of the problem that was fixed for the Datex II message type: a constant
was named `DATEX_2` while the value in the schema was `DATEX2`, so the same thing was
written to the database two different ways depending on which method the calling code
happened to use.

The remaining cases are not broken today, but they are built the same way.

| Constant | Value | Declared at |
|---|---|---|
| `E_18` | `E18` | `model/tms/TmsStationType.java:11` |
| `E_18` | `E18` | `model/weather/WeatherStationType.java:9` |
| `DSL_4_L` | `DSL_4L` | `model/tms/CalculatorDeviceType.java:8` |
| `DSL_4_G` | `DSL_4G` | `model/tms/CalculatorDeviceType.java:9` |
| `OTHER` | `MUU` | `model/tms/CalculatorDeviceType.java:11` |

The published document contains `E_18` and never `E18`, because nothing marks the value
method as the one Jackson should use. The value methods exist for reading the source
system's data. Keeping both a name and a differently spelled value on the same constant
invites someone to publish the wrong one later, so the two uses are worth separating.

The generated classes under `target/generated-sources` carry many more constants where the
name and the schema value differ, but those are differences in capitalisation coming from
the Datex II standard, which is what that standard asks for.

## 4. Counting site (a different repository)

The counting site endpoints under `/api/counting-site/v2/` are served by a CDK project,
not by this repository, so these have to be fixed there.

- `SiteModel.travelModes` is described as `"Removal timestamp"` and typed as a `date-time`
  string. The description belongs to another field, and the property name suggests the
  field should hold travel modes.
- `/directions`, `/travel-modes` and `/domains` each return an array of plain strings with
  no list of allowed values. These are enums in practice, and a client cannot tell from
  the document what may appear in them, nor whether the spelling follows the convention
  above.
- `SiteModel.geometry` is declared as a bare object, so the geometry is not described at
  all, unlike the geometry types in this repository.
- `JsonDataResponseModel.status` is a plain number described only as "Counter status", with
  no explanation of what the numbers mean. If the set of numbers is fixed, it is an enum
  and should be published as one.

The GeoJSON values `Feature` and `FeatureCollection` and the ISO 8601 durations in
`granularity` are correct as they are.

## 5. Checked and deliberately left alone

These came up while going through the published document and need no change.

- All v1 schemas outside the items above already publish `UPPER_SNAKE_CASE`.
- Waze feed values follow the feed specification Waze publishes and must keep that
  spelling.
- Datex II exchange and subscription values come from the Datex II standard.
- Road station, weathercam, forecast section and variable sign values are already written
  the right way, apart from `SPEEDLIMIT` above.
- Enum fields stored in the database all use `@Enumerated(EnumType.STRING)`, which writes
  the constant name as text. The alternative, `EnumType.ORDINAL`, stores the position of
  the constant in the declaration instead, so inserting a value in the middle of an enum
  silently changes the meaning of every row already written. Nothing in this repository
  uses that, and no enum field is left without the annotation, which would default to it.
