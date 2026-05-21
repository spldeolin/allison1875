package com.spldeolin.allison1875.formgenerator.dsl;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-02-11
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IndexDef {

    /**
     * 组成索引的字段的名称列表
     */
    @NotEmpty
    List<@NotEmpty String> itemNames;

    /**
     * 是否为唯一索引
     */
    @NotNull
    Boolean isUnique = false;

}