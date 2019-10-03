#!/bin/bash

FTP_URL_PROD="http://weathercam.digitraffic.fi/"
FTP_URL_TEST="http://weathercam-test.digitraffic.fi/"
S3_URL_TEST="http://weathercam-test.digitraffic.fi/s3/"
S3_URL_PROD="http://weathercam.digitraffic.fi/s3/"

test_exif () {
  curl -s "${FTP_URL_TEST}$1.jpg" > exif-test.jpg
  result=$(jhead exif-test.jpg | grep "Camera")
  if [[ ! -z $result ]];then
    echo "1"
    return;
  fi

  echo "0"
}

test_exif_s3 () {
  curl -s "${S3_URL_TEST}$1.jpg" > exif-test.jpg
  result=$(jhead exif-test.jpg | grep "Camera")
  if [[ ! -z $result ]];then
    echo "1"
    return;
  fi

  echo "0"
}

echo "Test FTP and S3 weathercam images exif existense"

# From newest to oldest
presetsZtoA=$(curl -k -s https://tie-test.digitraffic.fi/api/v1/data/camera-data | jq --raw-output '[.cameraStations[].cameraPresets[]] | sort_by(.measuredTime) | reverse | .[] | .id + " " + .measuredTime')


count=0
while read -r preset presetDate; do

  result=$(test_exif "${preset}")
  echo "EXIF result: $result ${preset} FTP"
  if [ $result -gt 0 ];then
    echo "ERROR: Preset ${preset} at FTP has Exif metadata: ${FTP_URL_TEST}${preset}.jpg"
    exit 1;
  fi

  result=$(test_exif_s3 "${preset}")
  echo "EXIF result: $result ${preset} S3"
  if [ $result -gt 0 ];then
    echo "ERROR: Preset ${preset} at S3 has Exif metadata: ${S3_URL_TEST}${preset}.jpg"
      exit 1;
  fi

done <<< "${presetsZtoA}"

exit 0;