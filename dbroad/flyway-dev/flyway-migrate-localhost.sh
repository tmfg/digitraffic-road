#!/bin/sh
cd ../..
mvn flyway:repair flyway:migrate -Dflyway.configFile=dbroad/flyway-dev/flyway-localhost.conf
