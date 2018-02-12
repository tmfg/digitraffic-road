# Digitraffic marine database

````bash
cp -r ../../digitraffic-ci-db/src/main/resources/sql/meri_pg/updates/ sql

cp ../../digitraffic-ci-db/src/main/resources/pom.xml .

docker-compose build && docker-compose up
````

PostgreSql db is running at localhost:54321 and adminer localhost:8081