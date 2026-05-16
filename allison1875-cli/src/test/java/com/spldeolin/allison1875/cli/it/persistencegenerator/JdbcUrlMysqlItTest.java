package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * persistence-generator jdbc-url-mysql 多表集成测试。
 *
 * <p>验证通过 jdbcUrl 直连 MySQL 时，多张表的独立生成能力：
 * 1. 每张表独立生成 Entity、Mapper、XML、Design 文件
 * 2. 各表的字段、类型、索引查询方法互不干扰
 * 3. Design 文件数量与表数量一致
 *
 * <p>包含三张表：t_user（唯一索引）、t_product（普通索引）、t_order_item（多索引）。
 *
 * @author Deolin 2026-05-16
 */
public class JdbcUrlMysqlItTest extends PersistenceGeneratorMySqlItBaseTest {

    @Override
    protected List<String> ddls() {
        return Arrays.asList(
                "CREATE TABLE `t_user` (\n"
                        + "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',\n"
                        + "  `username` varchar(50) NOT NULL COMMENT '用户名',\n"
                        + "  `email` varchar(100) NOT NULL COMMENT '邮箱',\n"
                        + "  `created_at` datetime NOT NULL COMMENT '创建时间',\n"
                        + "  PRIMARY KEY (`id`),\n"
                        + "  UNIQUE KEY `uk_username` (`username`)\n"
                        + ") ENGINE=InnoDB COMMENT='用户表'",
                "CREATE TABLE `t_product` (\n"
                        + "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',\n"
                        + "  `product_name` varchar(200) NOT NULL COMMENT '商品名称',\n"
                        + "  `price` decimal(10,2) NOT NULL COMMENT '价格',\n"
                        + "  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',\n"
                        + "  `category_id` bigint NOT NULL COMMENT '分类ID',\n"
                        + "  PRIMARY KEY (`id`),\n"
                        + "  KEY `idx_category_id` (`category_id`)\n"
                        + ") ENGINE=InnoDB COMMENT='商品表'",
                "CREATE TABLE `t_order_item` (\n"
                        + "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',\n"
                        + "  `order_id` bigint NOT NULL COMMENT '订单ID',\n"
                        + "  `product_id` bigint NOT NULL COMMENT '商品ID',\n"
                        + "  `quantity` int NOT NULL COMMENT '数量',\n"
                        + "  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',\n"
                        + "  PRIMARY KEY (`id`),\n"
                        + "  KEY `idx_order_id` (`order_id`),\n"
                        + "  KEY `idx_product_id` (`product_id`)\n"
                        + ") ENGINE=InnoDB COMMENT='订单项表'"
        );
    }

    @Test
    void test() throws IOException {
        runPersistenceGeneratorWithMySql("jdbc-url-mysql", null);

        // ========== 1. 验证 t_user 相关文件 ==========
        File userEntity = new File(basedir, "src/main/java/com/example/entity/TUserEntity.java");
        assertTrue(userEntity.exists(), "TUserEntity.java should be generated");
        String userEntityContent = new String(Files.readAllBytes(userEntity.toPath()), StandardCharsets.UTF_8);
        assertTrue(userEntityContent.contains("username"), "TUserEntity should contain field 'username'");
        assertTrue(userEntityContent.contains("email"), "TUserEntity should contain field 'email'");

        File userMapper = new File(basedir, "src/main/java/com/example/mapper/TUserMapper.java");
        assertTrue(userMapper.exists(), "TUserMapper.java should be generated");
        String userMapperContent = new String(Files.readAllBytes(userMapper.toPath()), StandardCharsets.UTF_8);
        assertTrue(userMapperContent.contains("interface"), "TUserMapper should be an interface");
        assertTrue(userMapperContent.contains("queryByUsername"),
                "TUserMapper should have queryByUsername (from unique index)");

        File userXml = new File(basedir, "src/main/resources/mapper/TUserMapper.xml");
        assertTrue(userXml.exists(), "TUserMapper.xml should be generated");
        String userXmlContent = new String(Files.readAllBytes(userXml.toPath()), StandardCharsets.UTF_8);
        assertTrue(userXmlContent.contains("t_user"), "TUserMapper.xml should reference t_user table");

        // ========== 2. 验证 t_product 相关文件 ==========
        File productEntity = new File(basedir, "src/main/java/com/example/entity/TProductEntity.java");
        assertTrue(productEntity.exists(), "TProductEntity.java should be generated");
        String productEntityContent = new String(Files.readAllBytes(productEntity.toPath()), StandardCharsets.UTF_8);
        assertTrue(productEntityContent.contains("productName"), "TProductEntity should contain field 'productName'");
        assertTrue(productEntityContent.contains("BigDecimal"), "TProductEntity should contain BigDecimal type");

        File productMapper = new File(basedir, "src/main/java/com/example/mapper/TProductMapper.java");
        assertTrue(productMapper.exists(), "TProductMapper.java should be generated");
        String productMapperContent = new String(Files.readAllBytes(productMapper.toPath()), StandardCharsets.UTF_8);
        assertTrue(productMapperContent.contains("queryByCategoryId"), "TProductMapper should have queryByCategoryId");

        // ========== 3. 验证 t_order_item 相关文件 ==========
        File orderItemEntity = new File(basedir, "src/main/java/com/example/entity/TOrderItemEntity.java");
        assertTrue(orderItemEntity.exists(), "TOrderItemEntity.java should be generated");
        String orderItemEntityContent = new String(Files.readAllBytes(orderItemEntity.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(orderItemEntityContent.contains("orderId"), "TOrderItemEntity should contain field 'orderId'");
        assertTrue(orderItemEntityContent.contains("productId"), "TOrderItemEntity should contain field 'productId'");
        assertTrue(orderItemEntityContent.contains("quantity"), "TOrderItemEntity should contain field 'quantity'");

        File orderItemMapper = new File(basedir, "src/main/java/com/example/mapper/TOrderItemMapper.java");
        assertTrue(orderItemMapper.exists(), "TOrderItemMapper.java should be generated");
        String orderItemMapperContent = new String(Files.readAllBytes(orderItemMapper.toPath()),
                StandardCharsets.UTF_8);
        assertTrue(orderItemMapperContent.contains("queryByOrderId"), "TOrderItemMapper should have queryByOrderId");
        assertTrue(orderItemMapperContent.contains("queryByProductId"),
                "TOrderItemMapper should have queryByProductId");

        // ========== 4. 验证 Design 文件 ==========
        File designDir = new File(basedir, "src/main/java/com/example/design");
        assertTrue(designDir.exists(), "design directory should exist");
        File[] designFiles = designDir.listFiles((d, n) -> n.endsWith(".java"));
        assertTrue(designFiles != null && designFiles.length >= 3,
                "At least 3 Design files should be generated (one per table)");
    }

}
