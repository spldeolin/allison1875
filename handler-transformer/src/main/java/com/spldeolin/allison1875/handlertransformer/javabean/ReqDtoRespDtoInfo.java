package com.spldeolin.allison1875.handlertransformer.javabean;

import java.util.List;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.common.ast.FileFlush;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2021-01-12
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqDtoRespDtoInfo {

    String paramType = null;

    String resultType = null;

    final List<FileFlush> flushes = Lists.newArrayList();

}