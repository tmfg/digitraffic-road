#!/usr/bin/env bash

# Give params as -param=value

BASEDIR=$(cd "$(dirname "$0")"; pwd)

cd ${BASEDIR}

echo "Running at dir $(pwd)"

mkdir -p ${BASEDIR}/lib

SS_JAR="${BASEDIR}/lib/schemaspy.jar"
PG_JAR="${BASEDIR}/lib/postgresql.jar"

# Download only if changed
if test -e ${SS_JAR}
then
    Z_FLAG="-z ${SS_JAR}"
else
    Z_FLAG=
fi
curl ${Z_FLAG} -o ${SS_JAR} -L https://github.com/schemaspy/schemaspy/releases/download/v6.0.0/schemaspy-6.0.0.jar

if test -e ${PG_JAR}
then
    Z_FLAG="-z ${PG_JAR}"
else
    Z_FLAG=
fi
curl ${Z_FLAG} -o ${PG_JAR} -L https://jdbc.postgresql.org/download/postgresql-42.2.5.jar

# Copy default parameters, read command line parameters and replace in properties
cp ${BASEDIR}/schemaspy-default.properties ${BASEDIR}/lib/schemaspy.properties
while [ "$1" != "" ]; do
    PARAM=`echo $1 | awk -F= '{print $1}'`
    VALUE=`echo $1 | awk -F= '{print $2}'`

    if [ ! -z "${PARAM}" ] && [ ! -z  "${VALUE}" ]; then
        PARAM=${PARAM:1}
        echo "Setting property: schemaspy.${PARAM}=${VALUE}"
        sed -i.bak -e "s;schemaspy.${PARAM}=.*;schemaspy.${PARAM}=${VALUE};g" ${BASEDIR}/lib/schemaspy.properties
    fi
    shift
done

java -jar ${BASEDIR}/lib/schemaspy.jar -configFile ${BASEDIR}/lib/schemaspy.properties
