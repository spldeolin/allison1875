package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator biz-id-unique-index 集成测试。
 *
 * @author Deolin 2026-05-16
 */
public class BizIdUniqueIndexItTest extends PersistenceGeneratorItBaseTest {

    @Test
    void test() throws IOException {
        runPersistenceGenerator("biz-id-unique-index");

        File mapperFile = new File(basedir, "src/main/java/com/example/mapper/TCouponMapper.java");
        assertTrue(mapperFile.exists(), "TCouponMapper.java should be generated");
        String mapperContent = new String(Files.readAllBytes(mapperFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(mapperContent.contains("queryByCouponCode"), "Should contain queryByCouponCode from unique index");
        assertTrue(mapperContent.contains("queryByCouponCodes"), "Should contain queryByCouponCodes for biz id batch");
        assertTrue(mapperContent.contains("queryByCouponCodesEachCouponCode"),
                "Should contain queryByCouponCodesEachCouponCode");

        File xmlFile = new File(basedir, "src/main/resources/mapper/TCouponMapper.xml");
        assertTrue(xmlFile.exists(), "TCouponMapper.xml should be generated");
        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("queryByCouponCodes"), "XML should contain queryByCouponCodes");
        assertTrue(xmlContent.contains("coupon_code IN"), "XML should query by coupon_code IN clause");
    }

}
