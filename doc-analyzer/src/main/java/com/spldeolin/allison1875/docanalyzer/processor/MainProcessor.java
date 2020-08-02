package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.mutable.MutableInt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.LoadClassUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Annotations;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.builder.EndpointDtoBuilder;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.dto.JsonPropertyDescriptionValueDto;
import com.spldeolin.allison1875.docanalyzer.dto.ValidatorDto;
import com.spldeolin.allison1875.docanalyzer.strategy.AnalyzeCustomValidationStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.DefaultAnalyzeCustomValidationStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.DefaultObtainConcernedResponseBodyStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.DefaultSpecificFieldDescriptionsStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.ObtainConcernedResponseBodyStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.SpecificFieldDescriptionsStrategy;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils;
import com.spldeolin.allison1875.docanalyzer.util.JsonSchemaTraverseUtils.EveryJsonSchemaHandler;
import com.spldeolin.allison1875.docanalyzer.yapi.YApiProcessor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * doc-analyzer的主流程
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
@Accessors(fluent = true)
public class MainProcessor {

    @Setter
    private ObtainConcernedResponseBodyStrategy obtainConcernedResponseBodyStrategy =
            new DefaultObtainConcernedResponseBodyStrategy();

    @Setter
    private AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy =
            new DefaultAnalyzeCustomValidationStrategy();

    @Setter
    private SpecificFieldDescriptionsStrategy specificFieldDescriptionsStrategy =
            new DefaultSpecificFieldDescriptionsStrategy();

