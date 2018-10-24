#!/usr/bin/env bash
curl --header "Content-Type: application/json" --request POST --data @seuranta.json http://localhost:9010/api/v1/maintenance/tracking/work_machine
