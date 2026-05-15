package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator all-column-types 集成测试。
 *
 * <p>验证所有 MySQL 列类型到 Java 类型的正确映射：
 * 1. Entity Java 文件生成及类型映射
 * 2. Mapper 接口生成
 * 3. Mapper XML 文件生成及列名包含
 * 4. Design 文件生成
 *
 * @author Deolin 2026-05-14
 */
public class AllColumnTypesItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("all-column-types");

        // ========== 1. 验证 Entity 文件生成及类型映射 ==========
        File entityFile = new File(basedir, "src/main/java/com/example/entity/TAllTypesEntity.java");
        assertTrue(entityFile.exists(), "TAllTypesEntity.java should be generated");
        String entityContent = new String(Files.readAllBytes(entityFile.toPath()), StandardCharsets.UTF_8);

        // tinyint(1) -> Boolean
        assertTrue(entityContent.contains("Boolean"), "tinyint(1) should map to Boolean");
        assertTrue(entityContent.contains("colBoolean"), "col_boolean should map to colBoolean");

        // tinyint (non-boolean) -> Byte
        assertTrue(entityContent.contains("Byte"), "tinyint should map to Byte");
        assertTrue(entityContent.contains("colTinyint"), "col_tinyint should map to colTinyint");

        // int -> Integer
        assertTrue(entityContent.contains("Integer"), "int should map to Integer");
        assertTrue(entityContent.contains("colInt"), "col_int should map to colInt");

        // bigint -> Long
        assertTrue(entityContent.contains("Long"), "bigint should map to Long");

        // double -> Double
        assertTrue(entityContent.contains("Double"), "double should map to Double");
        assertTrue(entityContent.contains("colDouble"), "col_double should map to colDouble");

        // decimal -> BigDecimal
        assertTrue(entityContent.contains("BigDecimal"), "decimal should map to BigDecimal");
        assertTrue(entityContent.contains("colDecimal"), "col_decimal should map to colDecimal");

        // varchar, char, text, mediumtext -> String
        assertTrue(entityContent.contains("colVarchar"), "col_varchar should map to colVarchar");
        assertTrue(entityContent.contains("colChar"), "col_char should map to colChar");
        assertTrue(entityContent.contains("colText"), "col_text should map to colText");
        assertTrue(entityContent.contains("colMediumtext"), "col_mediumtext should map to colMediumtext");

        // date -> LocalDate
        assertTrue(entityContent.contains("LocalDate"), "date should map to LocalDate");
        assertTrue(entityContent.contains("colDate"), "col_date should map to colDate");

        // time -> LocalTime
        assertTrue(entityContent.contains("LocalTime"), "time should map to LocalTime");
        assertTrue(entityContent.contains("colTime"), "col_time should map to colTime");

        // datetime, timestamp -> LocalDateTime
        assertTrue(entityContent.contains("LocalDateTime"), "datetime/timestamp should map to LocalDateTime");
        assertTrue(entityContent.contains("colDatetime"), "col_datetime should map to colDatetime");
        assertTrue(entityContent.contains("colTimestamp"), "col_timestamp should map to colTimestamp");

        // 验证包声明
        assertTrue(entityContent.contains("package com.example.entity"), "Entity should have correct package");

        // ========== 2. 验证 Mapper 接口生成 ==========
        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TAllTypesMapper.java");
        assertTrue(mapperFile.exists(), "TAllTypesMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("interface"), "Mapper file should be an interface");
        assertTrue(mapperContent.contains("TAllTypesMapper"), "Mapper should be named TAllTypesMapper");
        assertTrue(mapperContent.contains("insert"), "Mapper should contain insert method");
        assertTrue(mapperContent.contains("batchInsert"), "Mapper should contain batchInsert method");
        assertTrue(mapperContent.contains("updateById"), "Mapper should contain updateById method");
        assertTrue(mapperContent.contains("deleteById"), "Mapper should contain deleteById method");
        assertTrue(mapperContent.contains("queryById"), "Mapper should contain queryById method");
        assertTrue(mapperContent.contains("queryByIds"), "Mapper should contain queryByIds method");
        assertTrue(mapperContent.contains("package com.example.mapper"), "Mapper should have correct package");

        // ========== 3. 验证 Mapper XML 文件生成 ==========
        File xmlFile = new File(basedir, "src/main/resources/mapper/TAllTypesMapper.xml");
        assertTrue(xmlFile.exists(), "TAllTypesMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);

        // 验证基础结构
        assertTrue(xmlContent.contains("resultMap"), "XML should contain resultMap");
        assertTrue(xmlContent.contains("t_all_types"), "XML should reference table name 't_all_types'");
        assertTrue(xmlContent.contains("TAllTypesMapper") || xmlContent.contains("com.example.mapper.TAllTypesMapper"),
                "XML should reference mapper namespace");

        // 验证所有列在XML中出现
        assertTrue(xmlContent.contains("col_boolean"), "XML should contain col_boolean");
        assertTrue(xmlContent.contains("col_tinyint"), "XML should contain col_tinyint");
        assertTrue(xmlContent.contains("col_int"), "XML should contain col_int");
        assertTrue(xmlContent.contains("col_bigint"), "XML should contain col_bigint");
        assertTrue(xmlContent.contains("col_double"), "XML should contain col_double");
        assertTrue(xmlContent.contains("col_decimal"), "XML should contain col_decimal");
        assertTrue(xmlContent.contains("col_varchar"), "XML should contain col_varchar");
        assertTrue(xmlContent.contains("col_char"), "XML should contain col_char");
        assertTrue(xmlContent.contains("col_text"), "XML should contain col_text");
        assertTrue(xmlContent.contains("col_mediumtext"), "XML should contain col_mediumtext");
        assertTrue(xmlContent.contains("col_date"), "XML should contain col_date");
        assertTrue(xmlContent.contains("col_time"), "XML should contain col_time");
        assertTrue(xmlContent.contains("col_datetime"), "XML should contain col_datetime");
        assertTrue(xmlContent.contains("col_timestamp"), "XML should contain col_timestamp");

        // ========== 4. 验证 Design 文件生成 ==========
        File designDir = new File(basedir, "src/main/java/com/example/design");
        assertTrue(designDir.exists(), "design directory should exist");

        File[] designFiles = designDir.listFiles((dir, name) -> name.endsWith(".java") && name.contains("TAllTypes"));
        assertTrue(designFiles != null && designFiles.length > 0,
                "At least one Design file should be generated for TAllTypes");

        String designContent = new String(Files.readAllBytes(designFiles[0].toPath()), StandardCharsets.UTF_8);
        assertTrue(designContent.contains("package com.example.design"), "Design should have correct package");
        assertTrue(designContent.contains("class") || designContent.contains("interface"),
                "Design should contain a type declaration");
    }

}
