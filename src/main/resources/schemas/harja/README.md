Schemas copied from skemat https://github.com/finnishtransportagency/harja/tree/develop/resources/api/schemas

To update schema

1. Check out https://github.com/finnishtransportagency/harja
2. Run [copy-harja-schemas.sh](copy-harja-schemas.sh) with parameter pointing to previous harja-directory\
   e.g. ```./copy-harja-schemas.sh ../../../../../../harja```
3. Run test: [MaintenanceTrackingUpdateServiceTest: isAllTaskTypesMapped](./../../../../test/java/fi/livi/digitraffic/tie/service/maintenance/MaintenanceTrackingUpdateServiceTest.java)\
   to see required new task mappings.
