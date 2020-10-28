package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.ast.AstForestContext;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.LoadClassUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ValidateUtils;
import com.spldeolin.allison1875.base.util.ast.Annotations;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.JavadocDescriptions;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.docanalyzer.DocAnalyzerConfig;
import com.spldeolin.allison1875.docanalyzer.builder.EndpointDtoBuilder;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.strategy.AnalyzeCustomValidationStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.AnalyzeEnumConstantStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.DefaultAnalyzeCustomValidationStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.DefaultAnalyzeEnumConstantStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.DefaultObtainConcernedResponseBodyStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.DefaultSpecificFieldDescriptionsStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.ObtainConcernedResponseBodyStrategy;
import com.spldeolin.allison1875.docanalyzer.strategy.SpecificFieldDescriptionsStrategy;

/**
 * doc-analyzer的主流程
 *
 * @author Deolin 2020-06-10
 */
public class DocAnalyzer implements Allison1875MainProcessor {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(DocAnalyzer.class);

    private final static String docIgnore = "doc-ignore";

    private final static String docCat = "doc-cat";

    private ObtainConcernedResponseBodyStrategy obtainConcernedResponseBodyStrategy = new DefaultObtainConcernedResponseBodyStrategy();

    private AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy =
            new DefaultAnalyzeCustomValidationStrategy();

    private SpecificFieldDescriptionsStrategy specificFieldDescriptionsStrategy =
            new DefaultSpecificFieldDescriptionsStrategy();

    private AnalyzeEnumConstantStrategy analyzeEnumConstantStrategy = new DefaultAnalyzeEnumConstantStrategy();

    @Override
    public void preProcess() {
        Set<ConstraintViolation<DocAnalyzerConfig>> violations = ValidateUtils
                .validate(DocAnalyzerConfig.getInstance());
        if (violations.size() > 0) {
            log.warn("配置项校验未通过，请检查后重新运行");
            for (ConstraintViolation<DocAnalyzerConfig> violation : violations) {
                log.warn(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            System.exit(-9);
        }
    }

    @Override
    public void process(AstForest astForest) {
        astForest = new AstForest(astForest.getAnyClassFromHost(),
                DocAnalyzerConfig.getInstance().getDependencyProjectPaths());
        AstForestContext.setCurrent(astForest);

        // 首次遍历并解析astForest，然后构建jsg对象，jsg对象为后续生成JsonSchema所需
        JsgBuildProc jsgProcessor = new JsgBuildProc(astForest, analyzeCustomValidationStrategy,
                specificFieldDescriptionsStrategy.provideSpecificFieldDescriptions(), analyzeEnumConstantStrategy);
        JsonSchemaGenerator jsg = jsgProcessor.analyzeAstAndBuildJsg();

        // 收集endpoint
        Collection<EndpointDto> endpoints = Lists.newArrayList();

        // 再次遍历astForest，并遍历每个cu下的每个controller（是否是controller由Processor判断）
        ControllerIterateProc controllerIterateProcessor = new ControllerIterateProc(astForest.reset());
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
            Map<String, MethodDeclaration> methodsByShortestQualifier = new MethodCollectProc()
                    .collectMethods(controller);

            // doc-cat标志
            String controllerCat = findControllerCat(controller);

            // 处理@RequestMapping（controller的RequestMapping）
            RequestMappingProc requestMappingProcessor = new RequestMappingProc(controllerClass);

            // 遍历handler
            HandlerIterateProc handlerIterateProcessor = new HandlerIterateProc(controllerClass);
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

                // 收集handler的描述、是否过时、作者、源码位置 等基本信息
                EndpointDtoBuilder builder = new EndpointDtoBuilder();
                builder.cat(handlerCat);
                builder.handlerSimpleName(controller.getName() + "_" + handler.getName());
                builder.descriptionLines(JavadocDescriptions.getAsLines(handler));
                builder.isDeprecated(isDeprecated(controller, handler));
                builder.author(Authors.getAuthor(handler));
                builder.sourceCode(MethodQualifiers.getTypeQualifierWithMethodName(handler));

                // 处理@RequestMapping（handler的RequestMapping）
                requestMappingProcessor.analyze(reflectionMethod);
                builder.combinedUrls(requestMappingProcessor.getCombinedUrls());
                builder.combinedVerbs(requestMappingProcessor.getCombinedVerbs());

                // 分析Request Body
                RequestBodyProc requestBodyAnalyzeProcessor = new RequestBodyProc(jsg);
                builder.requestBodyJsonSchema(requestBodyAnalyzeProcessor.analyze(handler));

                // 分析Response Body
                ResponseBodyProc responseBodyAnalyzeProcessor = new ResponseBodyProc(jsg,
                        obtainConcernedResponseBodyStrategy);
                builder.responseBodyJsonSchema(responseBodyAnalyzeProcessor.analyze(controller, handler));

                // 构建EndpointDto
                endpoints.addAll(builder.build());

            });
        });

        // 同步到YApi
        new YApiSyncProc(endpoints).process();

        log.info(endpoints.size());
    }

    private String findControllerCat(ClassOrInterfaceDeclaration controller) {
        String controllerCat = findCat(controller);
        if (controllerCat == null) {
            controllerCat = JavadocDescriptions.getFirstLine(controller);
        }
        if (StringUtils.isEmpty(controllerCat)) {
            controllerCat = controller.getNameAsString();
        }
        return controllerCat;
    }

    private String findCat(NodeWithJavadoc<?> node) {
        for (String line : JavadocDescriptions.getAsLines(node)) {
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
        for (String line : JavadocDescriptions.getAsLines(node)) {
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

    public DocAnalyzer obtainConcernedResponseBodyStrategy(
            ObtainConcernedResponseBodyStrategy obtainConcernedResponseBodyStrategy) {
        this.obtainConcernedResponseBodyStrategy = obtainConcernedResponseBodyStrategy;
        return this;
    }

    public DocAnalyzer analyzeCustomValidationStrategy(
            AnalyzeCustomValidationStrategy analyzeCustomValidationStrategy) {
        this.analyzeCustomValidationStrategy = analyzeCustomValidationStrategy;
        return this;
    }

    public DocAnalyzer specificFieldDescriptionsStrategy(
            SpecificFieldDescriptionsStrategy specificFieldDescriptionsStrategy) {
        this.specificFieldDescriptionsStrategy = specificFieldDescriptionsStrategy;
        return this;
    }

    public DocAnalyzer analyzeEnumConstantStrategy(AnalyzeEnumConstantStrategy analyzeEnumConstantStrategy) {
        this.analyzeEnumConstantStrategy = analyzeEnumConstantStrategy;
        return this;
    }

}
