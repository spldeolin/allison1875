package com.spldeolin.allison1875.persistencegenerator.handle;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import com.google.inject.Singleton;

/**
 * @author Deolin 2021-03-23
 */
@Singleton
public class DefaultJdbcTypeHandle implements JdbcTypeHandle {

    @Override
    public Class<?> jdbcType2javaType(String columnType, String dataType) {
        if (columnType == null || dataType == null) {
            throw new IllegalArgumentException("illegal argument.");
        }
        if ("tinyint(1)".equalsIgnoreCase(columnType)) {
            return Boolean.class;
        }
        if (StringUtils.equalsAnyIgnoreCase(dataType, "varchar", "char", "text", "longtext")) {
            return String.class;
        }
        if ("tinyint".equals(dataType)) {
            return Byte.class;
        }
        if ("int".equals(dataType)) {
            return Integer.class;
        }
        if ("bigint".equals(dataType)) {
            return Long.class;
        }
        if ("date".equals(dataType)) {
            return Date.class;
        }
        if ("time".equals(dataType)) {
            return Date.class;
        }
        if ("datetime".equals(dataType)) {
            return Date.class;
        }
        if ("timestamp".equals(dataType)) {
            return Date.class;
        }
        if ("decimal".equals(dataType)) {
            return BigDecimal.class;
        }
        return null;
    }

}