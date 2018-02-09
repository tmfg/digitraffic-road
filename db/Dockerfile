FROM java:8

MAINTAINER Digitraffic

RUN \
  apt-get update && \
  apt-get -y install \
    maven && \
  rm -rf /var/lib/apt/lists/*

COPY flyway.conf /flyway.conf
COPY pom.xml /pom.xml
VOLUME /root/.m2
ENTRYPOINT ["mvn", "-B", "flyway:migrate"]
ENTRYPOINT ["mvn", "-B", "-Dflyway.configFiles=flyway.conf", "flyway:migrate"]