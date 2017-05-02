package fi.livi.digitraffic.tie.helper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

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

    public static LocalTime findLocalTime(ResultSet rs, String columnName) throws SQLException {
        Time time = rs.getTime(columnName);
        return rs.wasNull() ? null : time.toLocalTime();
    }

    public static ZonedDateTime findZonedDateTime(ResultSet rs, String columnName) throws SQLException {
        String column = rs.getString(columnName);
        return rs.wasNull() ? null : ZonedDateTime.parse(column);
    }

    public static LocalDateTime findLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return rs.wasNull() ? null : timestamp.toLocalDateTime();
    }

    public static LocalDate findLocalDate(ResultSet rs, String columnName) throws SQLException {
        Date date = rs.getDate(columnName);
        return rs.wasNull() ? null : date.toLocalDate();
    }
}
