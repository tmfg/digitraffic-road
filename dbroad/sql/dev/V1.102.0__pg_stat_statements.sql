CREATE EXTENSION IF NOT EXISTS pg_stat_statements WITH SCHEMA public;

CREATE TABLE "pghero_query_stats" (
  "id" serial primary key,
  "database" text,
  "user" text,
  "query" text,
  "query_hash" bigint,
  "total_time" float,
  "calls" bigint,
  "captured_at" timestamp
);
CREATE INDEX ON "pghero_query_stats" ("database", "captured_at");