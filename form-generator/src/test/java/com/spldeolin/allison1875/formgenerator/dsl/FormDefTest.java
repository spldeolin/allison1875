package com.spldeolin.allison1875.formgenerator.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.spldeolin.allison1875.formgenerator.dsl.enums.ItemType;
import com.spldeolin.allison1875.formgenerator.dsl.enums.TimeFormat;
import com.spldeolin.allison1875.formgenerator.dsl.item.MultiSelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.NumberItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.OnOffItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.SelectItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TimeItemDef;
import org.junit.jupiter.api.Test;

/**
 * @author Deolin 2026-06-29
 */
class FormDefTest {

    private final YAMLMapper yamlMapper = new YAMLMapper();

    @Test
    void shouldDeserializeFullForm() throws IOException {
        List<FormDef> forms = deserialize("dsl/full-form.yml");

        assertEquals(1, forms.size());
        FormDef form = forms.get(0);
        assertEquals("Order", form.getName());
        assertEquals("订单", form.getTitle());
        assertEquals("订单管理", form.getDesc());
        assertEquals(5, form.getItems().size());
        assertEquals(1, form.getIndices().size());

        // text item
        TextItemDef textItem = (TextItemDef) form.getItems().get(0);
        assertEquals("orderNo", textItem.getName());
        assertEquals("订单号", textItem.getTitle());
        assertTrue(textItem.getIsNonVoid());
        assertEquals(32, textItem.getMaxLength());
        assertEquals(ItemType.TEXT, textItem.getType());
        assertTrue(textItem.getCanInputOnInit());
        assertTrue(textItem.getCanInputOnEdit());

        // number item
        NumberItemDef numberItem = (NumberItemDef) form.getItems().get(1);
        assertEquals("amount", numberItem.getName());
        assertTrue(numberItem.getCanBeDecimal());
        assertEquals(ItemType.NUMBER, numberItem.getType());

        // select item
        SelectItemDef selectItem = (SelectItemDef) form.getItems().get(2);
        assertEquals("status", selectItem.getName());
        assertEquals(2, selectItem.getOptions().size());
        assertEquals("pending", selectItem.getOptions().get(0).getCode());
        assertEquals("待处理", selectItem.getOptions().get(0).getTitle());
        assertEquals(ItemType.SELECT, selectItem.getType());

        // time item
        TimeItemDef timeItem = (TimeItemDef) form.getItems().get(3);
        assertEquals("orderDate", timeItem.getName());
        assertEquals(TimeFormat.DATE_TIME, timeItem.getFormat());
        assertEquals(ItemType.TIME, timeItem.getType());

        // onOff item
        OnOffItemDef onOffItem = (OnOffItemDef) form.getItems().get(4);
        assertEquals("isPaid", onOffItem.getName());
        assertEquals(ItemType.ON_OFF, onOffItem.getType());

        // index
        IndexDef index = form.getIndices().get(0);
        assertEquals(List.of("orderNo"), index.getItemNames());
        assertTrue(index.getIsUnique());
    }

    @Test
    void shouldDeserializeMinimalForm() throws IOException {
        List<FormDef> forms = deserialize("dsl/minimal-form.yml");

        assertEquals(1, forms.size());
        FormDef form = forms.get(0);
        assertEquals("Simple", form.getName());
        assertEquals("简单表单", form.getTitle());
        assertNull(form.getDesc());
        assertNull(form.getIndices());
        assertEquals(1, form.getItems().size());
    }

    @Test
    void shouldApplyBuilderDefaults() {
        TextItemDef text = TextItemDef.builder()
                .name("test")
                .title("测试")
                .isNonVoid(true)
                .build();

        assertTrue(text.getCanInputOnInit());
        assertTrue(text.getCanInputOnEdit());
        assertFalse(text.getIsMultilineOrRich());
        assertEquals(255, text.getMaxLength());
        assertNull(text.getRegex());
        assertNull(text.getIsBuiltinField());
        assertEquals(ItemType.TEXT, text.getType());
    }

