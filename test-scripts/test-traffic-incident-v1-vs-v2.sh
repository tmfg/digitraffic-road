#!/bin/bash

echo "Test traffic-incident API v1 v2"
echo
V1_URL="https://tie-test.digitraffic.fi/api/v1/data/traffic-disorders-datex2?inactiveHours=0"
V2_URL="https://tie.digitraffic.fi/api/v1/data/traffic-disorders-datex2?inactiveHours=0"
#V1_URL="https://tie-test.digitraffic.fi/api/v1/data/traffic-disorders-datex2?inactiveHours=0"
#V2_URL="https://tie-test.digitraffic.fi/api/beta/traffic-datex2/traffic-incident.xml?inactiveHours=0"
echo "V1_URL=$V1_URL"
echo "V2_URL=$V2_URL"
echo
v1=$(curl -k -s "${V1_URL}" | grep 'situation id=')
v2=$(curl -k -s "${V1_URL}" | grep 'situation id=')

FAIL=false
echo
for i1 in ${v1//' '/}
do
    # call your procedure/other scripts here below
    i1=${i1//'ns2:'/}
    echo "Test for v1 $i1 presence in v2"
    FOUND=false;
    for i2 in ${v2//' '/}
    do
        if [ "$i1" == "$i2" ]; then
            #echo "Strings are equal $i2"
            FOUND=true;
        fi
    done

    if [[ $FOUND == false ]]; then
        echo "$i1 not found"
        FAIL=false
    else
        echo "$i1 found in both apis"
    fi
    echo
done

if [[ $FAIL == true ]]; then
    echo "Test failed"
    exit 1;
else
    echo "Test success"
    exit 0;
fi

exit;

diff <(curl -s --compressed -H "digitraffic-user: digitraffic-test" "https://tie-test.digitraffic.fi/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=EXEMPTED_TRANSPORT") \
<(curl -s "http://localhost:9010/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=EXEMPTED_TRANSPORT")

diff <(curl -s --compressed -H "digitraffic-user: digitraffic-test" "https://tie-test.digitraffic.fi/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=TRAFFIC_ANNOUNCEMENT") \
<(curl -s "http://localhost:9010/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=TRAFFIC_ANNOUNCEMENT")

diff <(curl -s --compressed -H "digitraffic-user: digitraffic-test" "https://tie-test.digitraffic.fi/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=ROAD_WORK") \
<(curl -s "http://localhost:9010/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=ROAD_WORK")

diff <(curl -s --compressed -H "digitraffic-user: digitraffic-test" "https://tie-test.digitraffic.fi/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=WEIGHT_RESTRICTION") \
<(curl -s "http://localhost:9010/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=WEIGHT_RESTRICTION")

# Uudet rajapinnat
diff <(curl -s --compressed -H "digitraffic-user: digitraffic-test" "https://tie-test.digitraffic.fi/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=EXEMPTED_TRANSPORT") \
<(curl -s "http://localhost:9010/api/traffic-messages/v1/simple?inactiveHours=0&includeAreaGeometry=false&situationType=EXEMPTED_TRANSPORT")

diff <(curl -s --compressed -H "digitraffic-user: digitraffic-test" "https://tie-test.digitraffic.fi/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=TRAFFIC_ANNOUNCEMENT") \
<(curl -s "http://localhost:9010/api/traffic-messages/v1/simple?inactiveHours=0&includeAreaGeometry=false&situationType=TRAFFIC_ANNOUNCEMENT")

diff <(curl -s --compressed -H "digitraffic-user: digitraffic-test" "https://tie-test.digitraffic.fi/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=ROAD_WORK") \
<(curl -s "http://localhost:9010/api/traffic-messages/v1/simple?inactiveHours=0&includeAreaGeometry=false&situationType=ROAD_WORK")

diff <(curl -s --compressed -H "digitraffic-user: digitraffic-test" "https://tie-test.digitraffic.fi/api/v3/data/traffic-messages/simple?inactiveHours=0&includeAreaGeometry=false&situationType=WEIGHT_RESTRICTION") \
<(curl -s "http://localhost:9010/api/traffic-messages/v1/simple?inactiveHours=0&includeAreaGeometry=false&situationType=WEIGHT_RESTRICTION")
