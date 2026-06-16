# Loading Datex II Announcements 3.7 Schema

Go to https://webtool.datex2.eu/wizard/#

1. Source
   * Select: V3.7 DATEX II DATEX II
2. Selection file
   * NONE
3. Profile Selection
    * NONE -> This includes everything in the output
4. Profile Location
   * Location Selection
     * ALL:
       * AreaLocation
       * LinearLocation
       * PointLocation
5. Selection
   * PayloadPublication
      SELECT -> Selecting the top level selects all items under it
     * Deselect all EnergyInfrastructure* items
6. Options
   * Select target PSM
     * XML Schema
   * Generate schema with definitions
   * Save selection to file selection.sel
7. Download generated schema zip file and unzip to this directory.
8. Finish
