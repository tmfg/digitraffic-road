#!/usr/bin/env sh
#set -x

ENVIRONMENT=${1:-dev}
COMMAND=${2:-info}
OPTIONS=${3:-}
# https://stackoverflow.com/a/4774063/23137398
SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 || exit ; pwd -P )"

# Show help and exit if -h/--help is used as first argument
if [ "$ENVIRONMENT" = "-h" ] || [ "$ENVIRONMENT" = "--help" ]; then
  echo "Usage: ./update-db.sh <env> [command: migrate, clean, info, validate, baseline, repair] [options: -outOfOrder=true -skipExecutingMigrations=true]"
  echo "Ie. ./update-db.sh dev migrate"
  exit 0
fi

# List of commands that can modify database schema or data
MUTATING_COMMANDS="migrate clean repair baseline"

# Check if current COMMAND is one of the mutating commands
NEEDS_CONFIRMATION=false
for c in $MUTATING_COMMANDS; do
  if [ "$COMMAND" = "$c" ]; then
    NEEDS_CONFIRMATION=true
    break
  fi
done

echo
echo "Run in ${SCRIPT_DIR}"
echo "./update-db.sh ${ENVIRONMENT} ${COMMAND} ${OPTIONS}"

# Ask confirmation for mutating commands
if [ "$NEEDS_CONFIRMATION" = "true" ]; then
  echo
  echo "WARNING: This will run Flyway '$COMMAND' against environment '$ENVIRONMENT' (URL from conf/${ENVIRONMENT}/flyway.conf)."
  echo "This may modify the database schema and/or data."
  printf "Do you want to continue? [y/N]: "
  read CONFIRM
  case "$CONFIRM" in
    y|Y|yes|YES)
      echo "Proceeding with Flyway $COMMAND..."
      ;;
    *)
      echo "Aborted by user."
      exit 1
      ;;
  esac
fi
echo

# --network=dbroad parameter is needed for docker internal connection.
# Otherwise we should use host.docker.internal for connecting from local network outside docker.
docker run --rm \
--network=dbroad \
-v ${SCRIPT_DIR}/conf/${ENVIRONMENT}:/flyway/conf \
-v "${SCRIPT_DIR}/sql:/flyway/sql" \
flyway/flyway:11.20.0-alpine ${COMMAND} ${OPTIONS}
