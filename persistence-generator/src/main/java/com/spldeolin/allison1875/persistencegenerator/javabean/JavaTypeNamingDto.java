package com.spldeolin.allison1875.persistencegenerator.javabean;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2021-05-24
 */
@Data
@Accessors(chain = true)
public class JavaTypeNamingDto {

    private String simpleName;

    private String qualifier;

    public JavaTypeNamingDto setClass(Class clazz) {
        simpleName = clazz.getSimpleName();
        qualifier = clazz.getName();
        return this;
    }

}