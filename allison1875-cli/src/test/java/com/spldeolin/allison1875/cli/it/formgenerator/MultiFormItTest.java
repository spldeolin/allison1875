package com.spldeolin.allison1875.cli.it.formgenerator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * multi-form 集成测试。
 *
 * <p>验证 DSL 包含多个 FormDef（Book + Author）时，每个表单独立生成全套文件：
 * <ul>
 *   <li>每个表单生成独立的 DDL、Entity、Mapper、Mapper XML、Controller、Service、DTO、Design 文件</li>
 *   <li>各表单生成的文件互不干扰（Book 生成的文件中不应出现 Author，反之亦然）</li>
 *   <li>每个 Controller 的 {@code @RequestMapping} 使用各自 {@code formName} 对应的路径</li>
 *   <li>select 字段的枚举仅在需要它的表单中生成</li>
 * </ul>
 *
 * @author Deolin 2026-05-17
 */
public class MultiFormItTest extends FormGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runFormGenerator("multi-form");

        // ============================================================
        // === Book 表单验证 ===
        // ============================================================

        // === DDL 验证 ===
        File ddlFile = new File(basedir, "sql/ddl.sql");
        assertTrue(ddlFile.exists(), "DDL file should be generated at sql/ddl.sql");
        String ddl = Files.readString(ddlFile.toPath());
        assertTrue(ddl.contains("CREATE TABLE"), "DDL should contain CREATE TABLE");
        // Book 表
        assertTrue(ddl.contains("CREATE TABLE `book`"), "DDL should contain Book table");
        assertTrue(ddl.contains("`book_code`"), "DDL should contain Book bizId column");
        assertTrue(ddl.contains("`book_name`"), "DDL should contain Book book_name column");
        assertTrue(ddl.contains("`price`"), "DDL should contain Book price column");
        // Author 表
        assertTrue(ddl.contains("CREATE TABLE `author`"), "DDL should contain Author table");
        assertTrue(ddl.contains("`author_code`"), "DDL should contain Author bizId column");
        assertTrue(ddl.contains("`author_name`"), "DDL should contain Author author_name column");
        assertTrue(ddl.contains("`nationality`"), "DDL should contain Author nationality column");

        // === Entity 验证：Book ===
        File bookEntityFile = new File(basedir, "src/main/java/com/example/entity/BookEntity.java");
        assertTrue(bookEntityFile.exists(), "BookEntity file should be generated");
        String bookEntityContent = Files.readString(bookEntityFile.toPath());
        assertTrue(bookEntityContent.contains("class BookEntity"), "Should declare class BookEntity");
        assertTrue(bookEntityContent.contains("bookName"), "BookEntity should contain bookName");
        assertTrue(bookEntityContent.contains("price"), "BookEntity should contain price");
        assertTrue(bookEntityContent.contains("bookCode"), "BookEntity should contain bookCode (auto bizId)");
        assertTrue(bookEntityContent.contains("createdAt"), "BookEntity should contain createdAt");
        assertTrue(bookEntityContent.contains("updatedAt"), "BookEntity should contain updatedAt");
        // Book 不包含 Author 的字段
        assertFalse(bookEntityContent.contains("authorName"),
                "BookEntity should NOT contain authorName (belongs to Author)");
        assertFalse(bookEntityContent.contains("nationality"),
                "BookEntity should NOT contain nationality (belongs to Author)");

        // === Entity 验证：Author ===
        File authorEntityFile = new File(basedir, "src/main/java/com/example/entity/AuthorEntity.java");
        assertTrue(authorEntityFile.exists(), "AuthorEntity file should be generated");
        String authorEntityContent = Files.readString(authorEntityFile.toPath());
        assertTrue(authorEntityContent.contains("class AuthorEntity"), "Should declare class AuthorEntity");
        assertTrue(authorEntityContent.contains("authorName"), "AuthorEntity should contain authorName");
        assertTrue(authorEntityContent.contains("nationality"), "AuthorEntity should contain nationality");
        assertTrue(authorEntityContent.contains("authorCode"), "AuthorEntity should contain authorCode (auto bizId)");
        // Author 不包含 Book 的字段
        assertFalse(authorEntityContent.contains("bookName"),
                "AuthorEntity should NOT contain bookName (belongs to Book)");
        assertFalse(authorEntityContent.contains("price"),
                "AuthorEntity should NOT contain price (belongs to Book)");

        // === Mapper 验证：Book ===
        File bookMapperFile = new File(basedir, "src/main/java/com/example/mapper/BookMapper.java");
        assertTrue(bookMapperFile.exists(), "BookMapper interface should be generated");
        String bookMapperContent = Files.readString(bookMapperFile.toPath());
        assertTrue(bookMapperContent.contains("interface BookMapper"), "Should declare interface BookMapper");
        assertTrue(bookMapperContent.contains("insert"), "BookMapper should contain insert method");

        // === Mapper 验证：Author ===
        File authorMapperFile = new File(basedir, "src/main/java/com/example/mapper/AuthorMapper.java");
        assertTrue(authorMapperFile.exists(), "AuthorMapper interface should be generated");
        String authorMapperContent = Files.readString(authorMapperFile.toPath());
        assertTrue(authorMapperContent.contains("interface AuthorMapper"), "Should declare interface AuthorMapper");
        assertTrue(authorMapperContent.contains("insert"), "AuthorMapper should contain insert method");

        // === Mapper XML 验证：Book ===
        File bookXmlFile = new File(basedir, "src/main/resources/mapper/BookMapper.xml");
        assertTrue(bookXmlFile.exists(), "BookMapper XML should be generated");
        String bookXmlContent = Files.readString(bookXmlFile.toPath());
        assertTrue(bookXmlContent.contains("insert"), "BookMapper XML should contain insert statement");
        assertTrue(bookXmlContent.contains("book_code"), "BookMapper XML should reference book_code");

        // === Mapper XML 验证：Author ===
        File authorXmlFile = new File(basedir, "src/main/resources/mapper/AuthorMapper.xml");
        assertTrue(authorXmlFile.exists(), "AuthorMapper XML should be generated");
        String authorXmlContent = Files.readString(authorXmlFile.toPath());
        assertTrue(authorXmlContent.contains("insert"), "AuthorMapper XML should contain insert statement");

        // === Controller 验证：Book ===
        File bookControllerFile = new File(basedir, "src/main/java/com/example/controller/BookController.java");
        assertTrue(bookControllerFile.exists(), "BookController file should be generated");
        String bookControllerContent = Files.readString(bookControllerFile.toPath());
        assertTrue(bookControllerContent.contains("class BookController"), "Should declare class BookController");
        assertTrue(bookControllerContent.contains("@RestController"), "BookController should have @RestController");
        assertTrue(bookControllerContent.contains("@RequestMapping("), "BookController should have @RequestMapping");
        // 路径使用 formName → varName（Book → book）
        assertTrue(bookControllerContent.contains("/api/v1/book"),
                "BookController @RequestMapping should use '/api/v1/book'");

        // === Controller 验证：Author ===
        File authorControllerFile = new File(basedir, "src/main/java/com/example/controller/AuthorController.java");
        assertTrue(authorControllerFile.exists(), "AuthorController file should be generated");
        String authorControllerContent = Files.readString(authorControllerFile.toPath());
        assertTrue(authorControllerContent.contains("class AuthorController"),
                "Should declare class AuthorController");
        assertTrue(authorControllerContent.contains("@RestController"), "AuthorController should have @RestController");
        // 路径使用 formName → varName（Author → author）
        assertTrue(authorControllerContent.contains("/api/v1/author"),
                "AuthorController @RequestMapping should use '/api/v1/author'");

        // === Service 验证：Book ===
        File saveBookServiceFile = new File(basedir, "src/main/java/com/example/service/SaveBookService.java");
        assertTrue(saveBookServiceFile.exists(), "SaveBookService interface should be generated");
        File listBooksServiceFile = new File(basedir, "src/main/java/com/example/service/ListBooksService.java");
        assertTrue(listBooksServiceFile.exists(), "ListBooksService interface should be generated");
        File getBookDetailServiceFile = new File(basedir,
                "src/main/java/com/example/service/GetBookDetailService.java");
        assertTrue(getBookDetailServiceFile.exists(), "GetBookDetailService interface should be generated");
        File deleteBookServiceFile = new File(basedir, "src/main/java/com/example/service/DeleteBookService.java");
        assertTrue(deleteBookServiceFile.exists(), "DeleteBookService interface should be generated");

        // === ServiceImpl 验证：Book ===
        File saveBookServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveBookServiceImpl.java");
        assertTrue(saveBookServiceImplFile.exists(), "SaveBookServiceImpl file should be generated");
        String saveBookServiceImplContent = Files.readString(saveBookServiceImplFile.toPath());
        assertTrue(saveBookServiceImplContent.contains("class SaveBookServiceImpl"),
                "Should declare class SaveBookServiceImpl");
        assertTrue(saveBookServiceImplContent.contains("bookMapper.insert"),
                "SaveBookServiceImpl should call bookMapper.insert");
        // 不引用 Author
        assertFalse(saveBookServiceImplContent.contains("authorMapper"),
                "SaveBookServiceImpl should NOT reference authorMapper");
        assertFalse(saveBookServiceImplContent.contains("AuthorEntity"),
                "SaveBookServiceImpl should NOT reference AuthorEntity");

        // === ServiceImpl 验证：Author ===
        File saveAuthorServiceImplFile = new File(basedir,
                "src/main/java/com/example/service/impl/SaveAuthorServiceImpl.java");
        assertTrue(saveAuthorServiceImplFile.exists(), "SaveAuthorServiceImpl file should be generated");
        String saveAuthorServiceImplContent = Files.readString(saveAuthorServiceImplFile.toPath());
        assertTrue(saveAuthorServiceImplContent.contains("class SaveAuthorServiceImpl"),
                "Should declare class SaveAuthorServiceImpl");
        assertTrue(saveAuthorServiceImplContent.contains("authorMapper.insert"),
                "SaveAuthorServiceImpl should call authorMapper.insert");
        // Author Save 应该包含 nationality 枚举的 .getCode() 转换（枚举 → String 存入 Entity）
        assertTrue(saveAuthorServiceImplContent.contains("req.getNationality().getCode()"),
                "SaveAuthorServiceImpl should call req.getNationality().getCode()");

        // === DTO 验证：Book ===
        assertTrue(new File(basedir, "src/main/java/com/example/dto/req/SaveBookReq.java").exists(),
                "SaveBookReq should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/req/ListBooksReq.java").exists(),
                "ListBooksReq should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/req/GetBookDetailReq.java").exists(),
                "GetBookDetailReq should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/req/DeleteBookReq.java").exists(),
                "DeleteBookReq should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/resp/SaveBookResp.java").exists(),
                "SaveBookResp should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/resp/ListBooksResp.java").exists(),
                "ListBooksResp should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/resp/GetBookDetailResp.java").exists(),
                "GetBookDetailResp should be generated");

        // === DTO 验证：Author ===
        assertTrue(new File(basedir, "src/main/java/com/example/dto/req/SaveAuthorReq.java").exists(),
                "SaveAuthorReq should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/req/ListAuthorsReq.java").exists(),
                "ListAuthorsReq should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/req/GetAuthorDetailReq.java").exists(),
                "GetAuthorDetailReq should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/req/DeleteAuthorReq.java").exists(),
                "DeleteAuthorReq should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/resp/SaveAuthorResp.java").exists(),
                "SaveAuthorResp should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/resp/ListAuthorsResp.java").exists(),
                "ListAuthorsResp should be generated");
        assertTrue(new File(basedir, "src/main/java/com/example/dto/resp/GetAuthorDetailResp.java").exists(),
                "GetAuthorDetailResp should be generated");

        // === Enum 验证：仅 NationalityEnum（Author 需要） ===
        File nationalityEnumFile = new File(basedir, "src/main/java/com/example/enums/NationalityEnum.java");
        assertTrue(nationalityEnumFile.exists(), "NationalityEnum file should be generated for Author");
        String nationalityEnumContent = Files.readString(nationalityEnumFile.toPath());
        assertTrue(nationalityEnumContent.contains("enum NationalityEnum"),
                "Should declare enum NationalityEnum");
        assertTrue(nationalityEnumContent.contains("CN"), "NationalityEnum should contain CN");
        assertTrue(nationalityEnumContent.contains("US"), "NationalityEnum should contain US");
        assertTrue(nationalityEnumContent.contains("JP"), "NationalityEnum should contain JP");
        // Book 没有 select/multiSelect 字段，不应生成 Book 相关枚举
        assertFalse(nationalityEnumContent.contains("Book"), "NationalityEnum should NOT reference Book");

        // === Design 验证：两个表单各自生成 Design ===
        File bookDesignFile = new File(basedir, "src/main/java/com/example/design/BookDesign.java");
        assertTrue(bookDesignFile.exists(), "BookDesign file should be generated");
        String bookDesignContent = Files.readString(bookDesignFile.toPath());
        assertTrue(bookDesignContent.contains("class BookDesign"), "Should declare class BookDesign");

        File authorDesignFile = new File(basedir, "src/main/java/com/example/design/AuthorDesign.java");
        assertTrue(authorDesignFile.exists(), "AuthorDesign file should be generated");
        String authorDesignContent = Files.readString(authorDesignFile.toPath());
        assertTrue(authorDesignContent.contains("class AuthorDesign"), "Should declare class AuthorDesign");

        // === 验证没有生成 api-docs 目录（enableDocAnalyzer=false） ===
        File apiDocsDir = new File(basedir, "api-docs");
        assertFalse(apiDocsDir.exists(), "api-docs directory should NOT be generated when enableDocAnalyzer=false");
    }

}
