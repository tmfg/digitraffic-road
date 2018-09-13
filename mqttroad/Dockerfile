FROM debian:9.5-slim

# copy config and repo keys and all
COPY config /mqtt/config

RUN apt-get update && apt-get -y install gnupg

# install mosquitto repo and install mosquitto
RUN apt-key add /mqtt/config/mosquitto-repo.gpg.key && \
    cp /mqtt/config/mosquitto-stretch.list /etc/apt/sources.list.d/mosquitto-stretch.list && \
    apt-get update && \
#    apt-cache madison mosquitto && \
    apt-get -y install mosquitto=1.4.15-0mosquitto4 && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /mqtt/config /mqtt/data /mqtt/log

VOLUME ["/mqtt/config", "/mqtt/data", "/mqtt/log"]

EXPOSE 1883 9001

ENTRYPOINT ["/usr/sbin/mosquitto", "-c", "/mqtt/config/mosquitto.conf"]