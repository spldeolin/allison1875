package com.spldeolin.allison1875.appgenerator.dsl;

import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Deolin 2026-05-24
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class MenuDef {

    String group;

    String icon;

    Integer order;

    /**
     * 关联的表单
     */
    FormDef form;

    Permissions permissions;

    @Value
    @Builder(toBuilder = true)
    @Jacksonized
    public static class Permissions {

        String list;

        String create;

        String update;

        String delete;

    }

}
