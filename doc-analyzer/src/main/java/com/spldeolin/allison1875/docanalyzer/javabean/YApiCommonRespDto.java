package com.spldeolin.allison1875.docanalyzer.javabean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2020-08-02
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class YApiCommonRespDto<T> {

    Integer errcode;

    String errmsg;

    T data;

}