package com.spldeolin.allison1875.appgenerator.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class PermissionEnumGenerateServiceImplTest {

    private PermissionEnumGenerateServiceImpl service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new PermissionEnumGenerateServiceImpl();
    }

    @Test
    void generatePermissionEnum_singleForm_generates4PermissionPoints() throws IOException {
        FormDef form = new FormDef();
        form.setName("Order");
        form.setTitle("订单");

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        assertTrue(Files.exists(file));

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.contains("LIST_ORDER(\"LIST_ORDER\", \"查看订单\", Group.ORDER, null)"));
        assertTrue(content.contains("CREATE_ORDER(\"CREATE_ORDER\", \"创建订单\", Group.ORDER, LIST_ORDER)"));
        assertTrue(content.contains("UPDATE_ORDER(\"UPDATE_ORDER\", \"编辑订单\", Group.ORDER, LIST_ORDER)"));
        assertTrue(content.contains("DELETE_ORDER(\"DELETE_ORDER\", \"删除订单\", Group.ORDER, LIST_ORDER)"));
    }

    @Test
    void generatePermissionEnum_multipleFormsIncludingBuiltinUser() throws IOException {
        FormDef orderForm = new FormDef();
        orderForm.setName("Order");
        orderForm.setTitle("订单");

        FormDef userForm = new FormDef();
        userForm.setName("User");
        userForm.setTitle("用户");

        List<FormDef> forms = Arrays.asList(orderForm, userForm);
        service.generatePermissionEnum(forms, tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("LIST_ORDER"));
        assertTrue(content.contains("CREATE_ORDER"));
        assertTrue(content.contains("UPDATE_ORDER"));
        assertTrue(content.contains("DELETE_ORDER"));

        assertTrue(content.contains("LIST_USER"));
        assertTrue(content.contains("CREATE_USER"));
        assertTrue(content.contains("UPDATE_USER"));
        assertTrue(content.contains("DELETE_USER"));

        assertTrue(content.contains("ORDER(\"ORDER\", \"订单管理\")"));
        assertTrue(content.contains("USER(\"USER\", \"用户管理\")"));
    }

    @Test
    void generatePermissionEnum_multiWordFormName_convertsToUpperSnake() throws IOException {
        FormDef form = new FormDef();
        form.setName("UserProfile");
        form.setTitle("用户信息");

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("LIST_USER_PROFILE"));
        assertTrue(content.contains("CREATE_USER_PROFILE"));
        assertTrue(content.contains("UPDATE_USER_PROFILE"));
        assertTrue(content.contains("DELETE_USER_PROFILE"));
        assertTrue(content.contains("Group.USER_PROFILE"));
        assertTrue(content.contains("USER_PROFILE(\"USER_PROFILE\", \"用户信息管理\")"));
    }

    @Test
    void generatePermissionEnum_verifyBaseOnReferences() throws IOException {
        FormDef form = new FormDef();
        form.setName("Product");
        form.setTitle("商品");

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("LIST_PRODUCT(\"LIST_PRODUCT\", \"查看商品\", Group.PRODUCT, null)"));
        assertTrue(content.contains("CREATE_PRODUCT(\"CREATE_PRODUCT\", \"创建商品\", Group.PRODUCT, LIST_PRODUCT)"));
        assertTrue(content.contains("UPDATE_PRODUCT(\"UPDATE_PRODUCT\", \"编辑商品\", Group.PRODUCT, LIST_PRODUCT)"));
        assertTrue(content.contains("DELETE_PRODUCT(\"DELETE_PRODUCT\", \"删除商品\", Group.PRODUCT, LIST_PRODUCT)"));
    }

    @Test
    void generatePermissionEnum_verifyPackageDeclaration() throws IOException {
        FormDef form = new FormDef();
        form.setName("Order");
        form.setTitle("订单");

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.myapp.demo");

        Path file = tempDir.resolve("src/main/java/com/myapp/demo/enums/PermissionEnum.java");
        assertTrue(Files.exists(file));

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.startsWith("package com.myapp.demo.enums;"));
    }

}
