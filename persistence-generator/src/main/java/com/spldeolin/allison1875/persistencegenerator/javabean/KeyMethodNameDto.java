package com.spldeolin.allison1875.persistencegenerator.javabean;

import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2020-12-08
 */
@Data
@Accessors(chain = true)
public class KeyMethodNameDto {

    private PropertyDto key;

    private String methodName;

}