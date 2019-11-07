#!/bin/sh

printf "NOW\tFORECA\tAPI\tDB\n"

while true; do
  NOW=$(TZ="UTC" gdate --iso-8601=seconds)
  FORECA=$(curl -s http://keli.foreca.fi/digitraffic/roadConditionsV1-json.php | jq -r '.messageTimestamp')
  # --iso-8601=seconds
  FORECA=$(echo ${FORECA} | TZ="UTC" xargs gdate +%Y-%m-%dT%TZ -d)
  #echo '2019-11-07T12:31:07+02:00' | TZ="UTC" xargs gdate -d
  API=$(curl -s https://tie-test.digitraffic.fi/api/v1/data/road-conditions?lastUpdated=true | jq -r '.dataUpdatedTime')
  # Prod port 54324
  DB=$(psql -h localhost -p 54323 -U road  -c "select to_char(updated, 'YYYY-MM-DD\"T\"HH24:MI:SSZ') from data_updated where data_type = 'FORECAST_SECTION_WEATHER_DATA'" -t | grep T | cut -d ' ' -f2)

  #echo ${NOW} ${FORECA} ${API} ${DB}

  printf "%s\t%s\t%s\t%s\n" "${NOW}" "${FORECA}" "${API}" "${DB}"

  sleep 10;
done

