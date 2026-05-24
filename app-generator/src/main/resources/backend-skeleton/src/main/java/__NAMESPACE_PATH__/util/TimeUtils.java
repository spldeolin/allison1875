package __NAMESPACE__.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtils {

    public static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    public static final LocalDate WORLD_BIRTHDAY = LocalDate.of(1970, 1, 1);

    public static final LocalTime DAY_START = LocalTime.of(0, 0, 0);

    public static final LocalTime DAY_END = LocalTime.of(23, 59, 59);

    public static final LocalDateTime WORLD_BORN_ON = LocalDateTime.of(WORLD_BIRTHDAY, DAY_START);

    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private TimeUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    public static LocalDateTime toLocalDateTime(long unixTimeStamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimeStamp), ZoneId.systemDefault());
    }

    public static LocalDateTime toLocalDateTime(String content) {
        return LocalDateTime.parse(content, DEFAULT_DATE_TIME_FORMATTER);
    }

    public static LocalDateTime toLocalDateTime(String content, String pattern) {
        return LocalDateTime.parse(content, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate toLocalDate(Date date) {
        return toLocalDateTime(date).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), SYSTEM_ZONE);
    }

    public static LocalTime toLocalTime(Date date) {
        return toLocalDateTime(date).toLocalTime();
    }

    public static String toString(LocalDateTime localDateTime) {
        return DEFAULT_DATE_TIME_FORMATTER.format(localDateTime);
    }

    public static String toString(LocalDateTime localDateTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(localDateTime);
    }

    public static String toString(LocalDate localDate) {
        return DEFAULT_DATE_FORMATTER.format(localDate);
    }

    public static String toString(LocalDate localDate, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(localDate);
    }

    public static String toString(LocalTime localTime) {
        return DEFAULT_TIME_FORMATTER.format(localTime);
    }

    public static String toString(LocalTime localTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(localTime);
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static long toUnixTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(SYSTEM_ZONE).toInstant().getEpochSecond();
    }

    public static LocalDateTime toggleToDayStart(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return LocalDateTime.of(localDateTime.toLocalDate(), DAY_START);
    }

    public static LocalDateTime toggleToDayEnd(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return LocalDateTime.of(localDateTime.toLocalDate(), DAY_END);
    }

}
