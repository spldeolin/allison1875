package com.spldeolin.allison1875.docanalyzer.javabean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Deolin 2020-08-02
 */
@Data
public class YApiProjectGetRespDto {

    @JsonProperty("_id")
    private Long id;

    // and more...

}