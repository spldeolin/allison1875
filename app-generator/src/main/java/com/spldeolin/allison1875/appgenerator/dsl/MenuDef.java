package com.spldeolin.allison1875.appgenerator.dsl;

import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * @author Deolin 2026-05-24
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuDef {

    String group;

    String icon;

    String order;

    /**
     * 关联的表单
     */
    FormDef form;

}