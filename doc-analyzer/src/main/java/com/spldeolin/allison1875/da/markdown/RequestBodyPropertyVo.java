package com.spldeolin.allison1875.da.markdown;

import lombok.Data;

/**
 * @author Deolin 2020-02-17
 */
@Data
public class RequestBodyPropertyVo {

    private String path;

    private String name;

    private String description;

    private String detailedJsonType;

    private String validators;

}