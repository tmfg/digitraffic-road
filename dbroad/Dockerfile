FROM java:8

MAINTAINER Digitraffic

RUN \
  apt-get update && \
  apt-get -y install \
    maven \
    postgresql-client && \
  rm -rf /var/lib/apt/lists/*

COPY flyway.conf /flyway.conf
COPY pom.xml /pom.xml
COPY wait-for-postgres.sh /wait-for-postgres.sh
COPY init-db.sh /init-db.sh