    public void process() {
        AstForest astForest = AstForest.getInstance();

        // 首次遍历并解析astForest，然后构建jsg对象，jsg对象为后续生成JsonSchema所需
        JsgBuildProcessor jsgProcessor = new JsgBuildProcessor(astForest, analyzeCustomValidationStrategy,
                specificFieldDescriptionsStrategy.provideSpecificFieldDescriptions());
        JsonSchemaGenerator jsg = jsgProcessor.analyzeAstAndBuildJsg();

        // 收集endpoint
        Collection<EndpointDto> endpoints = Lists.newArrayList();
        MutableInt handlerCount = new MutableInt(0);

        // 再次重头遍历astForest，并遍历每个cu下的每个controller（是否是controller由Processor判断）
        ControllerIterateProcessor controllerIterateProcessor = new ControllerIterateProcessor(astForest.reset());
        controllerIterateProcessor.iterate(controller -> {

            // DOC-IGNORE标志
            if (findIgnoreFlag(controller)) {
                return;
            }

            // 反射controller，如果失败那么这个controller就没有处理该controller的必要了
            Class<?> controllerClass;
            try {
                controllerClass = tryReflectController(controller);
            } catch (ClassNotFoundException e) {
                return;
            }

            // 收集controller内的所有方法
            Map<String, MethodDeclaration> methodsByShortestQualifier = new MethodCollectProcessor()
                    .collectMethods(controller);

            // 收集分组信息
            EndpointDtoBuilder builder = new EndpointDtoBuilder();
            builder.groupNames(findGroupNames(controller));

            // 处理@RequestMapping（controller的RequestMapping）
            RequestMappingProcessor requestMappingProcessor = new RequestMappingProcessor(controllerClass);

            // 遍历handler
            HandlerIterateProcessor handlerIterateProcessor = new HandlerIterateProcessor(controllerClass);
            handlerIterateProcessor.iterate(reflectionMethod -> {

                MethodDeclaration handler = methodsByShortestQualifier
                        .get(MethodQualifiers.getShortestQualifiedSignature(reflectionMethod));
                if (handler == null) {
                    // 可能是源码删除了某个handler但未编译，所以reflectionMethod存在，但MethodDeclaration已经不存在了
                    // 这种情况没有继续处理该handler的必要了
                    return;
                }

                // DOC-IGNORE标志
                if (findIgnoreFlag(handler)) {
                    return;
                }

                // 收集handler的描述、版本号、是否过去、作者、源码位置 等基本信息
                builder.description(getDescriptionOrElseName(controller, handler));
                builder.version("");
                builder.isDeprecated(isDeprecated(controller, handler));
                builder.author(Authors.getAuthor(handler));
                builder.sourceCode(MethodQualifiers.getTypeQualifierWithMethodName(handler));

                // 根据作者名过滤
                if (notContainAuthorNameFromConfig(builder.author())) {
                    return;
                }

                // 处理@RequestMapping（handler的RequestMapping）
                requestMappingProcessor.analyze(reflectionMethod);
                builder.combinedUrls(requestMappingProcessor.getCombinedUrls());
                builder.combinedVerbs(requestMappingProcessor.getCombinedVerbs());

                // 分析Request Body
                RequestBodyProcessor requestBodyAnalyzeProcessor = new RequestBodyProcessor(jsg);
                builder.requestBodyInfo(requestBodyAnalyzeProcessor.analyze(handler));

                // 分析Response Body
                ResponseBodyProcessor responseBodyAnalyzeProcessor = new ResponseBodyProcessor(jsg,
                        obtainConcernedResponseBodyStrategy);
                builder.responseBodyInfo(responseBodyAnalyzeProcessor.analyze(controller, handler));

                // 构建EndpointDto
                endpoints.add(builder.build());

                // handler个数
                handlerCount.increment();
            });
        });

        Set<String> catNames = endpoints.stream().map(EndpointDto::getGroupNames).collect(Collectors.toSet());
        catNames.add("回收站");
        YApiProcessor yApiProcessor = new YApiProcessor();
        Set<String> yapiCatNames = yApiProcessor.getYapiCatIdsEachName().keySet();
        yApiProcessor.addCat(Sets.difference(catNames, yapiCatNames));
        Map<String, Long> catIdsEachName = yApiProcessor.getYapiCatIdsEachName();

        Map<String, JsonNode> yapiUrls = yApiProcessor.listInterfaces();
        Set<String> urls = endpoints.stream().map(one -> Iterables.getFirst(one.getUrls(), ""))
                .collect(Collectors.toSet());

        // yapi中，在解析出endpoint中找不到url的接口，移动到回收站
        for (String url : yapiUrls.keySet()) {
            if (!urls.contains(url)) {
                yApiProcessor.deleteInterface(yapiUrls.get(url), catIdsEachName.get("回收站"));
            }
        }

        // 新增接口
        for (EndpointDto endpoint : endpoints) {
            String description = endpoint.getDescription();
            String title = StringUtils.splitLineByLine(description).get(0);
            String yapiDesc = "";
            if (endpoint.getIsDeprecated()) {
                yapiDesc = "> 该接口已被开发者标记为**已废弃**，不建议调用\n";
            }
            yapiDesc += "---\n";
            yapiDesc += "##### 注释\n";
            yapiDesc += "```\n";
            yapiDesc += description + "\n";
            yapiDesc += "```\n";
            yapiDesc += "##### 开发者\n";
            yapiDesc += endpoint.getAuthor() + "\n";
            yapiDesc += "##### 源码\n";
            yapiDesc += endpoint.getSourceCode() + "\n";

            EveryJsonSchemaHandler everyJsonSchemaHandler = (propertyName, jsonSchema, parentJsonSchema) -> {
                String raw = jsonSchema.getDescription();
                if (raw == null) {
                    return;
                }
                JsonPropertyDescriptionValueDto jpdv = JsonUtils.toObject(raw, JsonPropertyDescriptionValueDto.class);
                String comment = null;
                if (jpdv.getDescriptionLines().size() > 0) {
                    StringBuilder sb = new StringBuilder("注释\n");
                    for (String line : jpdv.getDescriptionLines()) {
                        if (StringUtils.isNotBlank(line)) {
                            sb.append("\t").append(line).append("\n");
                        } else {
                            sb.append("\n");
                        }
                    }
                    comment = sb.deleteCharAt(sb.length() - 1).toString();
                }
                String validatorInfo = null;
                if (jpdv.getValidators().size() > 0) {
                    StringBuilder sb = new StringBuilder("校验项\n");
                    for (ValidatorDto validator : jpdv.getValidators()) {
                        sb.append("\t").append(validator.getValidatorType()).append(validator.getNote()).append("\n");
                    }
                    validatorInfo = sb.deleteCharAt(sb.length() - 1).toString();
                }
                String format = null;
                if (jpdv.getJsonFormatPattern() != null) {
                    format = "格式\n";
                    format += "\t" + jpdv.getJsonFormatPattern();
                }
                jsonSchema.setDescription(Joiner.on("\n\n").skipNulls().join(comment, validatorInfo, format));
            };

            String reqJs = "";
            JsonSchema requestJsonSchema = endpoint.getRequestBodyJsonSchema();
            if (requestJsonSchema != null) {
                JsonSchemaTraverseUtils.traverse(requestJsonSchema, everyJsonSchemaHandler);
                reqJs = JsonUtils.toJson(requestJsonSchema);
            }
            String respJs = "";
            JsonSchema responseJsonSchema = endpoint.getResponseBodyJsonSchema();
            if (responseJsonSchema != null) {
                JsonSchemaTraverseUtils.traverse(responseJsonSchema, everyJsonSchemaHandler);
                respJs = JsonUtils.toJson(responseJsonSchema);
            }

            yApiProcessor.addInterface(title, Iterables.getFirst(endpoint.getUrls(), ""), reqJs, respJs, yapiDesc,
                    Iterables.getFirst(endpoint.getHttpMethods(), ""), catIdsEachName.get(endpoint.getGroupNames()));
        }

        log.info(handlerCount);
    }

