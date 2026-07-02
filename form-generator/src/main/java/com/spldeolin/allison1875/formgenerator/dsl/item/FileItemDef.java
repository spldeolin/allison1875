package com.spldeolin.allison1875.formgenerator.dsl.item;

import com.spldeolin.allison1875.formgenerator.dsl.ItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * 文件类字段定义
 *
 * @author Deolin 2026-07-02
 */
@Getter
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileItemDef extends ItemDef {

    /**
     * 字段类型，用于在反序列时区别ItemDef的具体类型
     */
    @Builder.Default
    ItemType type = ItemType.FILE;

    /**
     * 文件类别，对应后端FileCategoryEnum，默认general
     */
    @Builder.Default
    String category = "general";

    /**
     * 前端UX校验用的最大文件大小（MB），留空不限制
     */
    Integer maxFileSize;

}
