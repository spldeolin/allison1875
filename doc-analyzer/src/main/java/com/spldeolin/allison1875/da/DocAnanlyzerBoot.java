package com.spldeolin.allison1875.da;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
import com.spldeolin.allison1875.da.processor.ControllerIterateProcessor;
import com.spldeolin.allison1875.da.processor.HandlerIterateProcessor;
import com.spldeolin.allison1875.da.processor.JsonSchemaGeneratorProcessor;
import com.spldeolin.allison1875.da.processor.MethodCollectProcessor;
import com.spldeolin.allison1875.da.processor.RequestBodyAnalyzeProcessor;
import com.spldeolin.allison1875.da.processor.ResponseBodyAnalyzeProcessor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-06-01
 */
@Log4j2
public class DocAnanlyzerBoot {

    public static void main(String[] args) {
        new DocAnanlyzerBoot().process();
    }

    private static final PathMatcher pathMatcher = new AntPathMatcher();

    public void process() {
        AstForest astForest = AstForest.getInstance();

        // 首次遍历并解析astForest，然后构建jsg对象，jsg对象为后续生成JsonSchema所需
        JsonSchemaGeneratorProcessor jsgProcessor = new JsonSchemaGeneratorProcessor(astForest);
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

            EndpointDtoBuilder builder = new EndpointDtoBuilder();
            builder.groupNames(findGroupNames(controller));

            RequestMapping controllerRequestMapping = findRequestMappingAnnoOrElseNull(controllerClass);
            String[] cPaths = findValueFromAnno(controllerRequestMapping);
            RequestMethod[] cVerbs = findVerbFromAnno(controllerRequestMapping);

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

                RequestMapping methodRequestMapping = findRequestMappingAnnoOrElseNull(reflectionMethod);
                String[] mPaths = methodRequestMapping.value();
                RequestMethod[] mVerbs = methodRequestMapping.method();
                builder.combinedUrls(combineUrl(cPaths, mPaths));
                builder.combinedVerbs(combineVerb(cVerbs, mVerbs));

                builder.description(StringUtils.limitLength(Javadocs.extractEveryLine(handler, "\n"), 4096));
                builder.version("");
                builder.isDeprecated(isDeprecated(controller, handler));
                builder.author(Authors.getAuthorOrElseEmpty(handler));
                builder.sourceCode(Locations.getRelativePathWithLineNo(handler));

                // 分析Request Body
                RequestBodyAnalyzeProcessor requestBodyAnalyzeProcessor = new RequestBodyAnalyzeProcessor(astForest,
                        jsg);
                builder.requestBodyInfo(requestBodyAnalyzeProcessor.analyze(handler));

                // 分析Response Body
                ResponseBodyAnalyzeProcessor responseBodyAnalyzeProcessor = new ResponseBodyAnalyzeProcessor(astForest,
                        jsg);
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


    private Collection<RequestMethod> combineVerb(RequestMethod[] cVerbs, RequestMethod[] mVerbs) {
        Collection<RequestMethod> combinedVerbs = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cVerbs)) {
            combinedVerbs.addAll(Arrays.asList(cVerbs));
        }
        if (ArrayUtils.isNotEmpty(mVerbs)) {
            combinedVerbs.addAll(Arrays.asList(mVerbs));
        }
        if (combinedVerbs.size() == 0) {
            combinedVerbs.addAll(Arrays.asList(RequestMethod.values()));
        }
        return combinedVerbs;
    }

    private Collection<String> combineUrl(String[] cPaths, String[] mPaths) {
        Collection<String> combinedUrls = Lists.newArrayList();
        if (ArrayUtils.isNotEmpty(cPaths) && ArrayUtils.isNotEmpty(mPaths)) {
            for (String cPath : cPaths) {
                for (String mPath : mPaths) {
                    combinedUrls.add(pathMatcher.combine(cPath, mPath));
                }
            }
        } else if (ArrayUtils.isEmpty(cPaths)) {
            combinedUrls.addAll(Arrays.asList(mPaths));
        } else if (ArrayUtils.isEmpty(mPaths)) {
            combinedUrls.addAll(Arrays.asList(cPaths));
        } else {
            combinedUrls.add("/");
        }
        combinedUrls = ensureAllStartWithSlash(combinedUrls);
        return combinedUrls;
    }

    private Collection<String> ensureAllStartWithSlash(Collection<String> urls) {
        Collection<String> result = Lists.newArrayList();
        for (String url : urls) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            result.add(url);
        }
        return result;
    }


    private RequestMethod[] findVerbFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new RequestMethod[0] : controllerRequestMapping.method();
    }

    private String[] findValueFromAnno(RequestMapping controllerRequestMapping) {
        return controllerRequestMapping == null ? new String[0] : controllerRequestMapping.value();
    }

    private RequestMapping findRequestMappingAnnoOrElseNull(AnnotatedElement annotated) {
        return AnnotatedElementUtils.findMergedAnnotation(annotated, RequestMapping.class);
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
