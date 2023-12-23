package com.spldeolin.allison1875.inspector.javabean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-02-24
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PardonDto {

    @JsonProperty("源码位置")
    String sourceCode;

    /**
     * - type qualifier
     * - field qualifier
     * - method qualifier
     * - or else null
     */
    @JsonProperty("全限定名")
    String qualifier;

    @JsonProperty("条目号")
    String statuteNo;

}