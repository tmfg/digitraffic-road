FROM debian:8.11-slim

# copy config and repo keys and all
COPY config /mqtt/config

# install mosquitto repo and install mosquitto
RUN apt-key add /mqtt/config/mosquitto-repo.gpg.key && \
    cp /mqtt/config/mosquitto-jessie.list /etc/apt/sources.list.d/mosquitto-jessie.list && \
    apt-get update && \
    apt-get -y install mosquitto=1.4.15-0mosquitto4~jessie1 && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /mqtt/config /mqtt/data /mqtt/log

VOLUME ["/mqtt/config", "/mqtt/data", "/mqtt/log"]

EXPOSE 1883 9001

ENTRYPOINT ["/usr/sbin/mosquitto", "-c", "/mqtt/config/mosquitto.conf"]