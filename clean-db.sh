#!/bin/bash
set -e
(
  cd ../digitraffic-ci-db
  ./clean-migrate-sujuvuus-vagrant.sh
)
