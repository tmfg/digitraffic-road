FROM java:8

MAINTAINER Digitraffic

RUN \
  apt-get update && \
  apt-get -y install \
    maven \
    postgresql-client && \
  rm -rf /var/lib/apt/lists/*

COPY files/flyway.conf /flyway.conf
COPY files/pom.xml /pom.xml
COPY files/wait-for-postgres.sh /wait-for-postgres.sh
COPY files/init-db.sh /init-db.sh
