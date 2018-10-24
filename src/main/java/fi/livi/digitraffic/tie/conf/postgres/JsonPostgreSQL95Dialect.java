package fi.livi.digitraffic.tie.conf.postgres;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL95Dialect;

/**
 * Dialect enables usage for PostgresSQL jsonb data type.
 */
public class JsonPostgreSQL95Dialect extends PostgreSQL95Dialect {

    public JsonPostgreSQL95Dialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}