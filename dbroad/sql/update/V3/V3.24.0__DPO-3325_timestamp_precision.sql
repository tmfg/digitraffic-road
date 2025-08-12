alter table DEVICE alter column MODIFIED type timestamp(2) with time zone;
alter table DEVICE alter column DELETED_DATE type timestamp(2) with time zone;
alter table DEVICE alter column CREATED type timestamp(2) with time zone;

alter table DEVICE_DATA alter column CREATED type timestamp(2) with time zone;
alter table DEVICE_DATA alter column EFFECT_DATE type timestamp(2) with time zone;
alter table DEVICE_DATA alter column MODIFIED type timestamp(2) with time zone;

alter table DEVICE_DATA_DATEX2 alter column CREATED type timestamp(2) with time zone;
alter table DEVICE_DATA_DATEX2 alter column EFFECT_DATE type timestamp(2) with time zone;
alter table DEVICE_DATA_DATEX2 alter column MODIFIED type timestamp(2) with time zone;
