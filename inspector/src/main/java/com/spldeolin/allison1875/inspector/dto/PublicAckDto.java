package com.spldeolin.allison1875.inspector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Deolin 2020-02-24
 */
@Data
public class PublicAckDto {

    @JsonProperty("源码位置")
    private String sourceCode;

    /**
     * - type qualifier
     * - field qualifier
     * - method qualifier
     * - or else null
     */
    @JsonProperty("全限定名")
    private String qualifier;

    @JsonProperty("条目号")
    private String statuteNo;

}