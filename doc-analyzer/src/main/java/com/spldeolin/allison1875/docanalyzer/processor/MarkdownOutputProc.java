package com.spldeolin.allison1875.docanalyzer.processor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2023-12-11
 */
@Singleton
@Log4j2
public class MarkdownOutputProc {

    @Inject
    private DocAnalyzerConfig config;

    public void process(Collection<EndpointDto> endpoints) throws Exception {
        Preconditions.checkNotNull(config.getMarkdownDirectoryPath(),
                "requried 'DocAnalyzerConfig#markdownDirectoryPath' Property cannot be null");

        Multimap<String/*cat*/, EndpointDto> endpointMap = ArrayListMultimap.create();
        endpoints.forEach(e -> endpointMap.put(e.getCat(), e));

        for (String cat : endpointMap.keySet()) {
            StringBuilder content = new StringBuilder();
            for (EndpointDto endpoint : endpointMap.get(cat)) {
                String title = Iterables.getFirst(endpoint.getDescriptionLines(), null);
                if (StringUtils.isEmpty(title)) {
                    title = endpoint.getHandlerSimpleName();
                }
                content.append("## ").append(title).append("\n");

                if (endpoint.getDescriptionLines().size() > 1) {
                    endpoint.getDescriptionLines().stream().skip(1)
                            .forEach(line -> content.append(line).append("\n\n"));
                }

                content.append("### 请求方法与URL" + "\n");
                content.append(endpoint.getHttpMethod().toUpperCase()).append(" `").append(endpoint.getUrl())
                        .append("`\n");

                content.append("### Request Body的数据结构（application/json）\n");
                if (endpoint.getRequestBodyJsonSchema() != null) {
                    content.append(this.buildReqOrRespBodyPart(endpoint, true));
                } else {
                    content.append("无需Request Body\n");
                }

                content.append("### Response Body的数据结构（application/json）\n");
                if (endpoint.getResponseBodyJsonSchema() != null) {
                    content.append(this.buildReqOrRespBodyPart(endpoint, false));
                } else {
                    content.append("没有Response Body\n");
                }

                // TODO MarkdownOutProc中需要生成cURL
//                content.append("### cURL\n");
//                content.append("```java\n");
//                content.append(String.format(
//                        "curl --request %s --url 'http://localhost:8080%s' --header 'content-type:application/json' "
//                                + "--data '", endpoint.getHttpMethod().toUpperCase(), endpoint.getUrl()));
//                StringBuilder sb = new StringBuilder(64);
//                StringBuilder closeMarks = new StringBuilder(64);
//                JsonSchemaTraverseUtils.traverse(endpoint.getRequestBodyJsonSchema(),
//                        (propertyName, jsonSchema, parentJsonSchema, depth) -> {
//                            if (jsonSchema.isArraySchema()) {
//
//                            } else if (jsonSchema.isObjectSchema()) {
//
//                            } else {
//                                sb.append("\"").append(propertyName).append("\" : xxx");
//                                if (parentJsonSchema.isObjectSchema()) {
//                                    Lists.newArrayList(parentJsonSchema.asObjectSchema().getProperties().keySet())
//                                    .indexOf()
//                                }
//                            }
//                        });
//                content.append(sb).append(closeMarks.reverse());
//                content.append("'\n```\n");
                content.append("\n---\n");
            }

            File md = new File(config.getMarkdownDirectoryPath() + File.separator + cat + ".md");
            FileUtils.writeStringToFile(md, content.toString(), StandardCharsets.UTF_8);
            log.info("create markdown file. file={}", md);
        }
    }

    private String buildReqOrRespBodyPart(EndpointDto endpoint, boolean isReqBody) {
        StringBuilder content = new StringBuilder(64);
        content.append("| 字段名 | JSON类型 | 注释 |");
        if (isReqBody) {
            content.append(" 校验项 |");
        }
        content.append(" 格式 |\n");
        content.append("| --- | --- | --- | --- |");
        if (isReqBody) {
            content.append(" --- |");
        }
        content.append("\n");

        JsonSchema rootJsonSchema;
        if (isReqBody) {
            rootJsonSchema = endpoint.getRequestBodyJsonSchema();
        } else {
            rootJsonSchema = endpoint.getResponseBodyJsonSchema();
        }

        if (!rootJsonSchema.isObjectSchema() && !rootJsonSchema.isArraySchema()) {
            // TODO 解析javadoc中的@return，作为它的注释。优先级低，这样的写法非常少
            content.append("| | ").append(rootJsonSchema.getType().value()).append(" | | |\n");
        }

        JsonSchemaTraverseUtils.traverse(rootJsonSchema, (propertyName, jsonSchema, parentJsonSchema, depth) -> {
            if (jsonSchema.isArraySchema()) {
                return;
            }
            // 如果parent是arrayNode，从parrent获取jpdv，否则从自身node获取jpdv
            JsonPropertyDescriptionValueDto jpdv;
            if (parentJsonSchema.isArraySchema()) {
                jpdv = toJpdv(parentJsonSchema.getDescription());
            } else {
                jpdv = toJpdv(jsonSchema.getDescription());
            }
            // 字段名
            content.append("|").append(StringUtils.repeat("- ", depth)).append("`").append(propertyName).append("`|");
            // JSON类型
            if (jpdv.getReferencePath() != null) {
                content.append("object");
            } else {
                content.append(jsonSchema.getType().value());
            }
            if (parentJsonSchema.isArraySchema()) {
                content.append(" array");
            }
            if (jpdv.getReferencePath() != null) {
                content.append("<br>数据结构同：").append(jpdv.getReferencePath());
            }
            content.append("|");
            // 注释
            content.append(Joiner.on("<br>").join(jpdv.getDescriptionLines())).append("|");
            if (isReqBody) {
                // 校验项
                if (jpdv.getValids().size() == 1) {
                    content.append(jpdv.getValids().get(0).getValidatorType())
                            .append(jpdv.getValids().get(0).getNote());
                }
                if (jpdv.getValids().size() > 1) {
                    for (int i = 0; i < jpdv.getValids().size(); i++) {
                        ValidatorDto validator = jpdv.getValids().get(i);
                        content.append(i + 1).append(". ").append(validator.getValidatorType())
                                .append(validator.getNote()).append("<br>");
                    }
                }
            }
            content.append("|");
            content.append(jpdv.getJsonFormatPattern()).append("|\n");
        });
        return content.toString();
    }

    private JsonPropertyDescriptionValueDto toJpdv(String nullableJson) {
        if (nullableJson == null) {
            return JsonPropertyDescriptionValueDto.EMPTY;
        }
        return JsonUtils.toObject(nullableJson, JsonPropertyDescriptionValueDto.class);
    }

}
