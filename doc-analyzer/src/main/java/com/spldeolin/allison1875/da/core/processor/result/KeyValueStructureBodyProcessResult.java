package com.spldeolin.allison1875.da.core.processor.result;

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * kv型数据结构
 *
 * e.g.: @ResponseBody public UserVo ....
 *
 * @author Deolin 2020-01-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class KeyValueStructureBodyProcessResult extends BodyProcessResult {

    private ObjectSchema objectSchema;

}
