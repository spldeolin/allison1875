package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * multi-select-item 集成测试。
 *
 * <p>验证 multiSelect 类型字段的关联表生成：
 * <ul>
 *   <li>生成额外的关联表（association form）DDL：{@code CREATE TABLE student_hobbies}，包含主表业务主键列和选项 code 列</li>
 *   <li>关联表生成独立的 Entity/Mapper/XML</li>
 *   <li>Save API 的 Service 逻辑中包含「先删后建」关联实体的 forEach 循环</li>
 *   <li>Delete API 的 Service 逻辑中包含关联表的级联删除</li>
 *   <li>GetDetail API 的 Service 逻辑中包含关联表查询 + stream map 转枚举</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class MultiSelectItemItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("multi-select-item");

        // === DDL 验证：主表 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = Files.readString(ddlFile.toPath());
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // 主表 student
        assertTrue(ddl.contains("CREATE TABLE `student`"), "DDL should contain main table 'student'");
        assertTrue(ddl.contains("`student_name` VARCHAR(50) NOT NULL"),
                "Main table should contain student_name column");
        assertTrue(ddl.contains("`student_code`"), "Main table should contain bizId column");
        assertTrue(ddl.contains("`created_at`"), "Main table should contain created_at column");
        assertTrue(ddl.contains("`updated_at`"), "Main table should contain updated_at column");
        // 主表不应包含 multiSelect 字段（multiSelect 存储在关联表中）
        assertFalse(ddl.substring(0, ddl.indexOf("CREATE TABLE `student_hobbies`")).contains("`hobbies`"),
                "Main table should NOT contain hobbies column (stored in association table)");

        // === DDL 验证：关联表 ===
        assertTrue(ddl.contains("CREATE TABLE `student_hobbies`"),
                "DDL should contain association table 'student_hobbies'");
        String associationDdl = ddl.substring(ddl.indexOf("CREATE TABLE `student_hobbies`"));
        assertTrue(associationDdl.contains("`student_code` VARCHAR(36) NOT NULL"),
                "Association table should contain student_code column");
        assertTrue(associationDdl.contains("`hobbies` VARCHAR(64) NOT NULL"),
                "Association table should contain hobbies VARCHAR(64) column");
        assertTrue(associationDdl.contains("`created_at` DATETIME NOT NULL"),
                "Association table should contain created_at column");
        // 关联表应有唯一索引 (student_code, hobbies)
        assertTrue(associationDdl.contains("uk_student_code_hobbies") || associationDdl.contains("UNIQUE"),
                "Association table should have unique index on (student_code, hobbies)");

        // === Enum 验证 ===
        File enumFile = new File(basedir, "src/main/java/com/example/enums/HobbiesEnum.java");
        assertTrue(enumFile.exists(), "HobbiesEnum file should be generated");
        String enumContent = new String(Files.readAllBytes(enumFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(enumContent.contains("enum HobbiesEnum"), "Should declare enum HobbiesEnum");
        assertTrue(enumContent.contains("READING"), "Enum should contain READING entry");
        assertTrue(enumContent.contains("SPORTS"), "Enum should contain SPORTS entry");
        assertTrue(enumContent.contains("MUSIC"), "Enum should contain MUSIC entry");
        assertTrue(enumContent.contains("public static HobbiesEnum of"), "Enum should have of() method");

        // === Entity 验证：主表 ===
        File entityFile = new File(basedir, "src/main/java/com/example/entity/StudentEntity.java");
        assertTrue(entityFile.exists(), "StudentEntity file should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("class StudentEntity"), "Entity should declare class StudentEntity");
        assertTrue(entityContent.contains("studentName"), "Entity should contain studentName field");
        assertTrue(entityContent.contains("studentCode"), "Entity should contain studentCode field");
        // 主表 Entity 不应包含 hobbies 字段
        assertFalse(entityContent.contains("hobbies"), "StudentEntity should NOT contain hobbies field");

        // === Entity 验证：关联表 ===
        File associationEntityFile = new File(basedir,
                "src/main/java/com/example/entity/StudentHobbiesEntity.java");
        assertTrue(associationEntityFile.exists(), "StudentHobbiesEntity file should be generated");
        String associationEntityContent = new String(Files.readAllBytes(associationEntityFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(associationEntityContent.contains("class StudentHobbiesEntity"),
                "Should declare class StudentHobbiesEntity");
        assertTrue(associationEntityContent.contains("studentCode"),
                "Association entity should contain studentCode field");
        assertTrue(associationEntityContent.contains("String hobbies"),
                "Association entity should contain String hobbies field");
        assertTrue(associationEntityContent.contains("createdAt"),
                "Association entity should contain createdAt field");

        // === Mapper 验证：主表 ===
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/StudentMapper.java");
        assertTrue(mapperFile.exists(), "StudentMapper should be generated");

        // === Mapper 验证：关联表 ===
        File associationMapperFile = new File(basedir,
                "src/main/java/com/example/mapper/StudentHobbiesMapper.java");
        assertTrue(associationMapperFile.exists(), "StudentHobbiesMapper should be generated");
        String associationMapperContent = new String(Files.readAllBytes(associationMapperFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(associationMapperContent.contains("deleteByStudentCode"),
                "Association Mapper should contain deleteByStudentCode method");
        assertTrue(associationMapperContent.contains("queryByStudentCode"),
                "Association Mapper should contain queryByStudentCode method");

        // === Create ServiceImpl 验证：先删后建 ===
        File createServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/CreateStudentServiceImpl.java");
        assertTrue(createServiceImplFile.exists(), "CreateStudentServiceImpl should be generated");
        String createServiceImplContent = new String(Files.readAllBytes(createServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        // 「后创建」 — forEach 循环创建新的关联实体
        assertTrue(createServiceImplContent.contains("req.getHobbies()"),
                "Create service should iterate over req.getHobbies()");
        assertTrue(createServiceImplContent.contains("new StudentHobbiesEntity()"),
                "Create service should create new StudentHobbiesEntity instances");
        assertTrue(createServiceImplContent.contains("studentHobbiesMapper.insert(studentHobbies)"),
                "Create service should insert new association entities");
        // 主实体不应设置 hobbies 字段
        assertFalse(createServiceImplContent.contains("student.setHobbies"),
                "Create service should NOT set hobbies on main entity");

        // === Delete ServiceImpl 验证：级联删除 ===
        // 注意：Design Chain 已被 query-transformer 转换为 Mapper 调用
        File deleteServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/DeleteStudentServiceImpl.java");
        assertTrue(deleteServiceImplFile.exists(), "DeleteStudentServiceImpl should be generated");
        String deleteServiceImplContent = new String(Files.readAllBytes(deleteServiceImplFile.toPath()),
                StandardCharsets.UTF_8);
        // 主表删除
        assertTrue(deleteServiceImplContent.contains("studentMapper.deleteStudent("),
                "Delete service should delete from main table via studentMapper");
        // 关联表级联删除
        assertTrue(deleteServiceImplContent.contains("studentHobbiesMapper.deleteStudentHobbies("),
                "Delete service should cascade delete association table via studentHobbiesMapper");

        // === GetDetail ServiceImpl 验证：关联查询 + stream map ===
        File getDetailServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/GetStudentDetailServiceImpl.java");
        assertTrue(getDetailServiceImplFile.exists(), "GetStudentDetailServiceImpl should be generated");
        String getDetailServiceImplContent = new String(
                Files.readAllBytes(getDetailServiceImplFile.toPath()), StandardCharsets.UTF_8);
        // 查询关联表
        assertTrue(getDetailServiceImplContent.contains("studentHobbiesMapper.queryByStudentCode("),
                "GetDetail service should query association table by bizId");
        // stream map 转换为枚举
        assertTrue(getDetailServiceImplContent.contains("StudentHobbiesEntity::getHobbies"),
                "GetDetail service should map StudentHobbiesEntity::getHobbies");
        assertTrue(getDetailServiceImplContent.contains("HobbiesEnum::of"),
                "GetDetail service should map HobbiesEnum::of");
        assertTrue(getDetailServiceImplContent.contains("Collectors.toList()"),
                "GetDetail service should collect to List");
        // 设置返回值
        assertTrue(getDetailServiceImplContent.contains("result.setHobbies(hobbies)"),
                "GetDetail service should set hobbies on result");

        // === DTO 验证：CreateReq 包含 List<Enum> ===
        File createReqFile = new File(basedir, "src/main/java/com/example/dto/req/CreateStudentReq.java");
        assertTrue(createReqFile.exists(), "CreateStudentReq DTO should be generated");
        String createReqContent = new String(Files.readAllBytes(createReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(createReqContent.contains("List<HobbiesEnum> hobbies"),
                "CreateReq should contain List<HobbiesEnum> hobbies field");
        assertTrue(
                createReqContent.contains("@NotEmpty")
                        || createReqContent.contains("@javax.validation.constraints.NotEmpty"),
                "nonVoid multiSelect field should have @NotEmpty");

        // === DTO 验证：GetDetailResp 包含 List<Enum> ===
        File getDetailRespFile = new File(basedir,
                "src/main/java/com/example/dto/resp/GetStudentDetailResp.java");
        assertTrue(getDetailRespFile.exists(), "GetStudentDetailResp DTO should be generated");
        String getDetailRespContent = new String(Files.readAllBytes(getDetailRespFile.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(getDetailRespContent.contains("List<HobbiesEnum> hobbies"),
                "GetDetailResp should contain List<HobbiesEnum> hobbies field");

        // === DTO 验证：ListReq 包含 List<Enum> 过滤 ===
        File listReqFile = new File(basedir, "src/main/java/com/example/dto/req/ListStudentsReq.java");
        assertTrue(listReqFile.exists(), "ListStudentsReq DTO should be generated");
        String listReqContent = new String(Files.readAllBytes(listReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listReqContent.contains("List<HobbiesEnum> hobbies"),
                "ListReq should contain List<HobbiesEnum> for multiSelect in-filter");

        // === 验证没有生成 api-docs 目录 ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated");
    }

}
