package com.spldeolin.allison1875.docanalyzer.processor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
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
import com.spldeolin.allison1875.docanalyzer.constant.ControllerMarkerConstant;
import com.spldeolin.allison1875.docanalyzer.dto.EndpointDto;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeCustomValidationHandle;
import com.spldeolin.allison1875.docanalyzer.handle.AnalyzeEnumConstantHandle;
import com.spldeolin.allison1875.docanalyzer.handle.MoreJpdvAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.ObtainConcernedResponseBodyHandle;
import com.spldeolin.allison1875.docanalyzer.handle.SpecificFieldDescriptionsHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultAnalyzeCustomValidationHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultAnalyzeEnumConstantHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultMoreJpdvAnalysisHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultObtainConcernedResponseBodyHandle;
import com.spldeolin.allison1875.docanalyzer.handle.impl.DefaultSpecificFieldDescriptionsHandle;
import lombok.extern.log4j.Log4j2;

/**
 * doc-analyzer的主流程
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
public class DocAnalyzer implements Allison1875MainProcessor<DocAnalyzerConfig, DocAnalyzer> {

    protected ObtainConcernedResponseBodyHandle obtainConcernedResponseBodyHandle =
            new DefaultObtainConcernedResponseBodyHandle();

    protected AnalyzeCustomValidationHandle analyzeCustomValidationHandle = new DefaultAnalyzeCustomValidationHandle();

    protected SpecificFieldDescriptionsHandle specificFieldDescriptionsHandle =
            new DefaultSpecificFieldDescriptionsHandle();

    protected AnalyzeEnumConstantHandle analyzeEnumConstantHandle = new DefaultAnalyzeEnumConstantHandle();

    protected MoreJpdvAnalysisHandle moreJpdvAnalysisHandle = new DefaultMoreJpdvAnalysisHandle();

    public static final ThreadLocal<DocAnalyzerConfig> CONFIG = ThreadLocal.withInitial(DocAnalyzerConfig::new);

    @Override
    public DocAnalyzer config(DocAnalyzerConfig config) {
        Set<ConstraintViolation<DocAnalyzerConfig>> violations = ValidateUtils.validate(config);
        if (violations.size() > 0) {
            log.warn("配置项校验未通过，请检查后重新运行");
            for (ConstraintViolation<DocAnalyzerConfig> violation : violations) {
                log.warn(violation.getRootBeanClass().getSimpleName() + "." + violation.getPropertyPath() + " "
                        + violation.getMessage());
            }
            System.exit(-9);
        }
        CONFIG.set(config);
        return this;
    }

    @Override
    public void process(AstForest astForest) {
        // re parser
        astForest = new AstForest(astForest.getAnyClassFromHost(), CONFIG.get().getDependencyProjectPaths());
        AstForestContext.setCurrent(astForest);

        // 首次遍历并解析astForest，然后构建jsg对象，jsg对象为后续生成JsonSchema所需
        JsgBuildProc jsgProcessor = new JsgBuildProc(astForest, analyzeCustomValidationHandle,
                specificFieldDescriptionsHandle.provideSpecificFieldDescriptions(), analyzeEnumConstantHandle,
                moreJpdvAnalysisHandle);
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
                        obtainConcernedResponseBodyHandle);
                builder.responseBodyJsonSchema(responseBodyAnalyzeProcessor.analyze(controller, handler));

                // 构建EndpointDto
                endpoints.addAll(builder.build());

            });
        });

        // 同步到YApi
        new YApiSyncProc(moreJpdvAnalysisHandle, endpoints).process();

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
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_CAT)) {
                String catContent = org.apache.commons.lang3.StringUtils
                        .removeStartIgnoreCase(line, ControllerMarkerConstant.DOC_CAT).trim();
                if (catContent.length() > 0) {
                    return catContent;
                }
            }
        }
        return null;
    }

    private boolean findIgnoreFlag(NodeWithJavadoc<?> node) {
        for (String line : JavadocDescriptions.getAsLines(node)) {
            if (org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(line, ControllerMarkerConstant.DOC_IGNORE)) {
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

    public DocAnalyzer obtainConcernedResponseBodyHandle(
            ObtainConcernedResponseBodyHandle obtainConcernedResponseBodyHandle) {
        this.obtainConcernedResponseBodyHandle = obtainConcernedResponseBodyHandle;
        return this;
    }

    public DocAnalyzer analyzeCustomValidationHandle(AnalyzeCustomValidationHandle analyzeCustomValidationHandle) {
        this.analyzeCustomValidationHandle = analyzeCustomValidationHandle;
        return this;
    }

    public DocAnalyzer specificFieldDescriptionsHandle(
            SpecificFieldDescriptionsHandle specificFieldDescriptionsHandle) {
        this.specificFieldDescriptionsHandle = specificFieldDescriptionsHandle;
        return this;
    }

    public DocAnalyzer analyzeEnumConstantHandle(AnalyzeEnumConstantHandle analyzeEnumConstantHandle) {
        this.analyzeEnumConstantHandle = analyzeEnumConstantHandle;
        return this;
    }

}
