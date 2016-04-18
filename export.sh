#!/bin/sh
DIRECTORY=/digitraffic_config/scripts/ansible/upload

mkdir -p $DIRECTORY
cp -r target/metadata-0.0.1-SNAPSHOT.jar $DIRECTORY/metadata.jar
