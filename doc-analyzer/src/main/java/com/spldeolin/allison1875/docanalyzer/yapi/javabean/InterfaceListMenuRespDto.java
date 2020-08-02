package com.spldeolin.allison1875.docanalyzer.yapi.javabean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Deolin 2020-08-02
 */
@Data
public class InterfaceListMenuRespDto {

    @JsonProperty("_id")
    private Long id;

    private String name;

    // and more...

}