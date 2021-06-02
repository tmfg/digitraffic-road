UPDATE region_geometry
SET type =
    case
        when type = '0' then 'MUNICIPALITY'
        when type = '1' then 'PROVINCE'
        when type = '2' then 'REGIONAL_STATE_ADMINISTRATIVE_AGENCY'
        when type = '3' then 'WEATHER_REGION'
        when type = '4' then 'COUNTRY'
        when type = '5' then 'CITY_REGION'
        when type = '6' then 'TRAVEL_REGION'
        else 'UNKNOWN'
    end;

ALTER TABLE region_geometry
ADD CONSTRAINT region_geometry_type_check CHECK (
    type IN (
        'MUNICIPALITY',
        'PROVINCE',
        'REGIONAL_STATE_ADMINISTRATIVE_AGENCY',
        'WEATHER_REGION',
        'COUNTRY',
        'CITY_REGION',
        'TRAVEL_REGION',
        'UNKNOWN'
    )
);
