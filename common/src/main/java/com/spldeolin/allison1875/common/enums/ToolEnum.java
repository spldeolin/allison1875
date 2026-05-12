package com.spldeolin.allison1875.common.enums;

import java.util.function.Function;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Allison 1875 工具枚举，穷举所有可用的工具及其 Guice Module 配置映射。
 *
 * @author Deolin 2026-05-12
 */
@Getter
@AllArgsConstructor
public enum ToolEnum {

    DOC_ANALYZER("doc-analyzer", Config::getDocAnalyzerModule, false),

    HANDLER_TRANSFORMER("handler-transformer", Config::getHandlerTransformerModule, false),

    PERSISTENCE_GENERATOR("persistence-generator", Config::getPersistenceGeneratorModule, false),

    QUERY_TRANSFORMER("query-transformer", Config::getQueryTransformerModule, false),

    STAR_TRANSFORMER("star-transformer", Config::getStarTransformerModule, false),

    FORM_GENERATOR("form-generator", Config::getFormGeneratorModule, true),

    ;

    /**
     * 工具名称，对应 CLI --tool=xxx 和 Maven goal 名称
     */
    private final String toolName;

    /**
     * 从Config获取该工具对应的Guice Module实现类全限定名
     */
    private final Function<Config, String> moduleClassNameGetter;

    /**
     * 是否为组合工具（需要合并多个子工具Module），目前仅 form-generator 为 true
     */
    private final boolean composite;

    /**
     * 根据工具名解析为枚举值
     *
     * @param toolName 工具名称（如 "doc-analyzer"）
     * @return 对应的 ToolEnum
     * @throws Allison1875Exception 如果工具名不合法
     */
    public static ToolEnum fromToolName(String toolName) {
        for (ToolEnum tool : values()) {
            if (tool.toolName.equals(toolName)) {
                return tool;
            }
        }
        StringBuilder sb = new StringBuilder("不支持的工具名: ").append(toolName).append("，可选值: ");
        for (int i = 0; i < values().length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(values()[i].toolName);
        }
        throw new Allison1875Exception(sb.toString());
    }

}
