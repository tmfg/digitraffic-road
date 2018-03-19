package fi.livi.digitraffic.tie.helper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DaoUtils {

    private DaoUtils() {
    }

    public static Integer findInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    public static Long findLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    public static Double findDouble(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    public static Boolean findBoolean(ResultSet rs, String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    public static <E extends Enum<E>> E findEnum(ResultSet rs, String columnName, Class<E> clazz) throws SQLException {
        String value = rs.getString(columnName);
        return rs.wasNull() ? null : E.valueOf(clazz, value);
    }
}
