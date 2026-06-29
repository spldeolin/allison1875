package com.spldeolin.allison1875.formgenerator.dsl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import org.junit.jupiter.api.Test;

/**
 * @author Deolin 2026-06-29
 */
class FormDefValidateTest {

    @Test
    void shouldPassValidForm() {
        FormDef form = FormDef.builder()
                .name("Order")
                .title("订单")
                .items(List.of(
                        TextItemDef.builder().name("orderNo").title("订单号").isNonVoid(true).build()
                ))
                .build();

        assertDoesNotThrow(() -> FormDef.validate(List.of(form)));
    }

    @Test
    void shouldFailWhenFormsEmpty() {
        assertThrows(Allison1875Exception.class, () -> FormDef.validate(Collections.emptyList()));
    }

    @Test
    void shouldFailWhenFormsNull() {
        assertThrows(Allison1875Exception.class, () -> FormDef.validate(null));
    }

    @Test
    void shouldFailWhenNameEmpty() {
        FormDef form = FormDef.builder()
                .title("订单")
                .items(List.of(
                        TextItemDef.builder().name("f").title("t").isNonVoid(true).build()
                ))
                .build();

        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> FormDef.validate(List.of(form)));
        assertTrue(ex.getMessage().contains("name 不能为空"));
    }

    @Test
    void shouldFailWhenNameNotUpperCamel() {
        FormDef form = FormDef.builder()
                .name("order")
                .title("订单")
                .items(List.of(
                        TextItemDef.builder().name("f").title("t").isNonVoid(true).build()
                ))
                .build();

        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> FormDef.validate(List.of(form)));
        assertTrue(ex.getMessage().contains("首字母必须大写"));
    }

    @Test
    void shouldFailWhenNameEqualsUser() {
        FormDef form = FormDef.builder()
                .name("User")
                .title("用户")
                .items(List.of(
                        TextItemDef.builder().name("f").title("t").isNonVoid(true).build()
                ))
                .build();

        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> FormDef.validate(List.of(form)));
        assertTrue(ex.getMessage().contains("不允许与内置实体同名"));
    }

    @Test
    void shouldFailWhenItemsEmpty() {
        FormDef form = FormDef.builder()
                .name("Order")
                .title("订单")
                .items(Collections.emptyList())
                .build();

        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> FormDef.validate(List.of(form)));
        assertTrue(ex.getMessage().contains("items 不能为空"));
    }

    @Test
    void shouldFailWhenItemNameNotLowerCamel() {
        FormDef form = FormDef.builder()
                .name("Order")
                .title("订单")
                .items(List.of(
                        TextItemDef.builder().name("OrderNo").title("t").isNonVoid(true).build()
                ))
                .build();

        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> FormDef.validate(List.of(form)));
        assertTrue(ex.getMessage().contains("首字母必须小写"));
    }

    @Test
    void shouldFailWhenDuplicateItemNames() {
        FormDef form = FormDef.builder()
                .name("Order")
                .title("订单")
                .items(List.of(
                        TextItemDef.builder().name("field").title("t1").isNonVoid(true).build(),
                        TextItemDef.builder().name("field").title("t2").isNonVoid(true).build()
                ))
                .build();

        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> FormDef.validate(List.of(form)));
        assertTrue(ex.getMessage().contains("唯一"));
    }

    @Test
    void shouldFailWhenIndexReferencesNonExistentItem() {
        FormDef form = FormDef.builder()
                .name("Order")
                .title("订单")
                .items(List.of(
                        TextItemDef.builder().name("orderNo").title("t").isNonVoid(true).build()
                ))
                .indices(List.of(
                        IndexDef.builder().itemNames(List.of("nonExistent")).isUnique(true).build()
                ))
                .build();

        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> FormDef.validate(List.of(form)));
        assertTrue(ex.getMessage().contains("缺失"));
    }

    @Test
    void shouldFailWhenIndexContainsMultiSelectItem() {
        FormDef form = FormDef.builder()
                .name("Order")
                .title("订单")
                .items(List.of(
                        TextItemDef.builder().name("orderNo").title("t").isNonVoid(true).build(),
                        MultiSelectItemDef.builder().name("tags").title("标签").isNonVoid(false)
                                .options(List.of(OptionDef.builder().code("a").title("A").build()))
                                .build()
                ))
                .indices(List.of(
                        IndexDef.builder().itemNames(List.of("tags")).isUnique(true).build()
                ))
                .build();

        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> FormDef.validate(List.of(form)));
        assertTrue(ex.getMessage().contains("多选类型"));
    }

    @Test
    void shouldFailWhenItemIsNonVoidNull() {
        FormDef form = FormDef.builder()
                .name("Order")
                .title("订单")
                .items(List.of(
                        TextItemDef.builder().name("field").title("t").build()
                ))
                .build();

        Allison1875Exception ex = assertThrows(Allison1875Exception.class,
                () -> FormDef.validate(List.of(form)));
        assertTrue(ex.getMessage().contains("isNonVoid"));
    }

}
