package com.spldeolin.allison1875.persistencegenerator.facade.javabean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-05-24
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JavaTypeNamingDto {

    String simpleName;

    String qualifier;

    public JavaTypeNamingDto setClass(Class<?> clazz) {
        simpleName = clazz.getSimpleName();
        qualifier = clazz.getName();
        return this;
    }

}