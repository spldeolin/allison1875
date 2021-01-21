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

    private Map<String, Object> more;

}