    private String getDescriptionOrElseName(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        String result = StringUtils.limitLength(JavadocDescriptions.getEveryLineInOne(handler, "\n"), 4096);
        if (StringUtils.isBlank(result)) {
            result = controller.getNameAsString() + "_" + handler.getNameAsString();
        }
        return result;
    }

    private boolean notContainAuthorNameFromConfig(String author) {
        String filterByAuthorName = DocAnalyzerConfig.getInstance().getFilterByAuthorName();
        if (StringUtils.isEmpty(filterByAuthorName)) {
            return false;
        }
        return !author.contains(filterByAuthorName);
    }

    private String findGroupNames(ClassOrInterfaceDeclaration controller) {
        CompilationUnit cu = controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
        String result = null;
        for (Comment oc : cu.getOrphanComments()) {
            if (oc.isLineComment() && StringUtils.lowerCase(oc.getContent().trim()).startsWith("doc-group")) {
                result = oc.getContent().substring("doc-group".length() + 1).trim();
                break;
            }
        }
        if (StringUtils.isBlank(result)) {
            result = "未分类";
        }
        return result;
    }

    private boolean findIgnoreFlag(ClassOrInterfaceDeclaration controller) {
        CompilationUnit cu = controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
        for (Comment oc : cu.getOrphanComments()) {
            if (oc.isLineComment() && StringUtils.lowerCase(oc.getContent().trim()).startsWith("doc-ignore")) {
                return true;
            }
        }
        return false;
    }

    private boolean findIgnoreFlag(MethodDeclaration handler) {
        for (String line : JavadocDescriptions.getEveryLine(handler)) {
            if (line.equalsIgnoreCase("doc-ignore")) {
                return true;
            }
        }
        return false;
    }

    private boolean isDeprecated(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        return Annotations.isAnnotationPresent(handler, Deprecated.class) || Annotations
                .isAnnotationPresent(controller, Deprecated.class);
    }

    private Class<?> tryReflectController(ClassOrInterfaceDeclaration controller) throws ClassNotFoundException {
        String qualifier = controller.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        try {
            return LoadClassUtils.loadClass(qualifier, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            log.error("类[{}]无法被加载", qualifier);
            throw e;
        }
    }

}
