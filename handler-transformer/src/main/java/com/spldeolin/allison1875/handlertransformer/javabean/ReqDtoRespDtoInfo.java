package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Collection;
import com.google.common.collect.Lists;
import lombok.Data;

/**
 * @author Deolin 2021-01-12
 */
@Data
public class ReqDtoRespDtoInfo {

    private String reqDtoQualifier = null;

    private String respDtoQualifier = null;

    private String paramType = null;

    private String resultType = null;

    private final Collection<String> dtoQualifiers = Lists.newArrayList();

}