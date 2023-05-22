package com.spldeolin.allison1875.base.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * java.time包下的“时间”类型工具类
 * <pre>
 * 涉及
 * java.util.Date与“时间”类型的转化；
 * String与“时间”类型的转化；
 * Unix时间戳与“时间”类型的转化；
 * 简化“时间”类型各种操作的代码量，一切时区均使用系统所在时区；
 * </pre>
 *
 * @author Deolin 2018/05/23
 */
public class TimeUtils {

    public static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    /**
     * 1970-01-01
     */
    public static final LocalDate WORLD_BIRTHDAY = LocalDate.of(1970, 1, 1);

    /**
     * 00:00:00
     */
    public static final LocalTime DAY_START = LocalTime.of(0, 0, 0);

    /**
     * 23:59:59
     */
    public static final LocalTime DAY_END = LocalTime.of(23, 59, 59);

    /**
     * 1970-01-01 00:00:00
     */
    public static final LocalDateTime WORLD_BORN_ON = LocalDateTime.of(WORLD_BIRTHDAY, DAY_START);

    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private TimeUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * java.util.Date -> LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), SYSTEM_ZONE);
    }

    /**
     * UNIX时间戳 -> LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(long unixTimeStamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimeStamp), ZoneId.systemDefault());
    }

    /**
     * String -> LocalDateTime  （yyyy-MM-dd）
     */
    public static LocalDateTime toLocalDateTime(String content) {
        return LocalDateTime.parse(content, DEFAULT_DATE_TIME_FORMATTER);
    }

    /**
     * String -> LocalDateTime  （指定格式）
     */
    public static LocalDateTime toLocalDateTime(String content, String pattern) {
        return LocalDateTime.parse(content, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * java.util.Date -> LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        return toLocalDateTime(date).toLocalDate();
    }

    /**
     * java.util.Date -> LocalTime
     */
    public static LocalTime toLocalTime(Date date) {
        return toLocalDateTime(date).toLocalTime();
    }

    /**
     * LocalDateTime转化为String（yyyy-MM-dd HH:mm:ss）
     */
    public static String toString(LocalDateTime localDateTime) {
        return DEFAULT_DATE_TIME_FORMATTER.format(localDateTime);
    }

    /**
     * LocalDateTime -> String （指定格式）
     */
    public static String toString(LocalDateTime localDateTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(localDateTime);
    }

    /**
     * LocalDate -> String  （yyyy-MM-dd）
     */
    public static String toString(LocalDate localDate) {
        return DEFAULT_DATE_FORMATTER.format(localDate);
    }

    /**
     * LocalDate -> String  （指定格式）
     */
    public static String toString(LocalDate localDate, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(localDate);
    }

    /**
     * LocalTime -> String  （HH:mm:ss）
     */
    public static String toString(LocalTime localTime) {
        return DEFAULT_TIME_FORMATTER.format(localTime);
    }

    /**
     * LocalTime -> String  （指定格式）
     */
    public static String toString(LocalTime localTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(localTime);
    }

    /**
     * LocalDateTime -> java.util.Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDateTime -> UNIX时间戳
     */
    public static long toUnixTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(SYSTEM_ZONE).toInstant().getEpochSecond();
    }

    /**
     * 将时间拨动到当天的00:00:00
     */
    public static LocalDateTime toggleToDayStart(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), DAY_START);
    }

    /**
     * 将时间拨动到当填的23:59:59
     */
    public static LocalDateTime toggleToDayEnd(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), DAY_END);
    }

}
