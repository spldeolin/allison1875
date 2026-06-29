package com.spldeolin.allison1875.cli;

import java.io.File;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.enums.ToolEnum;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * CLI 主入口。
 *
 * @author Deolin 2026-05-10
 */
@Slf4j
public class Entrypoint {

    public static void main(String[] args) {
        // 打印banner
        Allison1875.hello();

        // 解析CLI参数
        CliArgs cliArgs = parseArgs(args);
        log.info("toolName={} domainName={} configFile={}", cliArgs.tool, cliArgs.domainName, cliArgs.configFile);

        // 读取.allison1875配置文件并反序列化
        Config config = Config.fromYaml(new File(cliArgs.configFile));
        log.info("config={}", JsonUtils.toJson(config));

        // 执行allison1875
        Allison1875.letsGo(cliArgs.tool, config, cliArgs.domainName);
    }

    /**
     * 解析CLI参数，提取 --tool、--domain、--config
     */
    private static CliArgs parseArgs(String[] args) {
        String tool = null;
        String domainName = null;
        String configFilePath = null;
        for (String arg : args) {
            if (arg.startsWith("--tool=")) {
                tool = arg.substring("--tool=".length());
            } else if (arg.startsWith("--domain=")) {
                domainName = arg.substring("--domain=".length());
            } else if (arg.startsWith("--config=")) {
                configFilePath = arg.substring("--config=".length());
            }
        }
        if (tool == null || tool.isEmpty()) {
            throw new Allison1875Exception("必须通过 --tool=<toolName> 指定工具名（如 doc-analyzer）");
        }
        if (configFilePath == null || configFilePath.isEmpty()) {
            throw new Allison1875Exception("必须通过 --config=<path> 指定 .allison1875.yml 配置文件路径");
        }
        CliArgs cliArgs = new CliArgs();
        cliArgs.tool = ToolEnum.of(tool);
        cliArgs.domainName = domainName;
        cliArgs.configFile = configFilePath;
        return cliArgs;
    }

    /**
     * CLI参数
     */
    private static class CliArgs {

        ToolEnum tool;

        String domainName;

        String configFile;

    }

}
