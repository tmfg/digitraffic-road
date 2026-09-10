Schemas loaded from https://webtool.datex2.eu/wizard/# with options:

1. Source
   * V3.7 DATEX II DATEX II Situation, RoadTrafficData, Vms, EnergyInfrastructure, Facilities, Parking, TrafficManagementPlan, EnhancedRerouting and FaultAndStatus

2. Selection file
   * None

3. Profile Selection
   * B: RTTI: Delegated Regulation (EU) 962/2015
     * Road Traffic Data
       * Traffic Speed
       * Traffic Volume

4. Profile Location
   * PointLocation
     * Point by coordinates

5. Selection
   * PayloadPublication
     * ElaboratedDataPublication
     * MeasuredDataPublication
     * MeasurementSiteTablePublication

6. Options
   * Select target PSM
     * XML Schema
   * Generate schema with definitions
   * Save selection to file selection.sel
7. Download generated schema zip file and unzip to this directory.
8. Manually add `DATEXII_3_OpenLrBinary.xsd` (not available from the wizard — must be generated from the Enterprise Architect UML model).
9. Add `xmlns:olrb="http://datex2.eu/schema/3/openLrBinary"` namespace declaration and `<xs:import namespace="http://datex2.eu/schema/3/openLrBinary" schemaLocation="DATEXII_3_OpenLrBinary.xsd" />` to `DATEXII_3_D2Payload.xsd`.
10. Finish

All schemas also available at https://docs.datex2.eu/downloads/modelv37/#datex-ii-xml-schema
