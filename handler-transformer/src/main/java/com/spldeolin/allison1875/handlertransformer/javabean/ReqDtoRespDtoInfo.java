package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.Collection;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-01-12
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqDtoRespDtoInfo {

     String reqDtoQualifier = null;

     String respDtoQualifier = null;

     String paramType = null;

     String resultType = null;

     final Collection<String> dtoQualifiers = Lists.newArrayList();

}