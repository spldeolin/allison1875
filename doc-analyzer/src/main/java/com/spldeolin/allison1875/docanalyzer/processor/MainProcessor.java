package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.mutable.MutableInt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
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

    private final static String docIgnore = "doc-ignore";

    private final static String docCat = "doc-cat";

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

            // doc-ignore标志
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

            // doc-cat标志
            String controllerCat = findControllerCat(controller);

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

                // doc-ignore标志
                if (findIgnoreFlag(handler)) {
                    return;
                }

                // doc-cat标志
                String handlerCat = findCat(handler);
                if (handlerCat == null) {
                    handlerCat = controllerCat;
                }

                // 收集handler的描述、版本号、是否过去、作者、源码位置 等基本信息
                EndpointDtoBuilder builder = new EndpointDtoBuilder();
                builder.cat(handlerCat);
                builder.handlerSimpleName(controller.getName() + "_" + handler.getName());
                builder.descriptionLines(JavadocDescriptions.getEveryLine(handler));
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

        Set<String> catNames = endpoints.stream().map(EndpointDto::getCat).collect(Collectors.toSet());
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
            Collection<String> descriptionLines = endpoint.getDescriptionLines();
            String title = Iterables.getFirst(descriptionLines, null);
            if (title == null || title.length() == 0) {
                title = endpoint.getHandlerSimpleName();
            }
            String yapiDesc = endpoint.toStringPrettily();

            EveryJsonSchemaHandler everyJsonSchemaHandler = (propertyName, jsonSchema, parentJsonSchema) -> {
                JsonPropertyDescriptionValueDto jpdv = JsonUtils
                        .toObjectSkipNull(jsonSchema.getDescription(), JsonPropertyDescriptionValueDto.class);
                if (jpdv == null) {
                    return;
                }
                jsonSchema.setDescription(jpdv.toStringPrettily());
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
                    Iterables.getFirst(endpoint.getHttpMethods(), ""), catIdsEachName.get(endpoint.getCat()));
        }

        log.info(handlerCount);
    }

    private String findControllerCat(ClassOrInterfaceDeclaration controller) {
        String controllerCat = findCat(controller);
        if (controllerCat == null) {
            controllerCat = JavadocDescriptions.getTrimmedFirstLine(controller, true);
        }
        if (controllerCat == null) {
            controllerCat = controller.getNameAsString();
        }
        return controllerCat;
    }

    private boolean notContainAuthorNameFromConfig(String author) {
        String filterByAuthorName = DocAnalyzerConfig.getInstance().getFilterByAuthorName();
        if (StringUtils.isEmpty(filterByAuthorName)) {
            return false;
        }
        return !author.contains(filterByAuthorName);
    }

    private String findCat(NodeWithJavadoc<?> node) {
        Collection<String> lines = JavadocDescriptions.getEveryLine(node);
        for (String line : lines) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, docCat)) {
                String catContent = org.apache.commons.lang3.StringUtils.removeStartIgnoreCase(line, docCat).trim();
                if (catContent.length() > 0) {
                    return catContent;
                }
            }
        }
        return null;
    }

    private boolean findIgnoreFlag(NodeWithJavadoc<?> node) {
        for (String line : JavadocDescriptions.getEveryLine(node)) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, docIgnore)) {
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
