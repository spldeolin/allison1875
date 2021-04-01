package com.spldeolin.allison1875.persistencegenerator.handle;

/**
 * @author Deolin 2021-03-23
 */
public interface JdbcTypeHandle {

    Class<?> jdbcType2javaType(String columnType, String dataType);

}
