package com.spldeolin.allison1875.persistencegenerator.javabean;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-08
 */
@Data
@Accessors(chain = true)
public class QueryByKeysDto {

    private PropertyDto key;

    private String methodName;

    private String varsName;

}