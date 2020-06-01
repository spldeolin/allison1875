package com.spldeolin.allison1875.da.approved.demo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.collection.ast.AstForest;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.util.LoadClassUtils;
import com.spldeolin.allison1875.base.util.ast.MethodQualifiers;
import lombok.extern.log4j.Log4j2;

/**
 * 示例代码：获取每个Spring MVC handler的所有请求URL和请求方法
 *
 * （基于反射，借助spring-core的AnnotatedElementUtils和AntPathMatcher）
 *
 * @author Deolin 2020-05-31
 */
@Log4j2
public class MappingDemo {

    public static void main(String[] args) {
        AstForest astForest = AstForest.getInstance();
        Map<String, Pair<Collection<String>, Collection<RequestMethod>>> handlerInfos = Maps.newHashMap();
        Map<String, MethodDeclaration> methods = Maps.newHashMap();
        for (CompilationUnit cu : astForest) {
            for (ClassOrInterfaceDeclaration controller : cu
                    .findAll(ClassOrInterfaceDeclaration.class, MappingDemo::isController)) {

                Class<?> controllerClass;
                try {
                    controllerClass = LoadClassUtils
                            .loadClass(controller.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new),
                                    astForest.getCurrentClassLoader());
                } catch (ClassNotFoundException e) {
                    continue;
                }

                for (MethodDeclaration method : controller.findAll(MethodDeclaration.class)) {
                    methods.put(MethodQualifiers.getShortestQualifiedSignature(method), method);
                }

                RequestMapping controllerRequestMapping = AnnotatedElementUtils
                        .findMergedAnnotation(controllerClass, RequestMapping.class);
                String[] controllerPaths =
                        controllerRequestMapping == null ? new String[0] : controllerRequestMapping.value();
                RequestMethod[] controllerVerbs =
                        controllerRequestMapping == null ? new RequestMethod[0] : controllerRequestMapping.method();
                for (Method method : controllerClass.getDeclaredMethods()) {
                    RequestMapping methodRequestMapping = AnnotatedElementUtils
                            .findMergedAnnotation(method, RequestMapping.class);
                    if (methodRequestMapping != null) {
                        AntPathMatcher apm = new AntPathMatcher();
                        Collection<String> urls = Lists.newArrayList();
                        String[] methodPaths = methodRequestMapping.value();
                        if (ArrayUtils.isNotEmpty(controllerPaths) && ArrayUtils.isNotEmpty(methodPaths)) {
                            for (String controllerPath : controllerPaths) {
                                for (String methodPath : methodPaths) {
                                    urls.add(apm.combine(controllerPath, methodPath));
                                }
                            }
                        } else if (ArrayUtils.isEmpty(controllerPaths)) {
                            urls.addAll(Arrays.asList(methodPaths));
                        } else if (ArrayUtils.isEmpty(methodPaths)) {
                            urls.addAll(Arrays.asList(controllerPaths));
                        } else {
                            urls.add("/");
                        }
                        urls = ensureAllStartWithSlash(urls);

                        Collection<RequestMethod> verbs = Lists.newArrayList();
                        if (ArrayUtils.isNotEmpty(controllerVerbs)) {
                            verbs.addAll(Arrays.asList(controllerVerbs));
                        }
                        if (ArrayUtils.isNotEmpty(methodRequestMapping.method())) {
                            verbs.addAll(Arrays.asList(methodRequestMapping.method()));
                        }
                        if (verbs.size() == 0) {
                            verbs.addAll(Arrays.asList(RequestMethod.values()));
                        }

                        handlerInfos.put(MethodQualifiers.getShortestQualifiedSignature(method), Pair.of(urls, verbs));
                    }
                }
            }

        }

        for (String fromReflect : handlerInfos.keySet()) {
            if (!methods.containsKey(fromReflect)) {
                log.error(fromReflect);
            }
        }

        log.info(Joiner.on("\n").withKeyValueSeparator("♨♨♨♨♨♨♨♨").join(handlerInfos));
    }

    private static boolean isController(ClassOrInterfaceDeclaration coid) {
        for (AnnotationExpr annotation : coid.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolve = annotation.resolve();
                if (resolve.hasAnnotation(QualifierConstants.CONTROLLER) || QualifierConstants.CONTROLLER
                        .equals(resolve.getName())) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("annotation [{}] of class [{}] cannot resolve", annotation.getNameAsString(),
                        coid.getNameAsString(), e);
            }
        }
        return false;
    }

    private static Collection<String> ensureAllStartWithSlash(Collection<String> urls) {
        Collection<String> result = Lists.newArrayList();
        for (String url : urls) {
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            result.add(url);
        }
        return result;
    }

}
