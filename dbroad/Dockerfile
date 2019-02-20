# this is used just for waiting postgresql to initialize
FROM postgres:9.6.11

RUN \
apt-get update && \
apt-get -y install \
  maven \
  postgresql-client && \
rm -rf /var/lib/apt/lists/*

COPY files/flyway.conf files/pom.xml files/wait-for-postgres.sh files/init-db.sh /