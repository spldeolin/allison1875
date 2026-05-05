package com.spldeolin.allison1875.docanalyzer.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeEnumConstantsRetval;
import com.spldeolin.allison1875.docanalyzer.dto.AnalyzeValidRetval;
import com.spldeolin.allison1875.docanalyzer.dto.CategorizedMarkdownDTO;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDTO;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDTO;
import com.spldeolin.allison1875.docanalyzer.dto.PathParamDTO;
import com.spldeolin.allison1875.docanalyzer.dto.QueryParamDTO;
import com.spldeolin.allison1875.docanalyzer.service.MarkdownService;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2023-12-11
 */
@Singleton
@Slf4j
public class MarkdownServiceImpl implements MarkdownService {


    private static final String illegalChars = "\\/:*?\"<>|";

    @Inject
    private Config config;

    @Override
    public void flushToMarkdown(List<EndpointDTO> endpoints) {
        List<CategorizedMarkdownDTO> categorizedMds = categorizeMarkdowns(endpoints);

        for (CategorizedMarkdownDTO categorizedMd : categorizedMds) {
            StringBuilder dirPath = new StringBuilder(config.getMarkdownDir().getPath());
            if (CollectionUtils.isNotEmpty(categorizedMd.getHierarchicalCategories())) {
                for (String hierarchicalCategory : categorizedMd.getHierarchicalCategories()) {
                    dirPath.append(File.separator).append(sanitizeFileName(hierarchicalCategory));
                }
            }
            if (!new File(dirPath.toString()).exists()) {
                new File(dirPath.toString()).mkdirs();
            }
            File md = new File(dirPath + File.separator + sanitizeFileName(categorizedMd.getDirectCategory()) + ".md");
            try {
                FileUtils.writeStringToFile(md, categorizedMd.getContent(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            log.info("create markdown file. file={}", md);
        }
    }

    @Override
    public List<CategorizedMarkdownDTO> categorizeMarkdowns(List<EndpointDTO> endpoints) {
        if (config.getSingleEndpointPerMarkdown()) {
            // description的第一行（即API的标题）作为cat，以复用this.flushToMarkdown方法来实现singleMarkdown
            endpoints = endpoints.stream().map(e -> e.copy().setDirectCategory(e.getDescriptionLines().get(0)))
                    .collect(Collectors.toList());
        }

        Multimap<String/*hierarchicalcat + cat*/, EndpointDTO> endpointMap = LinkedListMultimap.create();
        endpoints.forEach(e -> endpointMap.put(e.getHierarchicalCategories() + e.getDirectCategory(), e));

        List<CategorizedMarkdownDTO> retval = Lists.newArrayList();
        for (String key : endpointMap.keySet()) {
            StringBuilder content = new StringBuilder();
            List<EndpointDTO> endpointGroup = Lists.newArrayList(endpointMap.get(key));
            for (EndpointDTO endpoint : endpointGroup) {
                content.append(this.generateEndpointDoc(endpoint));
            }

            CategorizedMarkdownDTO categorizedMd = new CategorizedMarkdownDTO();
            categorizedMd.setHierarchicalCategories(endpointGroup.get(0).getHierarchicalCategories());
            categorizedMd.setDirectCategory(endpointGroup.get(0).getDirectCategory());
            categorizedMd.setContent(content.toString());
            categorizedMd.setEndpointGroup(endpointGroup);
            retval.add(categorizedMd);
        }
        return retval;
    }

    private String sanitizeFileName(String fileName) {
        StringBuilder sanitized = new StringBuilder();
        for (char c : fileName.toCharArray()) {
            if (illegalChars.indexOf(c) != -1) { // 直接检查非法字符
                log.warn("replaced illegal character: '{}'", c);
                sanitized.append('_');
            } else {
                sanitized.append(c);
            }
        }
        // 清理首尾空格
        String processed = sanitized.toString().trim();
        if (StringUtils.isEmpty(processed)) {
            return "_";
        }
        return processed;
    }

    protected String generateEndpointDoc(EndpointDTO endpoint) {
        StringBuilder result = new StringBuilder(64);
        String title = Iterables.getFirst(endpoint.getDescriptionLines(), "");
        result.append("## ").append(title).append("\n");

        if (endpoint.getDescriptionLines().size() > 1) {
            endpoint.getDescriptionLines().stream().skip(1)
                    .forEach(line -> result.append(line).append("\n\n"));
        }

        if (endpoint.getSinceVersion() != null || endpoint.getDeprecatedDescription() != null) {
            result.append("### 兼容性说明\n");
            if (endpoint.getSinceVersion() != null) {
                result.append("- 本接口加入版本：").append(endpoint.getSinceVersion()).append("\n\n");
            }
            if (endpoint.getDeprecatedDescription() != null) {
                result.append("- 本接口已过时，不建议调用，过时原因：").append(endpoint.getDeprecatedDescription())
                        .append("\n\n");
            }
        }

        result.append("### URL\n");
        String urlsText = Joiner.on(" 或 ").join(endpoint.getUrls());
        result.append(endpoint.getHttpMethod().toUpperCase()).append(" ").append(urlsText).append("\n");

        if (CollectionUtils.isNotEmpty(endpoint.getPathParams())) {
            result.append("### Path Param\n");
            result.append(this.generatePathParams(endpoint.getPathParams()));
        }

        if (CollectionUtils.isNotEmpty(endpoint.getQueryParams())) {
            result.append("### Query Param\n");
            result.append(this.generateQueryParams(endpoint.getQueryParams()));
        }

        if (endpoint.getRequestBodyJsonSchema() != null) {
            result.append("### Request Body (application/json)\n");
            result.append(this.generateReqOrRespDoc(endpoint, true));
        }

        if (endpoint.getResponseBodyJsonSchema() != null && !endpoint.getResponseBodyJsonSchema().isNullSchema()) {
            result.append(getResponseBodyTableTitle()).append("\n");
            result.append(this.generateReqOrRespDoc(endpoint, false));
        }

        // 可拓展的更多文档内容
        result.append(this.generateMoreDoc(endpoint));

        // markdown语法的分隔线
        result.append("\n---\n");
        return result.toString();
    }

    private StringBuilder generatePathParams(List<PathParamDTO> pathParams) {
        StringBuilder result = new StringBuilder("|字段名|类型|注释|\n");
        result.append("|---|---|---|\n");
        for (PathParamDTO pathParam : pathParams) {
            result.append("|");
            result.append(pathParam.getName());
            result.append("|");
            result.append(pathParam.getType().getTitle());
            result.append("|");
            result.append(Joiner.on("<br>").join(pathParam.getDescriptionLines()));
            result.append("|\n");
        }
        return result;
    }

    private StringBuilder generateQueryParams(List<QueryParamDTO> queryParams) {
        StringBuilder result = new StringBuilder("|字段名|类型|注释|是否必填|默认值|\n");
        result.append("|---|---|---|---|---|\n");
        for (QueryParamDTO queryParam : queryParams) {
            result.append("|");
            result.append(queryParam.getName());
            result.append("|");
            result.append(queryParam.getType().getTitle());
            result.append("|");
            result.append(Joiner.on("<br>").join(queryParam.getDescriptionLines()));
            result.append("|");
            result.append(queryParam.isRequired() ? "是" : "否");
            result.append("|");
            result.append(MoreObjects.firstNonNull(queryParam.getDefaultValue(), ""));
            result.append("|\n");
        }
        return result;
    }


    protected StringBuilder generateReqOrRespDoc(EndpointDTO endpoint, boolean isReqBody) {
        JsonSchema rootJsonSchema;
        if (isReqBody) {
            rootJsonSchema = endpoint.getRequestBodyJsonSchema();
        } else {
            rootJsonSchema = endpoint.getResponseBodyJsonSchema();
        }

        StringBuilder content = new StringBuilder(1024);
        content.append("| 字段名 | JSON类型 | 注释 | 其他 |\n");
        content.append("| --- | --- | --- | --- |\n");

        // 根节点就是valueType，意味着下面的JsonSchemaTraverseUtils.traverse不会进行回调
        if (rootJsonSchema.isValueTypeSchema()) {
            String desc = Joiner.on("<br>").join(isReqBody ? endpoint.getReqBodyParamDescriptionLines()
                    : endpoint.getReturnDescriptionLines());
            content.append("| | ").append(StringUtils.capitalize(rootJsonSchema.getType().value())).append(" |")
                    .append(desc).append(" | |\n");
            return content;
        }

        JsonSchemaTraverseUtils.traverse(rootJsonSchema, (propertyName, jsonSchema, parentJsonSchema, depth) -> {
            if (jsonSchema.isArraySchema()) {
                return;
            }
            // 如果parent是arrayNode，从parrent获取jpdv，否则从自身node获取jpdv
            JsonPropertyDescriptionValueDTO jpdv;
            if (parentJsonSchema.isArraySchema()) {
                jpdv = JsonPropertyDescriptionValueDTO.deserialize(parentJsonSchema.getDescription());
            } else {
                jpdv = JsonPropertyDescriptionValueDTO.deserialize(jsonSchema.getDescription());
            }

            if (jpdv == null) {
                // root schema 为 object arrary
                jpdv = new JsonPropertyDescriptionValueDTO();
            }

            String row = "";
            row += "|";
            // 字段名
            row += StringUtils.repeat("- ", depth) + propertyName;
            row += "|";
            // JSON类型
            if (jsonSchema instanceof ReferenceSchema) {
                row += "Object";
            } else if (jsonSchema instanceof ObjectSchema) {
                row += "Object";
            } else if (jsonSchema instanceof IntegerSchema) {
                row += jpdv.getJsonIntegerTypeEnum().getTitle();
            } else {
                row += StringUtils.capitalize(jsonSchema.getType().value());
            }
            if (parentJsonSchema.isArraySchema()) {
                row += " Array";
            }
            if (jpdv.getReferencePath() != null) {
                row += "<br>数据结构同：" + jpdv.getReferencePath();
            }
            row += "|";
            // 注释
            if (StringUtils.isEmpty(propertyName)) { // 顶层为array或者simple value
                row += Joiner.on("<br>").join(isReqBody ? endpoint.getReqBodyParamDescriptionLines()
                        : endpoint.getReturnDescriptionLines());
            } else {
                row += Joiner.on("<br>").join(jpdv.getCommentLines());
            }
            row += "|";
            // 其他 - 校验项
            StringBuilder validDoc = null;
            if (isReqBody) {
                if (CollectionUtils.isNotEmpty(jpdv.getValids())) {
                    validDoc = new StringBuilder();
                    if (jpdv.getValids().size() == 1) {
                        validDoc.append(jpdv.getValids().get(0).getValidatorType())
                                .append(jpdv.getValids().get(0).getNote());
                    } else {
                        for (int i = 0; i < jpdv.getValids().size(); i++) {
                            AnalyzeValidRetval validator = jpdv.getValids().get(i);
                            validDoc.append(i + 1).append(". ").append(validator.getValidatorType())
                                    .append(validator.getNote()).append("<br>");
                        }
                        validDoc.delete(validDoc.length() - 4, validDoc.length());
                    }
                }
            }

            // 其他 - 枚举项
            StringBuilder enumDoc = null;
            if (CollectionUtils.isNotEmpty(jpdv.getAnalyzeEnumConstantsRetvals())) {
                enumDoc = new StringBuilder();
                for (AnalyzeEnumConstantsRetval enumConstant : jpdv.getAnalyzeEnumConstantsRetvals()) {
                    enumDoc.append(enumConstant.getCode()).append(" : ").append(enumConstant.getTitle()).append("<br>");
                }
                enumDoc.delete(enumDoc.length() - 4, enumDoc.length());
            }
            // 其他 - 格式
            String formatDoc = null;
            if (StringUtils.isNotEmpty(jpdv.getFormatPattern())) {
                formatDoc = "格式：" + jpdv.getFormatPattern();
            }
            // 其他 - 兼容性
            String compatibilityDoc = null;
            if (StringUtils.isNotEmpty(jpdv.getSinceVersion())) {
                compatibilityDoc = "本字段加入版本：" + jpdv.getSinceVersion();
            }
            if (StringUtils.isNotEmpty(jpdv.getDeprecatedDescription())) {
                if (compatibilityDoc == null) {
                    compatibilityDoc =
                            "本字段已过时，原因：" + jpdv.getDeprecatedDescription().replaceAll("\\r?\\n", " ");
                } else {
                    compatibilityDoc +=
                            "<br>本字段已过时，原因：" + jpdv.getDeprecatedDescription().replaceAll("\\r?\\n", " ");
                }
            }
            // 其他 - 更多
            String moreDoc = null;
            if (CollectionUtils.isNotEmpty(jpdv.getMoreDocLines())) {
                moreDoc = Joiner.on("<br>").join(jpdv.getMoreDocLines());
            }
            row += Joiner.on("<br><br>").skipNulls().join(validDoc, enumDoc, formatDoc, compatibilityDoc, moreDoc);
            row += "|\n";
            content.append(row);
        });

        return content;
    }


    protected String generateMoreDoc(EndpointDTO endpoint) {
        return "";
    }

    protected String getResponseBodyTableTitle() {
        return "### Response Body (application/json)";
    }

}
