#!/bin/bash

MARKDOWN_FILE=DATABASE.md
PG_DBNAME=road
PG_USERNAME=road
PG_PASSWORD=road
PG_PORT=54322
echo "Generate database diagram as markdown in ${MARKDOWN_FILE} file"
PGPASSWORD=${PG_PASSWORD} npx --yes pg-mermaid@0.2.1 --dbname ${PG_DBNAME} --username ${PG_USERNAME} -h localhost -p ${PG_PORT} --output-path ${MARKDOWN_FILE} --excluded-tables flyway_schema_history geography_columns geometry_columns spatial_ref_sys
# Generate plain mermaid erDiagram
echo "Remove indexes from generated markdown and save as DATABASE.mermaid with only diagram of db"
grep 'erDiagram' ${MARKDOWN_FILE} -A10000000 | grep '```' -B10000000 | grep -v '```' > DATABASE.mermaid

# Convert mermaid diagram to svg and refer to it from markdown
echo "Convert mermaid diagram to svg and refer to it from markdown"
npx --yes  @mermaid-js/mermaid-cli@11.12.0 -i ${MARKDOWN_FILE} -o ${MARKDOWN_FILE}


FROM_FILE=./DATABASE-1.svg
TO_FILE=./DATABASE.svg

if [ -f "${FROM_FILE}" ]
then
  echo "File ${FROM_FILE} exists, rename it to ${TO_FILE}"
  if ! command -v gsed 2>&1 >/dev/null
  then
    echo "Run sed to replace reference from ${FROM_FILE} to ${TO_FILE} in ${MARKDOWN_FILE}"
    sed -i '' 's/\.\/DATABASE-1.svg/\.\/DATABASE.svg/' ${MARKDOWN_FILE}
  else
    echo "Run gsed to replace reference from ${FROM_FILE} to ${TO_FILE} in ${MARKDOWN_FILE}"
    gsed -i 's/\.\/DATABASE-1.svg/\.\/DATABASE.svg/' ${MARKDOWN_FILE}
  fi
  mv "${FROM_FILE}" "${TO_FILE}"
fi
echo
echo "Generated ${MARKDOWN_FILE} with database diagram in it."
echo "Plain SVG diagram in file ${TO_FILE}"
echo


