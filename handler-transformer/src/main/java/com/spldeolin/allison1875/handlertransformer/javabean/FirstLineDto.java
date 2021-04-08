package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Map;
import lombok.Data;

/**
 * @author Deolin 2020-12-22
 */
@Data
public class FirstLineDto {

    private String handlerUrl;

    private String handlerName;

    private String handlerDescription;

    private String presentServiceQualifier;

    private String serviceName;

    private Map<String, Object> more;

    @Override
    public String toString() {
        return handlerUrl + " " + handlerDescription;
    }

}