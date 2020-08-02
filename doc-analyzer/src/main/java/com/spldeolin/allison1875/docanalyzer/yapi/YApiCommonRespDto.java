package com.spldeolin.allison1875.docanalyzer.yapi;

import lombok.Data;

/**
 * @author Deolin 2020-08-02
 */
@Data
public class YApiCommonRespDto<T> {

    private Integer code;

    private String errmsg;

    private T data;

}