    @Test
    void shouldApplyNumberItemDefaults() {
        NumberItemDef number = NumberItemDef.builder()
                .name("count")
                .title("数量")
                .isNonVoid(true)
                .build();

        assertFalse(number.getCanBeDecimal());
        assertEquals(ItemType.NUMBER, number.getType());
    }

    @Test
    void shouldApplyTimeItemDefaults() {
        TimeItemDef time = TimeItemDef.builder()
                .name("created")
                .title("创建时间")
                .isNonVoid(true)
                .build();

        assertEquals(TimeFormat.DATE_TIME, time.getFormat());
        assertEquals(ItemType.TIME, time.getType());
    }

    @Test
    void shouldApplyIndexDefDefaults() {
        IndexDef index = IndexDef.builder()
                .itemNames(List.of("a", "b"))
                .build();

        assertFalse(index.getIsUnique());
    }

    @Test
    void shouldDeriveFormWithToBuilder() {
        FormDef original = FormDef.builder()
                .name("Order")
                .title("订单")
                .items(List.of(TextItemDef.builder().name("f1").title("t1").isNonVoid(true).build()))
                .build();

        FormDef derived = original.toBuilder().title("订单V2").build();

        assertNotSame(original, derived);
        assertEquals("订单", original.getTitle());
        assertEquals("订单V2", derived.getTitle());
        assertEquals(original.getName(), derived.getName());
        assertEquals(original.getItems(), derived.getItems());
    }

    @Test
    void shouldDeriveItemWithToBuilder() {
        TextItemDef original = TextItemDef.builder()
                .name("field")
                .title("字段")
                .isNonVoid(true)
                .maxLength(100)
                .build();

        TextItemDef derived = original.toBuilder().maxLength(200).build();

        assertNotSame(original, derived);
        assertEquals(100, original.getMaxLength());
        assertEquals(200, derived.getMaxLength());
        assertEquals(original.getName(), derived.getName());
    }

    @Test
    void shouldBuildMultiSelectItemDef() {
        OptionDef opt = OptionDef.builder().code("a").title("A").build();
        MultiSelectItemDef multi = MultiSelectItemDef.builder()
                .name("tags")
                .title("标签")
                .isNonVoid(false)
                .options(List.of(opt))
                .build();

        assertEquals(ItemType.MULTI_SELECT, multi.getType());
        assertEquals(1, multi.getOptions().size());
        assertEquals("A", multi.getOptions().get(0).getTitle());
    }

    @Test
    void shouldGetNonAuditedItems() {
        TextItemDef builtin = TextItemDef.builder()
                .name("bizId")
                .title("业务主键")
                .isNonVoid(true)
                .isBuiltinField(true)
                .build();
        TextItemDef user = TextItemDef.builder()
                .name("field1")
                .title("用户字段")
                .isNonVoid(true)
                .build();
        FormDef form = FormDef.builder()
                .name("Test")
                .title("测试")
                .items(List.of(builtin, user))
                .build();

        List<ItemDef> nonAudited = form.getNonAuditedItems();
        assertEquals(1, nonAudited.size());
        assertEquals("field1", nonAudited.get(0).getName());
    }

    @Test
    void shouldConvertOptionDefToEnumConstantName() {
        OptionDef opt = OptionDef.builder().code("pendingApproval").title("待审批").build();
        assertEquals("PENDING_APPROVAL", opt.javaEnumConstantName());

        OptionDef numeric = OptionDef.builder().code("1st").title("第一").build();
        assertEquals("E1ST", numeric.javaEnumConstantName());
    }

    private List<FormDef> deserialize(String resource) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(is, "resource not found: " + resource);
            String yaml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return yamlMapper.readValue(yaml, new TypeReference<>() {
            });
        }
    }

}
