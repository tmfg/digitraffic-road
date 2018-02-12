#!/bin/bash
# wait-for-postgres.sh

set -e

host="$1"
user="$2"
db="$3"
PGPASSWORD="$4"
shift
shift
shift
shift

cmd="$@"

export PGPASSWORD

until psql -h "$host" -U "$user" -d "$db" -c '\q'; do
  >&2 echo "Postgres is unavailable - sleeping"
  sleep 1
done

>&2 echo "Postgres is up - executing command"
exec $cmd