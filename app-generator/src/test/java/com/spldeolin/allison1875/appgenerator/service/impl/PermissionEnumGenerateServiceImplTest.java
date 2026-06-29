package com.spldeolin.allison1875.appgenerator.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.spldeolin.allison1875.formgenerator.dsl.FormDef;

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
        setupSkeletonTemplate("com.example");

        FormDef form = FormDef.builder().name("Order").title("订单").build();

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        assertTrue(Files.exists(file));

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.contains("LIST_ORDER(\"LIST_ORDER\", \"查看订单\", Group.ORDER, null)"));
        assertTrue(content.contains(
                "CREATE_ORDER(\"CREATE_ORDER\", \"创建订单\", Group.ORDER, Lists.newArrayList(LIST_ORDER))"));
        assertTrue(content.contains(
                "UPDATE_ORDER(\"UPDATE_ORDER\", \"编辑订单\", Group.ORDER, Lists.newArrayList(LIST_ORDER))"));
        assertTrue(content.contains(
                "DELETE_ORDER(\"DELETE_ORDER\", \"删除订单\", Group.ORDER, Lists.newArrayList(LIST_ORDER))"));
    }

    @Test
    void generatePermissionEnum_multipleFormsIncludingBuiltinUser() throws IOException {
        setupSkeletonTemplate("com.example");

        FormDef orderForm = FormDef.builder().name("Order").title("订单").build();
        FormDef userForm = FormDef.builder().name("User").title("用户").build();

        List<FormDef> forms = Arrays.asList(orderForm, userForm);
        service.generatePermissionEnum(forms, tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("LIST_ORDER"));
        assertTrue(content.contains("CREATE_ORDER"));
        assertTrue(content.contains("UPDATE_ORDER"));
        assertTrue(content.contains("DELETE_ORDER"));

        assertTrue(content.contains("LIST_USER(\"LIST_USER\", \"查看用户\", Group.USER, null)"));
        assertTrue(content.contains(
                "CREATE_USER(\"CREATE_USER\", \"创建用户\", Group.USER, Lists.newArrayList(LIST_USER))"));
        assertTrue(content.contains(
                "UPDATE_USER(\"UPDATE_USER\", \"编辑用户\", Group.USER, Lists.newArrayList(LIST_USER))"));
        assertTrue(content.contains(
                "DELETE_USER(\"DELETE_USER\", \"删除用户\", Group.USER, Lists.newArrayList(LIST_USER))"));

        assertTrue(content.contains("ORDER(\"ORDER\", \"订单管理\")"));
        assertTrue(content.contains("USER(\"USER\", \"用户管理\")"));
    }

    @Test
    void generatePermissionEnum_multiWordFormName_convertsToUpperSnake() throws IOException {
        setupSkeletonTemplate("com.example");

        FormDef form = FormDef.builder().name("UserProfile").title("用户信息").build();

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
        setupSkeletonTemplate("com.example");

        FormDef form = FormDef.builder().name("Product").title("商品").build();

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("LIST_PRODUCT(\"LIST_PRODUCT\", \"查看商品\", Group.PRODUCT, null)"));
        assertTrue(content.contains(
                "CREATE_PRODUCT(\"CREATE_PRODUCT\", \"创建商品\", Group.PRODUCT, Lists.newArrayList(LIST_PRODUCT))"));
        assertTrue(content.contains(
                "UPDATE_PRODUCT(\"UPDATE_PRODUCT\", \"编辑商品\", Group.PRODUCT, Lists.newArrayList(LIST_PRODUCT))"));
        assertTrue(content.contains(
                "DELETE_PRODUCT(\"DELETE_PRODUCT\", \"删除商品\", Group.PRODUCT, Lists.newArrayList(LIST_PRODUCT))"));
    }

    @Test
    void generatePermissionEnum_verifyPackageDeclaration() throws IOException {
        setupSkeletonTemplate("com.myapp.demo");

        FormDef form = FormDef.builder().name("Order").title("订单").build();

        service.generatePermissionEnum(Collections.singletonList(form), tempDir, "com.myapp.demo");

        Path file = tempDir.resolve("src/main/java/com/myapp/demo/enums/PermissionEnum.java");
        assertTrue(Files.exists(file));

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.startsWith("package com.myapp.demo.enums;"));
    }

    @Test
    void generatePermissionEnum_builtinGrantEntriesRemainAfterInjection() throws IOException {
        setupSkeletonTemplate("com.example");

        FormDef orderForm = FormDef.builder().name("Order").title("订单").build();

        service.generatePermissionEnum(Collections.singletonList(orderForm), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("GRANT_PERMISSION("));
        assertTrue(content.contains("\"GRANT_PERMISSION\", \"授予权限\", Group.ROLE, Lists.newArrayList(LIST_ROLE))"));
        assertTrue(content.contains(
                "GRANT_ROLE(\"GRANT_ROLE\", \"授予角色\", Group.USER, Lists.newArrayList(LIST_USER, LIST_ROLE))"));
    }

    @Test
    void generatePermissionEnum_markersReplacedCleanly() throws IOException {
        setupSkeletonTemplate("com.example");

        FormDef orderForm = FormDef.builder().name("Order").title("订单").build();

        service.generatePermissionEnum(Collections.singletonList(orderForm), tempDir, "com.example");

        Path file = tempDir.resolve("src/main/java/com/example/enums/PermissionEnum.java");
        String content = Files.readString(file, StandardCharsets.UTF_8);

        assertFalse(content.contains("// === 以下枚举项由 app-generator 生成，勿手动修改 ==="));
        assertFalse(content.contains("// === 由 app-generator 生成 ==="));
    }

    private void setupSkeletonTemplate(String namespace) throws IOException {
        String namespacePath = namespace.replace('.', '/');
        Path targetFile = tempDir.resolve("src/main/java/" + namespacePath + "/enums/PermissionEnum.java");
        Files.createDirectories(targetFile.getParent());

        String template = "package " + namespace + ".enums;\n" + "\n" + "import java.util.List;\n"
                + "import com.google.common.collect.Lists;\n" + "\n" + "import lombok.AllArgsConstructor;\n"
                + "import lombok.Getter;\n" + "\n" + "@Getter\n" + "@AllArgsConstructor\n"
                + "public enum PermissionEnum {\n" + "\n"
                + "    LIST_USER(\"LIST_USER\", \"查看用户\", Group.USER, null), CREATE_USER(\"CREATE_USER\", \"创建用户\", "
                + "Group.USER,\n"
                + "            Lists.newArrayList(LIST_USER)), UPDATE_USER(\"UPDATE_USER\", \"编辑用户\", Group.USER,\n"
                + "            Lists.newArrayList(LIST_USER)), DELETE_USER(\"DELETE_USER\", \"删除用户\", Group.USER,\n"
                + "            Lists.newArrayList(LIST_USER)),\n" + "\n"
                + "    LIST_ROLE(\"LIST_ROLE\", \"查看角色\", Group.ROLE, null), CREATE_ROLE(\"CREATE_ROLE\", \"创建角色\", "
                + "Group.ROLE,\n"
                + "            Lists.newArrayList(LIST_ROLE)), UPDATE_ROLE(\"UPDATE_ROLE\", \"编辑角色\", Group.ROLE,\n"
                + "            Lists.newArrayList(LIST_ROLE)), DELETE_ROLE(\"DELETE_ROLE\", \"删除角色\", Group.ROLE,\n"
                + "            Lists.newArrayList(LIST_ROLE)),\n" + "\n"
                + "    GRANT_ROLE(\"GRANT_ROLE\", \"授予角色\", Group.USER, Lists.newArrayList(LIST_USER, LIST_ROLE)), "
                + "GRANT_PERMISSION(\n"
                + "            \"GRANT_PERMISSION\", \"授予权限\", Group.ROLE, Lists.newArrayList(LIST_ROLE)),\n" + "\n"
                + "    // === 以下枚举项由 app-generator 生成，勿手动修改 ===\n" + "    ;\n" + "\n"
                + "    private final String code;\n" + "    private final String title;\n"
                + "    private final Group group;\n" + "\n" + "    private final List<PermissionEnum> baseOn;\n" + "\n"
                + "    @Getter\n" + "    @AllArgsConstructor\n" + "    public enum Group {\n"
                + "        USER(\"USER\", \"用户管理\"), ROLE(\"ROLE\", \"角色管理\"),\n"
                + "        // === 由 app-generator 生成 ===\n" + "        ;\n" + "\n"
                + "        private final String code;\n" + "        private final String title;\n" + "    }\n" + "\n"
                + "}\n";

        Files.writeString(targetFile, template, StandardCharsets.UTF_8);
    }

}
