package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.JsonUtils;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.javabean.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.javabean.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.javabean.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.service.MarkdownService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;
import com.spldeolin.allison1875.docanalyzer.util.LoadClassUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-12-11
 */
@Singleton
@Slf4j
public class MarkdownServiceImpl implements MarkdownService {

    private static final EasyRandom er = initEasyRandom();

    @Inject
    private DocAnalyzerConfig config;

    @Override
    public void flushToMarkdown(List<EndpointDto> endpoints) {
        Multimap<String/*cat*/, EndpointDto> endpointMap = ArrayListMultimap.create();
        endpoints.forEach(e -> endpointMap.put(e.getCat(), e));

        for (String cat : endpointMap.keySet()) {
            StringBuilder content = new StringBuilder();
            for (EndpointDto endpoint : endpointMap.get(cat)) {
                try {
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

                    // 生成cURL
                    if (config.getEnableCurl() && endpoint.getRequestBodyDescribe() != null) {
                        String fakeReqJson = fakeJsonByDescribe(endpoint.getRequestBodyDescribe());
                        if (fakeReqJson != null) {
                            content.append("### cURL\n");
                            content.append("```shell\n");
                            content.append(String.format("curl --request %s --url 'http://localhost:8080%s' --header "
                                            + "'content-type:application/json' " + "--data '",
                                    endpoint.getHttpMethod().toUpperCase(), endpoint.getUrl()));
                            content.append(fakeReqJson);
                            content.append("'\n```\n");
                        }
                    }

                    // 生成返回值示例
                    if (config.getEnableResponseBodySample() && endpoint.getResponseBodyDescribe() != null) {
                        String fakeRespJson = fakeJsonByDescribe(endpoint.getResponseBodyDescribe());
                        if (fakeRespJson != null) {
                            content.append("### Response Body的示例\n");
                            content.append("```json\n");
                            content.append(fakeRespJson);
                            content.append("\n```\n");
                        }
                    }

                    // markdown语法的分隔线
                    content.append("\n---\n");
                } catch (Exception e) {
                    log.error("fail to output to markdown, endpoint={}", endpoint, e);
                }
            }

            File md = new File(config.getMarkdownDirectoryPath() + File.separator + cat + ".md");
            try {
                FileUtils.writeStringToFile(md, content.toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            log.info("create markdown file. file={}", md);
        }
    }

    private String fakeJsonByDescribe(String describe) throws Exception {
        try {
            Object fakeDto = er.nextObject(LoadClassUtils.loadClass(describe, this.getClass().getClassLoader()));
            return JsonUtils.toJsonPrettily(fakeDto);
        } catch (Exception e) {
            log.error("fail to fake json, describe={}", describe, e);
            throw e;
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
            if (jpdv.getDescriptionLines() != null) { // 目前已知类似“返回值是List<String>”的情况会导致这个属性为null
                content.append(Joiner.on("<br>").join(jpdv.getDescriptionLines())).append("|");
            }
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

    private static EasyRandom initEasyRandom() {
        EasyRandomParameters erp = new EasyRandomParameters();
        erp.collectionSizeRange(2, 2);
        erp.stringLengthRange(1, 8);
        erp.randomizationDepth(5);
        erp.ignoreRandomizationErrors(true);
        erp.randomize(String.class, () -> RandomStringUtils.randomAlphanumeric(8));
        erp.randomize(Integer.class, () -> RandomUtils.nextInt(0, 99999));
        erp.randomize(Long.class, () -> RandomUtils.nextLong(1000000000L, 9999999999L));
        erp.randomize(BigDecimal.class,
                () -> new BigDecimal(RandomStringUtils.randomNumeric(5) + "." + RandomStringUtils.randomNumeric(2)));
        erp.randomize(Object.class, () -> "x");
        return new EasyRandom(erp);
    }

}
