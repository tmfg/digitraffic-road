#!/bin/bash

FTP_URL_PROD="http://weathercam.digitraffic.fi/"
FTP_URL_TEST="http://weathercam-test.digitraffic.fi/"
S3_URL_TEST="http://weathercam-test.digitraffic.fi/s3/"
S3_URL_PROD="http://weathercam.digitraffic.fi/s3/"

diff_ftp_s3 () {
  result=$(diff <(curl -s "${FTP_URL_TEST}$1.jpg") <(curl -s "${S3_URL_TEST}$1.jpg"))
  if [[ ! -z $result ]];then
    echo "1"
  else
    echo "0"
  fi
}

echo "Test FTP vs S3 weathercam images equality"

# From oldest to newest
presetsAtoZ=$(curl -k -s https://tie-test.digitraffic.fi/api/v1/data/camera-data | jq --raw-output '[.cameraStations[].cameraPresets[]] | sort_by(.measuredTime) | .[] | .id + " " + .measuredTime')
# From newest to oldest
presetsZtoA=$(curl -k -s https://tie-test.digitraffic.fi/api/v1/data/camera-data | jq --raw-output '[.cameraStations[].cameraPresets[]] | sort_by(.measuredTime) | reverse | .[] | .id + " " + .measuredTime')

# Limit age to be older than 1 minute to resolve cache issues
unameOut="$(uname -s)"
case "${unameOut}" in
    Darwin*)    LIMIT=$(date -u -v-1M +%Y-%m-%dT%H:%M:%SZ);;
    *)          LIMIT=$(date -u --date='1 minutes ago' +"%Y-%m-%dT%TZ")
esac

echo "Limit to older than ${LIMIT} to resolve cache issues for just updated images (cache time 1 m)"

count=0
while read -r preset presetDate; do
  if [[ ! -z $presetDate ]];then
    if [[ ${presetDate} < ${LIMIT} ]];then
      (( count++ ))

      result=$(diff_ftp_s3 "${preset}")
      echo "DIFF result: $result ${preset} $presetDate > ${LIMIT}"
      if [ $result -gt 0 ];then
        echo "ERROR: Preset ${preset} diffs between ftp and S3"
        exit 1;
      fi
      if [ ${count} -ge 20 ];then
        break;
      fi
    else
      echo "SKIPP age under 1m: ${preset} $presetDate > ${LIMIT}"
    fi
  fi
#echo "\"$presetDate\" $preset"
done <<< "${presetsZtoA}"

exit 0;