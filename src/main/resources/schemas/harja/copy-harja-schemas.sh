#!/usr/bin/env bash

BASEDIR=$(cd "$(dirname $0)" || exit; /bin/pwd)
echo "BASEDIR: ${BASEDIR}"

echo "HARJA project root dir as parameter e.g."
echo "./copy-harja-schemas.sh ../../../../../../harja"
echo
HARJA=${1:?"HARJA project root dir is required parameter. E.g. ./copy-harja-schemas.sh ../../../../../../harja"}
echo $HARJA
ls -al $HARJA

# Copy root schemas for maintenance trackings. Both are equal but one has sijainti geometria-sijainti.schema.json and another geometria-viivasijainti.schema.json
# Replacing that ref with manually combined schema for both (geometria-sijainti-viivasijainti-combined.schema.json) we need only generat on version of java classes.
cp ${HARJA}/resources/api/schemas/tyokoneenseurannan-kirjaus-request.schema.json ${BASEDIR}/tyokoneenseurannan-kirjaus-request.schema.json.org
cp ${HARJA}/resources/api/schemas/tyokoneenseurannan-kirjaus-viivageometrialla-request.schema.json ${BASEDIR}/tyokoneenseurannan-kirjaus-viivageometrialla-request.schema.json.org
cp ${BASEDIR}/tyokoneenseurannan-kirjaus-request.schema.json.org ${BASEDIR}/tyokoneenseurannan-kirjaus-request.schema.json

for ENTITY in 'otsikko.schema.json' \
              'organisaatio.schema.json' \
              'tunniste.schema.json' \
              'geometria-viivasijainti.schema.json' \
              'geometria-sijainti.schema.json' \
              'koordinaattisijainti.schema.json' \
              'viivageometriasijainti.schema.json' \
              'suoritettavat-tehtavat.schema.json'
do
    echo "cp ${HARJA}/resources/api/schemas/entities/${ENTITY} ${BASEDIR}/entities/"
    cp ${HARJA}/resources/api/schemas/entities/${ENTITY} ${BASEDIR}/entities/
done

# In root folder schemas replace refs to entitites e.g.
# "$ref": "file:resources/api/schemas/entities/suoritettavat-tehtavat.schema.json" ->
# "$ref": "entities/suoritettavat-tehtavat.schema.json"
find ${BASEDIR}/ -maxdepth 1 -type f -name '*.schema.json*' -exec sed -i '' -e 's/file:resources\/api\/schemas\///' {} \;

# In entities folder schemas replace refs to entitites e.g.
# "$ref": "file:resources/api/schemas/entities/viivageometriasijainti.schema.json",
# "$ref": "viivageometriasijainti.schema.json"
find ${BASEDIR}/entities/ -type f -name '*.schema.json' -exec sed -i '' -e 's/file:resources\/api\/schemas\/entities\///' {} \;

# In root folder schemas replace refs to entitites geometria-sijainti.schema.json and geometria-viivasijainti.schema.json
# with schema combining both sijainti types:
# "$ref": "entities/geometria-sijainti.schema.json" ->
# "$ref": "entities/geometria-sijainti-viivasijainti-combined.schema.json"
echo "Replacing geometria-sijainti.schema.json and geometria-viivasijainti.schema.json with geometria-sijainti-viivasijainti-combined.schema.json"
find ${BASEDIR}/ -maxdepth 1 -type f -name '*.schema.json' -exec sed -i '' -e 's/geometria-sijainti.schema.json/geometria-sijainti-viivasijainti-combined.schema.json/' {} \;
find ${BASEDIR}/ -maxdepth 1 -type f -name '*.schema.json' -exec sed -i '' -e 's/geometria-viivasijainti.schema.json/geometria-sijainti-viivasijainti-combined.schema.json/' {} \;
