# this is used just for waiting postgresql to initialize
FROM postgres:12.4

RUN \
apt-get update && \
apt-get -y install \
  maven && \
rm -rf /var/lib/apt/lists/*

COPY files/flyway.conf files/pom.xml files/init-db.sh /