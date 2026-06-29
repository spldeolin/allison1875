package com.spldeolin.allison1875.appgenerator.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import com.spldeolin.allison1875.formgenerator.dsl.item.TextItemDef;
import org.junit.jupiter.api.Test;

/**
 * @author Deolin 2026-06-29
 */
class AppDefTest {

    private final YAMLMapper yamlMapper = new YAMLMapper();

    @Test
    void shouldDeserializeAppDef() throws IOException {
        AppDef appDef = deserialize("dsl/app.yml");

        assertEquals("com.example.demo", appDef.getNamespace());
        assertEquals("my-app", appDef.getName());
        assertEquals("示例应用", appDef.getTitle());
        assertEquals(2, appDef.getMenus().size());

        MenuDef menu1 = appDef.getMenus().get(0);
        assertEquals("业务管理", menu1.getGroup());
        assertEquals("1", menu1.getIcon());
        assertEquals(1, menu1.getOrder());
        assertNotNull(menu1.getForm());
        assertEquals("Order", menu1.getForm().getName());
        assertEquals("订单", menu1.getForm().getTitle());
        assertEquals(1, menu1.getForm().getItems().size());

        MenuDef menu2 = appDef.getMenus().get(1);
        assertEquals("Product", menu2.getForm().getName());
    }

    @Test
    void shouldBuildAppDefWithBuilder() {
        FormDef form = FormDef.builder()
                .name("Test")
                .title("测试")
                .items(List.of(TextItemDef.builder().name("f").title("t").isNonVoid(true).build()))
                .build();
        MenuDef menu = MenuDef.builder()
                .group("g")
                .icon("i")
                .order(1)
                .form(form)
                .build();
        AppDef appDef = AppDef.builder()
                .namespace("com.example")
                .name("demo")
                .title("演示")
                .menus(List.of(menu))
                .build();

        assertEquals("com.example", appDef.getNamespace());
        assertEquals("demo", appDef.getName());
        assertEquals(1, appDef.getMenus().size());
        assertEquals("Test", appDef.getMenus().get(0).getForm().getName());
    }

    @Test
    void shouldDeriveMenuDefWithToBuilder() {
        MenuDef original = MenuDef.builder()
                .group("g")
                .order(1)
                .form(FormDef.builder().name("A").title("a").build())
                .build();

        MenuDef.Permissions permissions = MenuDef.Permissions.builder()
                .list("LIST_A")
                .create("CREATE_A")
                .update("UPDATE_A")
                .delete("DELETE_A")
                .build();
        MenuDef derived = original.toBuilder().permissions(permissions).order(100001).build();

        assertNotSame(original, derived);
        assertEquals(1, original.getOrder());
        assertEquals(100001, derived.getOrder());
        assertNotNull(derived.getPermissions());
        assertEquals("LIST_A", derived.getPermissions().getList());
    }

    private AppDef deserialize(String resource) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(is, "resource not found: " + resource);
            String yaml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return yamlMapper.readValue(yaml, AppDef.class);
        }
    }

}
