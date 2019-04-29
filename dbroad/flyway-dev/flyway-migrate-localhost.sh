#!/bin/sh
cd ../..
mvn flyway:migrate -Dflyway.configFile=dbroad/flyway-dev/flyway-localhost.conf
