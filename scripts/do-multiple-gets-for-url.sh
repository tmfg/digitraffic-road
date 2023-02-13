#!/bin/sh
BASEDIR=$(cd $(dirname $0); /bin/pwd)

URL=${1:?"Url is required parameter"}
echo $URL
for i in {1..4};
do
    NOW=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
    echo "do   ${i} ${NOW}"
    curl "${URL}" \
      -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9' \
      -H 'Accept-Language: en-US,en;q=0.9,fi;q=0.8' \
      -H 'Cache-Control: max-age=0' \
      -H "Digitraffic-User: internal-jouni-${i}-${NOW}" \
      -H 'Connection: keep-alive' \
      -H 'Cookie: _ga=GA1.2.386561827.1661422057; _gid=GA1.2.40830554.1672212894' \
      -H 'If-Modified-Since: Wed, 28 Dec 2022 13:17:29 GMT' \
      -H 'If-None-Match: W/"0a2c82d07787b4df3702ed228c5b5f75f"' \
      -H 'Upgrade-Insecure-Requests: 1' \
      -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36' \
      --compressed \
      --insecure \
      -s \
      -o "vastaus-${i}.json" &
      echo "done ${i} $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
done