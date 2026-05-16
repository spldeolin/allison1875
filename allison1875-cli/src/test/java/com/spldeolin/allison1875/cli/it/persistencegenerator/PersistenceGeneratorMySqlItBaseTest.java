package com.spldeolin.allison1875.cli.it.persistencegenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import com.spldeolin.allison1875.cli.Bootstrap;

/**
 * persistence-generator MySQL 集成测试基类。
 *
 * <p>继承 {@link PersistenceGeneratorItBaseTest}，新增 MySQL 直连能力：
 * ①通过系统属性（mysql.host / mysql.port / mysql.user / mysql.password / mysql.database）读取数据库连接参数，
 *   参数不齐时自动跳过测试（Assumptions）；
 * ②在 MySQL 中创建测试表（子类通过 {@link #ddls()} 提供 DDL 列表）；
 * ③将 jdbcUrl、userName、password、schema 注入 .allison1875.yml；
 * ④测试结束后自动清理测试表。
 *
 * <p>CI 中通过 {@code -Dmysql.host=... -Dmysql.port=...} 传入连接参数；
 * 本地开发不传参数时该测试自动跳过。
 *
 * <p>子类只需：
 * <ul>
 *   <li>实现 {@link #ddls()} 返回要创建的 MySQL DDL 列表；</li>
 *   <li>在 @Test 方法中调用 {@link #runPersistenceGenerator(String)} 或
 *       {@link #runPersistenceGeneratorWithMySql(String)}；</li>
 *   <li>基于 {@link #basedir} 编写断言。</li>
 * </ul>
 *
 * @author Deolin 2026-05-16
 */
public abstract class PersistenceGeneratorMySqlItBaseTest extends PersistenceGeneratorItBaseTest {

    protected String jdbcUrl;
    protected String schema;
    protected String userName;
    protected String password;

    /**
     * 子类返回需要创建的 MySQL DDL 语句列表。
     * 基类负责在测试前执行这些 DDL，在测试后执行对应的 DROP TABLE。
     */
    protected abstract List<String> ddls();

    /**
     * 执行 persistence-generator（MySQL jdbcUrl 模式）。
     *
     * <p>完整流程：
     * ①读取 MySQL 系统属性并验证；
     * ②在 MySQL 中创建测试表；
     * ③拷贝测试资源到临时目录；
     * ④解析 yml 路径并注入 MySQL 连接配置；
     * ⑤调用 Bootstrap 执行 persistence-generator；
     * ⑥清理测试表。
     */
    @Override
    protected void runPersistenceGenerator(String caseName) {
        runPersistenceGeneratorWithMySql(caseName, null);
    }

    @Override
    protected void runPersistenceGenerator(String caseName, String domainName) {
        runPersistenceGeneratorWithMySql(caseName, domainName);
    }

    protected void runPersistenceGeneratorWithMySql(String caseName, String domainName) {
        // 1. 读取 MySQL 系统属性
        String host = prop("mysql.host");
        String port = prop("mysql.port");
        String user = prop("mysql.user");
        String password = prop("mysql.password");
        String database = prop("mysql.database");

        Assumptions.assumeTrue(host != null && port != null && user != null && password != null && database != null,
                "MySQL connection parameters not fully specified via -D flags, skipping test");

        this.jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port,
                database);
        this.schema = database;
        this.userName = user;
        this.password = password;

        // 2. 创建测试表
        createTables(ddls());

        try {
            // 3. 拷贝测试资源到临时工作目录
            basedir = copyResourceToWorkDir(caseName);

            // 4. 解析yml中的相对路径为绝对路径并回写，同时注入MySQL连接配置
            File configFile = resolveAndRewriteConfig(basedir);
            injectMySqlConfig(configFile);

            // 5. 组装CLI参数并调用Bootstrap.main()
            List<String> args = new ArrayList<>();
            args.add("--tool=persistence-generator");
            args.add("--config=" + configFile.getAbsolutePath());
            if (domainName != null && !domainName.isEmpty()) {
                args.add("--domain=" + domainName);
            }

            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Bootstrap.main(args.toArray(new String[0]));
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        } finally {
            // 6. 清理测试表
            dropTables(ddls());
        }
    }

    /**
     * 在已解析路径的 yml 中注入 jdbcUrl、userName、password、schema，同时移除 ddl 字段。
     */
    @SuppressWarnings("unchecked")
    private void injectMySqlConfig(File configFile) {
        Yaml yaml = new Yaml();
        Map<String, Object> configMap;
        try (InputStream fis = new FileInputStream(configFile)) {
            configMap = yaml.load(fis);
        } catch (IOException e) {
            throw new UncheckedIOException("读取配置文件失败: " + configFile.getAbsolutePath(), e);
        }

        // 注入 MySQL 连接配置
        configMap.put("jdbcUrl", jdbcUrl);
        configMap.put("userName", userName);
        configMap.put("password", password);
        configMap.put("schema", schema);

        // 移除 ddl（jdbcUrl 模式下不使用 ddl）
        configMap.remove("ddl");

        // 回写
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);
        Yaml dumpYaml = new Yaml(dumperOptions);
        try (FileWriter writer = new FileWriter(configFile)) {
            dumpYaml.dump(configMap, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("回写配置文件失败: " + configFile.getAbsolutePath(), e);
        }
    }

    /**
     * 在 MySQL 中批量创建测试表
     */
    private void createTables(List<String> ddls) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, userName, password);
                Statement stmt = conn.createStatement()) {
            for (String ddl : ddls) {
                stmt.execute(ddl);
            }
        } catch (Exception e) {
            throw new RuntimeException("创建测试表失败", e);
        }
    }

    /**
     * 在 MySQL 中批量删除测试表
     */
    private void dropTables(List<String> ddls) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, userName, password);
                Statement stmt = conn.createStatement()) {
            for (String ddl : ddls) {
                String tableName = extractTableName(ddl);
                if (tableName != null) {
                    stmt.execute("DROP TABLE IF EXISTS `" + tableName + "`");
                }
            }
        } catch (Exception e) {
            // 清理失败不中断测试
            System.err.println("清理测试表失败: " + e.getMessage());
        }
    }

    /**
     * 从 CREATE TABLE DDL 中提取表名
     */
    private String extractTableName(String ddl) {
        String upper = ddl.toUpperCase();
        int createIdx = upper.indexOf("CREATE TABLE ");
        if (createIdx < 0) {
            return null;
        }
        String after = ddl.substring(createIdx + "CREATE TABLE ".length()).trim();
        // 处理 IF NOT EXISTS
        if (after.toUpperCase().startsWith("IF NOT EXISTS ")) {
            after = after.substring("IF NOT EXISTS ".length()).trim();
        }
        // 去掉反引号
        if (after.startsWith("`")) {
            int end = after.indexOf('`', 1);
            if (end > 0) {
                return after.substring(1, end);
            }
        }
        // 简单按空格截取
        int spaceIdx = after.indexOf(' ');
        if (spaceIdx > 0) {
            return after.substring(0, spaceIdx);
        }
        return after;
    }

    private static String prop(String key) {
        String value = System.getProperty(key);
        return (value != null && !value.isEmpty()) ? value : null;
    }

}
