package com.spldeolin.allison1875.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.enums.ToolEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yanshaowei01 2026-05-10
 */
@Slf4j
public class Bootstrap {

    public static void main(String[] args) {
        // 打印banner
        Allison1875.hello();

        // 解析CLI参数
        CliArgs cliArgs = parseArgs(args);
        log.info("toolName={} domainName={} configFile={}", cliArgs.toolName, cliArgs.domainName, cliArgs.configFile);

        // 解析工具名为ToolEnum
        ToolEnum tool = ToolEnum.fromToolName(cliArgs.toolName);

        // 读取.allison1875配置文件并反序列化
        Config config = loadConfig(cliArgs.configFile);
        log.info("config={}", config);

        // 执行allison1875
        Allison1875.letsGo(tool, config, cliArgs.domainName);
    }

    /**
     * 解析CLI参数，提取 --tool、--domain、--config
     */
    private static CliArgs parseArgs(String[] args) {
        String toolName = null;
        String domainName = null;
        String configFilePath = null;
        for (String arg : args) {
            if (arg.startsWith("--tool=")) {
                toolName = arg.substring("--tool=".length());
            } else if (arg.startsWith("--domain=")) {
                domainName = arg.substring("--domain=".length());
            } else if (arg.startsWith("--config=")) {
                configFilePath = arg.substring("--config=".length());
            }
        }
        if (toolName == null || toolName.isEmpty()) {
            throw new Allison1875Exception("必须通过 --tool=<toolName> 指定工具名（如 doc-analyzer）");
        }
        if (configFilePath == null || configFilePath.isEmpty()) {
            throw new Allison1875Exception("必须通过 --config=<path> 指定 .allison1875.yml 配置文件路径");
        }
        CliArgs cliArgs = new CliArgs();
        cliArgs.toolName = toolName;
        cliArgs.domainName = domainName;
        cliArgs.configFile = configFilePath;
        return cliArgs;
    }

    /**
     * 读取.allison1875配置文件并反序列化为Config
     */
    private static Config loadConfig(String configFilePath) {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            throw new Allison1875Exception("配置文件不存在: " + configFile.getAbsolutePath());
        }
        Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        try (FileInputStream fis = new FileInputStream(configFile)) {
            return yaml.load(fis);
        } catch (IOException e) {
            throw new Allison1875Exception("读取配置文件失败: " + configFile.getAbsolutePath(), e);
        }
    }

    /**
     * CLI参数
     */
    private static class CliArgs {

        String toolName;

        String domainName;

        String configFile;

    }

}
