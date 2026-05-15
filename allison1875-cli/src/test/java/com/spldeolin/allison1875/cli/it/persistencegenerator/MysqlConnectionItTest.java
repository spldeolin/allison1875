package com.spldeolin.allison1875.cli.it.persistencegenerator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * MySQL JDBC 连接测试。
 *
 * <p>仅在通过 Maven 系统属性显式指定 MySQL 连接参数时才执行。
 * 需要以下系统属性全部非空：
 * <ul>
 *   <li>mysql.host</li>
 *   <li>mysql.port</li>
 *   <li>mysql.user</li>
 *   <li>mysql.password</li>
 *   <li>mysql.database</li>
 * </ul>
 *
 * <p>CI 中通过 {@code -Dmysql.host=... -Dmysql.port=...} 传入；
 * 本地开发不传参数时该测试自动跳过。
 *
 * @author Deolin 2026-05-15
 */
public class MysqlConnectionItTest {

    @Test
    void test() {
        String host = prop("mysql.host");
        String port = prop("mysql.port");
        String user = prop("mysql.user");
        String password = prop("mysql.password");
        String database = prop("mysql.database");

        Assumptions.assumeTrue(host != null && port != null && user != null && password != null && database != null,
                "MySQL connection parameters not fully specified via -D flags, skipping test");

        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port,
                database);

        assertDoesNotThrow(() -> {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                assertNotNull(conn, "JDBC Connection should not be null");
                assertTrue(conn.isValid(5), "Connection should be valid");

                DatabaseMetaData meta = conn.getMetaData();
                System.out.println(
                        "MySQL connected: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            }
        }, "Should connect to MySQL via JDBC without exception");
    }

    private static String prop(String key) {
        String value = System.getProperty(key);
        return (value != null && !value.isEmpty()) ? value : null;
    }

}