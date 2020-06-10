package com.spldeolin.allison1875.da.processor;

import java.util.Map;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.LoadClassUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Annotations;
import com.spldeolin.allison1875.base.util.ast.Authors;
import com.spldeolin.allison1875.base.util.ast.Javadocs;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import com.spldeolin.allison1875.da.builder.EndpointDtoBuilder;
import com.spldeolin.allison1875.da.dto.EndpointDto;
import com.spldeolin.allison1875.da.markdown.MarkdownConverter;
import lombok.extern.log4j.Log4j2;

/**
 * doc-analyzer的主流程
 *
 * @author Deolin 2020-06-10
 */
@Log4j2
public class MainProcessor {

    public void process() {
        AstForest astForest = AstForest.getInstance();

        // 首次遍历并解析astForest，然后构建jsg对象，jsg对象为后续生成JsonSchema所需
        JsgBuildProcessor jsgProcessor = new JsgBuildProcessor(astForest);
        JsonSchemaGenerator jsg = jsgProcessor.analyzeAstAndBuildJsg();

        // 再次重头遍历astForest，并遍历每个cu下的每个controller（是否是controller由Processor判断）
        ControllerIterateProcessor controllerIterateProcessor = new ControllerIterateProcessor(astForest.reset());
        controllerIterateProcessor.iterate(controller -> {

            // 反射controller，如果失败那么这个controller就没有处理该controller的必要了
            Class<?> controllerClass;
            try {
                controllerClass = tryReflectController(controller, astForest);
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

                // 收集handler的描述、版本号、是否过去、作者、源码位置 等基本信息
                builder.description(StringUtils.limitLength(Javadocs.extractEveryLine(handler, "\n"), 4096));
                builder.version("");
                builder.isDeprecated(isDeprecated(controller, handler));
                builder.author(Authors.getAuthorOrElseEmpty(handler));
                builder.sourceCode(Locations.getRelativePathWithLineNo(handler));

                // 处理@RequestMapping（handler的RequestMapping）
                requestMappingProcessor.analyze(reflectionMethod);
                builder.combinedUrls(requestMappingProcessor.getCombinedUrls());
                builder.combinedVerbs(requestMappingProcessor.getCombinedVerbs());

                // 分析Request Body
                RequestBodyProcessor requestBodyAnalyzeProcessor = new RequestBodyProcessor(astForest, jsg);
                builder.requestBodyInfo(requestBodyAnalyzeProcessor.analyze(handler));

                // 分析Response Body
                ResponseBodyProcessor responseBodyAnalyzeProcessor = new ResponseBodyProcessor(astForest, jsg);
                builder.responseBodyInfo(responseBodyAnalyzeProcessor.analyze(controller, handler));

                // 构建EndpointDto
                EndpointDto endpoint = builder.build();

                // 转化为视图层
                new MarkdownConverter().convert(Lists.newArrayList(endpoint), false);
            });

        });

    }

    private String findGroupNames(ClassOrInterfaceDeclaration controller) {
        CompilationUnit cu = controller.findCompilationUnit().orElseThrow(CuAbsentException::new);
        String result = null;
        for (Comment oc : cu.getOrphanComments()) {
            if (oc.isLineComment() && oc.getContent().trim().startsWith("DOC-GROUP")) {
                result = oc.getContent().replaceFirst("DOC-GROUP", "").trim();
                break;
            }
        }
        if (StringUtils.isBlank(result)) {
            result = "未分类";
        }
        return result;
    }

    private boolean isDeprecated(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        return Annotations.isAnnoPresent(handler, Deprecated.class) || Annotations
                .isAnnoPresent(controller, Deprecated.class);
    }

    private Class<?> tryReflectController(ClassOrInterfaceDeclaration controller, AstForest astForest)
            throws ClassNotFoundException {
        String qualifier = controller.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new);
        try {
            return LoadClassUtils.loadClass(qualifier, astForest.getCurrentClassLoader());
        } catch (ClassNotFoundException e) {
            log.error("类[{}]无法被加载", qualifier);
            throw e;
        }
    }

}
