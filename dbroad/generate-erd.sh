#!/bin/bash
set -euo pipefail  # Exit on error, undefined variable, or pipe failure

#
# Generate Entity Relationship Diagram (ERD) for the Road database
#
# This script connects to the local PostgreSQL database and:
# 1. Generates a markdown file with mermaid diagram and index information
# 2. Extracts the pure mermaid diagram to a separate file
# 3. Converts the diagram to SVG format
#
# Prerequisites:
# - PostgreSQL database running on localhost:54322
# - Database: road, User: road, Password: road
#
# Output files:
# - DATABASE.md: Markdown with diagram reference and index information
# - DATABASE.mermaid: Pure mermaid diagram definition
# - DATABASE.svg: SVG diagram image
#

readonly MARKDOWN_FILE="DATABASE.md"
readonly MERMAID_FILE="DATABASE.mermaid"
readonly SVG_FILE="DATABASE.svg"

# Database connection details
readonly DB_HOST="localhost"
readonly DB_PORT="54322"
readonly DB_NAME="road"
readonly DB_USER="road"
readonly DB_PASSWORD="road"

# Step 1: Generate markdown with mermaid diagram from database
echo "1. Generating database diagram as markdown from PostgreSQL..."
PGPASSWORD="${DB_PASSWORD}" npx --yes pg-mermaid@0.2.1 \
  --dbname "${DB_NAME}" \
  --username "${DB_USER}" \
  -h "${DB_HOST}" \
  -p "${DB_PORT}" \
  --output-path "${MARKDOWN_FILE}" \
  --excluded-tables flyway_schema_history geography_columns geometry_columns spatial_ref_sys

# Step 2: Extract pure mermaid diagram (between erDiagram and ```, excluding the fence markers)
echo "2. Extracting mermaid diagram to ${MERMAID_FILE}..."
awk '/erDiagram/,/^```$/ {if (!/^```$/) print}' "${MARKDOWN_FILE}" > "${MERMAID_FILE}"

# Rebuild markdown to reference the generated SVG and include the diagram
MARKDOWN_TMP="$(mktemp "${MARKDOWN_FILE}.tmp.XXXXXX")"
{
  echo "## Diagram"
  echo
  echo "![diagram](./${SVG_FILE})"
  echo
  echo "Mermaid source: [${MERMAID_FILE}](./${MERMAID_FILE})"
  echo
  awk 'BEGIN {print_rest = 0} /^## Indexes$/ {print_rest = 1} print_rest {print}' "${MARKDOWN_FILE}"
} > "${MARKDOWN_TMP}"
mv "${MARKDOWN_TMP}" "${MARKDOWN_FILE}"

MERMAID_CHANGED_IN_GIT=true
if git rev-parse --is-inside-work-tree >/dev/null 2>&1 && \
   git rev-parse --verify HEAD >/dev/null 2>&1; then
  if git diff --quiet HEAD -- "${MERMAID_FILE}"; then
    MERMAID_CHANGED_IN_GIT=false
  fi
fi

if [ "${MERMAID_CHANGED_IN_GIT}" = true ] || [ ! -f "${SVG_FILE}" ]; then
  # Step 3: Convert mermaid diagram to SVG with deterministic output
  echo "3. Converting diagram to SVG..."
  npx --yes @mermaid-js/mermaid-cli@11.12.0 \
    -i "${MERMAID_FILE}" \
    -o "${SVG_FILE}" \
    -c mermaid-config.json \
    --svgId road-db-diagram
else
  echo "3. Skipping SVG conversion because ${MERMAID_FILE} did not change"
fi

echo ""
echo "✓ Generated ${MARKDOWN_FILE} with database diagram"
echo "✓ Pure mermaid diagram: ${MERMAID_FILE}"
echo "✓ SVG diagram: ${SVG_FILE}"
echo ""
