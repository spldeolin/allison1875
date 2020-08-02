package com.spldeolin.allison1875.docanalyzer.yapi.javabean;

import lombok.Data;

/**
 * @author Deolin 2020-08-02
 */
@Data
public class CommonRespDto<T> {

    private Integer errcode;

    private String errmsg;

    private T data;

}