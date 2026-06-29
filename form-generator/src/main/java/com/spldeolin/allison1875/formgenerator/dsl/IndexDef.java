package com.spldeolin.allison1875.formgenerator.dsl;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Deolin 2026-02-11
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class IndexDef {

    /**
     * 组成索引的字段的名称列表
     */
    List<String> itemNames;

    /**
     * 是否为唯一索引
     */
    @Builder.Default
    Boolean isUnique = false;

}
