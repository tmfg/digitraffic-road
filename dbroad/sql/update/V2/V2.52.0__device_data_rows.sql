-- ok, first add missing primary key to DEVICE_DATA
alter table DEVICE_DATA add primary key (ID);

create table DEVICE_DATA_ROW (
    ID              serial primary key,
    DEVICE_DATA_ID  integer not null,
    SCREEN          numeric(8,0) not null,
    ROW_NUMBER      numeric(4,0) not null,
    TEXT            text
);

alter table DEVICE_DATA_ROW add foreign key (DEVICE_DATA_ID) references DEVICE_DATA(ID);

create index DEVICE_DATA_ROW_DEVICE_DATA_FKEY on DEVICE_DATA_ROW(DEVICE_DATA_ID);