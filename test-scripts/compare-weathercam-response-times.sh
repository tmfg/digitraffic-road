#!/bin/sh

FTP_URL_PROD="http://weathercam.digitraffic.fi/"
FTP_URL_TEST="http://weathercam-test.digitraffic.fi/"
S3_URL="http://weathercam-test.digitraffic.fi/s3/"

fetch_history () {
  echo "Fetching history for preset $1..."
  urls=$(curl -s https://tie-test.digitraffic.fi/api/beta/camera-preset-history/$1 | jq --raw-output '.history[].url')
  echo "...done $1"
  echo "$urls"
}

fetch_image () {
  total_time=$(curl -s -w '%{time_total}' -o /dev/null "$1")
  echo "$total_time"
}

compare_ftp_s3 () {
  ftp_time_prod=$(fetch_image "${FTP_URL_PROD}$1.jpg")
  ftp_time_test=$(fetch_image "${FTP_URL_TEST}$1.jpg")
  s3_time=$(fetch_image "${S3_URL}$1.jpg")
  #printf "%s: ftp vs s3:\t%s\t%s\n" "$1" "$ftp_time" "$s3_time"
  printf "%s\t%s\t%s\t%s\n" "$1" "$ftp_time_prod" "$ftp_time_test" "$s3_time"
}

presetids=$(curl -s https://tie-test.digitraffic.fi/api/v1/metadata/camera-stations | jq --raw-output '.features[].properties.presets[].presetId')

printf "PRESETID\tftp prod [s]\tftp [s]\ts3 [s]\n"
for presetid in $presetids; do
#    echo "Preset id: ${presetid}"
    compare_ftp_s3 ${presetid}
done


#for presetid in $presetids; do
#    echo "Preset id: ${presetid}"
#    history="$(fetch_history "${presetid}")"
#
#done

#/api/beta/camera-preset-history/{presetId}

#curl -sX GET --header 'Accept: application/json' 'https://tie.digitraffic.fi/api/v1/data/tms-data?lastUpdated=false' | jq '[.tmsStations[].sensorValues[]] | sort_by(.name) | group_by(.name) | map({"name": .[0].name, "count": length})'


