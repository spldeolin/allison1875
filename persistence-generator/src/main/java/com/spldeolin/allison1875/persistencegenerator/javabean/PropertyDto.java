package com.spldeolin.allison1875.persistencegenerator.javabean;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-07-12
 */
@Data
@Accessors(chain = true)
public class PropertyDto {

    private String name;

    private Class<?> type;

    private String description;

}