package com.spldeolin.allison1875.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表该handler的参数形式是URL Query，Req声明中的Nest DTO将会被忽略
 *
 * 适配Allison 1875 handler-transformer
 *
 * @author Deolin 2024-09-28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GetUrlQuery {

}
