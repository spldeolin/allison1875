package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * basic-form 集成测试。
 *
 * <p>验证 form-generator 最小 DSL（一个 text 字段 + 一个 number 字段）的完整端到端流程，
 * 包括 DDL、Entity、Mapper、Mapper XML、Controller、Service、DTO 的生成。
 *
 * @author Deolin 2026-05-17
 */
public class BasicFormItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("basic-form");

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = new String(Files.readAllBytes(ddlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        assertTrue(ddl.contains("book_code"), "DDL should contain auto-added bizId column 'book_code'");
        assertTrue(ddl.contains("book_name"), "DDL should contain user-defined column 'book_name'");
        assertTrue(ddl.contains("price"), "DDL should contain user-defined column 'price'");
        assertTrue(ddl.contains("created_at"), "DDL should contain auto-added column 'created_at'");
        assertTrue(ddl.contains("updated_at"), "DDL should contain auto-added column 'updated_at'");
        // 唯一索引
        assertTrue(ddl.contains("uk_book_code") || ddl.contains("UNIQUE"), "DDL should contain unique index on book_code");

        // === Entity 验证 ===
        File entityFile = new File(basedir, "src/main/java/com/example/entity/BookEntity.java");
        assertTrue(entityFile.exists(), "Entity file should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(entityContent.contains("class BookEntity"), "Entity should declare class BookEntity");
        assertTrue(entityContent.contains("bookCode"), "Entity should contain bookCode field");
        assertTrue(entityContent.contains("bookName"), "Entity should contain bookName field");
        assertTrue(entityContent.contains("price"), "Entity should contain price field");
        assertTrue(entityContent.contains("createdAt"), "Entity should contain createdAt field");
        assertTrue(entityContent.contains("updatedAt"), "Entity should contain updatedAt field");

        // === Mapper 验证 ===
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/BookMapper.java");
        assertTrue(mapperFile.exists(), "Mapper interface should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(mapperContent.contains("interface BookMapper"), "Mapper should declare interface BookMapper");
        assertTrue(mapperContent.contains("insert"), "Mapper should contain insert method");
        assertTrue(mapperContent.contains("queryByBookCode"), "Mapper should contain queryByBookCode method");

        // === Mapper XML 验证 ===
        File xmlFile = new File(basedir, "src/main/resources/mapper/BookMapper.xml");
        assertTrue(xmlFile.exists(), "Mapper XML should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("insert"), "Mapper XML should contain insert statement");
        assertTrue(xmlContent.contains("queryByBookCode"), "Mapper XML should contain queryByBookCode statement");

        // === Controller 验证 ===
        File controllerFile = new File(basedir, "src/main/java/com/example/controller/BookController.java");
        assertTrue(controllerFile.exists(), "Controller file should be generated");
        String controllerContent = new String(Files.readAllBytes(controllerFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(controllerContent.contains("class BookController"), "Controller should declare class BookController");
        assertTrue(controllerContent.contains("@RestController"), "Controller should have @RestController");
        assertTrue(controllerContent.contains("@RequestMapping"), "Controller should have @RequestMapping");

        // === Service 验证 ===
        File serviceFile = new File(basedir, "src/main/java/com/example/service/SaveBookService.java");
        assertTrue(serviceFile.exists(), "SaveBookService interface should be generated");
        String serviceContent = new String(Files.readAllBytes(serviceFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(serviceContent.contains("interface SaveBookService"), "Service should declare interface SaveBookService");
        assertTrue(serviceContent.contains("saveBook"), "Service should contain saveBook method");

        File listServiceFile = new File(basedir, "src/main/java/com/example/service/ListBooksService.java");
        assertTrue(listServiceFile.exists(), "ListBooksService interface should be generated");

        File getDetailServiceFile = new File(basedir, "src/main/java/com/example/service/GetBookDetailService.java");
        assertTrue(getDetailServiceFile.exists(), "GetBookDetailService interface should be generated");

        File deleteServiceFile = new File(basedir, "src/main/java/com/example/service/DeleteBookService.java");
        assertTrue(deleteServiceFile.exists(), "DeleteBookService interface should be generated");

        // === ServiceImpl 验证 ===
        File serviceImplFile = new File(basedir, "src/main/java/com/example/service/impl/SaveBookServiceImpl.java");
        assertTrue(serviceImplFile.exists(), "SaveBookServiceImpl file should be generated");
        String serviceImplContent = new String(Files.readAllBytes(serviceImplFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(serviceImplContent.contains("class SaveBookServiceImpl"), "ServiceImpl should declare class SaveBookServiceImpl");
        assertTrue(serviceImplContent.contains("saveBook"), "ServiceImpl should implement saveBook");
        assertTrue(serviceImplContent.contains("bookMapper.insert"), "ServiceImpl should call bookMapper.insert for create");
        assertTrue(serviceImplContent.contains("bookMapper.updateById"), "ServiceImpl should call bookMapper.updateById for edit");

        File listServiceImplFile = new File(basedir, "src/main/java/com/example/service/impl/ListBooksServiceImpl.java");
        assertTrue(listServiceImplFile.exists(), "ListBooksServiceImpl file should be generated");

        File getDetailServiceImplFile = new File(basedir, "src/main/java/com/example/service/impl/GetBookDetailServiceImpl.java");
        assertTrue(getDetailServiceImplFile.exists(), "GetBookDetailServiceImpl file should be generated");

        File deleteServiceImplFile = new File(basedir, "src/main/java/com/example/service/impl/DeleteBookServiceImpl.java");
        assertTrue(deleteServiceImplFile.exists(), "DeleteBookServiceImpl file should be generated");

        // === DTO 验证 ===
        File saveReqFile = new File(basedir, "src/main/java/com/example/dto/req/SaveBookReq.java");
        assertTrue(saveReqFile.exists(), "SaveBookReq DTO should be generated");
        String saveReqContent = new String(Files.readAllBytes(saveReqFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(saveReqContent.contains("class SaveBookReq"), "Should contain class SaveBookReq");
        assertTrue(saveReqContent.contains("bookName"), "SaveReq should contain bookName field");
        assertTrue(saveReqContent.contains("price"), "SaveReq should contain price field");

        File listReqFile = new File(basedir, "src/main/java/com/example/dto/req/ListBooksReq.java");
        assertTrue(listReqFile.exists(), "ListBooksReq DTO should be generated");

        File getDetailReqFile = new File(basedir, "src/main/java/com/example/dto/req/GetBookDetailReq.java");
        assertTrue(getDetailReqFile.exists(), "GetBookDetailReq DTO should be generated");

        File deleteReqFile = new File(basedir, "src/main/java/com/example/dto/req/DeleteBookReq.java");
        assertTrue(deleteReqFile.exists(), "DeleteBookReq DTO should be generated");

        File saveRespFile = new File(basedir, "src/main/java/com/example/dto/resp/SaveBookResp.java");
        assertTrue(saveRespFile.exists(), "SaveBookResp DTO should be generated");

        File listRespFile = new File(basedir, "src/main/java/com/example/dto/resp/ListBooksResp.java");
        assertTrue(listRespFile.exists(), "ListBooksResp DTO should be generated");
        String listRespContent = new String(Files.readAllBytes(listRespFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(listRespContent.contains("bookName"), "ListResp should contain bookName field");
        assertTrue(listRespContent.contains("price"), "ListResp should contain price field");

        File getDetailRespFile = new File(basedir, "src/main/java/com/example/dto/resp/GetBookDetailResp.java");
        assertTrue(getDetailRespFile.exists(), "GetBookDetailResp DTO should be generated");

        // === Design 验证 ===
        File designFile = new File(basedir, "src/main/java/com/example/design/BookDesign.java");
        assertTrue(designFile.exists(), "Design file should be generated");
        String designContent = new String(Files.readAllBytes(designFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(designContent.contains("class BookDesign"), "Design should declare class BookDesign");

        // === 验证没有生成 api-docs 目录 ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertTrue(apiDocsDir.exists(), "api-docs directory should be generated");
    }

}
