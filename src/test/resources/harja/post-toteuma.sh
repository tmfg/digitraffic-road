#!/usr/bin/env bash
curl -i --header "Content-Type: application/json" --request POST --data @controller/toteuma.json http://localhost:9010/api/integrations/work-machine/v2/realizations
