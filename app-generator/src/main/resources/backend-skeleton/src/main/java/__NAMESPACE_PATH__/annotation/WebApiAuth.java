package __NAMESPACE__.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import __NAMESPACE__.enums.PermissionEnum;

/**
 * 标注接口所需的功能权限。ApiAuthFilter 将检查当前用户是否拥有声明的权限。
 *
 * @author Deolin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebApiAuth {

    PermissionEnum[] value();

}
