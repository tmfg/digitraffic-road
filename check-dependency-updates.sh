#!/usr/bin/env bash
mvn versions:display-dependency-updates

echo
echo "You can update pom.xml by running: mvn versions:use-latest-releases"
echo "Old version of pom.xml will be saved as pom.xml.versionsBackup"