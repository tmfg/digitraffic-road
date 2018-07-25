FROM toke/mosquitto

COPY config/mosquitto.conf /mqtt/config/mosquitto.conf
COPY config/mosquitto.passwd /mqtt/config/mosquitto.passwd
COPY config/mosquitto.acl /mqtt/config/mosquitto.acl

# Passwords below are trivial and superceded by other security mechanisms.
# See DPO-497 comments.
RUN mosquitto_passwd -b /mqtt/config/mosquitto.passwd updater updater