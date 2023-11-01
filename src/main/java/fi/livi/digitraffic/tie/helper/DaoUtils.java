package fi.livi.digitraffic.tie.helper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DaoUtils {

    private DaoUtils() {
    }

    public static Integer findInteger(final ResultSet rs, final String columnName) throws SQLException {
        final int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    public static Long findLong(final ResultSet rs, final String columnName) throws SQLException {
        final long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    public static Double findDouble(final ResultSet rs, final String columnName) throws SQLException {
        final double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    public static Boolean findBoolean(final ResultSet rs, final String columnName) throws SQLException {
        final boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    public static <E extends Enum<E>> E findEnum(final ResultSet rs, final String columnName, final Class<E> clazz) throws SQLException {
        final String value = rs.getString(columnName);
        return rs.wasNull() ? null : E.valueOf(clazz, value);
    }
}
