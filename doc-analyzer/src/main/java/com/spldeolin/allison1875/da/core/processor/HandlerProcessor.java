package com.spldeolin.allison1875.da.core.processor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.ast.classloader.WarOrFatJarClassLoader;
import com.spldeolin.allison1875.base.ast.collection.StaticAstContainer;
import com.spldeolin.allison1875.da.core.constant.QualifierConstants;
import com.spldeolin.allison1875.da.core.processor.result.HandlerProcessResult;
import com.spldeolin.allison1875.da.core.strategy.DefaultHandlerFilter;
import com.spldeolin.allison1875.da.core.strategy.HandlerFilter;
import com.spldeolin.allison1875.da.core.strategy.ResponseBodyTypeParser;
import com.spldeolin.allison1875.da.core.util.MethodQualifier;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-02
 */
@Log4j2
public class HandlerProcessor {

    public Collection<HandlerProcessResult> process(HandlerFilter handlerFilter,
            ResponseBodyTypeParser hanlderResultTypeParser) {
        Collection<HandlerProcessResult> result = Lists.newLinkedList();

        StaticAstContainer.getClassOrInterfaceDeclarations().stream()
                .filter(coid -> isFilteredController(coid, handlerFilter)).forEach(controller -> {

            // reflect controller
            Class<?> reflectController;
            String name = this.qualifierForClassLoader(controller);
            try {
                reflectController = WarOrFatJarClassLoader.classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                log.warn("class[{}] not found", name);
                return;
            }

            Map<String, Method> declaredMethods = listDeclaredMethodAsMap(reflectController);
            controller.getMethods().stream().filter(method -> this.isFilteredHandler(method, handlerFilter))
                    .forEach(handler -> {
                        HandlerProcessResult entry = new HandlerProcessResult();

                        // controller
                        entry.controller(controller);

                        // handler
                        String shortestQualifiedSignature = MethodQualifier.getShortestQualifiedSignature(handler);
                        entry.shortestQualifiedSignature(shortestQualifiedSignature);
                        entry.handler(handler);
                        Method reflectHandler = declaredMethods.get(shortestQualifiedSignature);
                        if (reflectHandler == null) {
                            log.warn("method[{}] not found", shortestQualifiedSignature);
                            return;
                        }

                        // result
                        if (hanlderResultTypeParser != null) {
                            entry.responseBodyResolvedType(hanlderResultTypeParser.parse(handler));
                        } else {
                            entry.responseBodyResolvedType(handler.getType().resolve());
                        }

                        // requestBody requestParams pathVariables
                        Collection<Parameter> requestParams = Lists.newLinkedList();
                        Collection<Parameter> pathVariables = Lists.newLinkedList();
                        for (Parameter parameter : handler.getParameters()) {
                            parameter.getAnnotationByName("RequestBody").map(AnnotationExpr::resolve)
                                    .filter(resolvedAnno -> QualifierConstants.REQUEST_BODY
                                            .equals(resolvedAnno.getId())).ifPresent(
                                    resolvedAnno -> entry.requestBodyResolveType(parameter.getType().resolve()));
                            parameter.getAnnotationByName("RequestParam").map(AnnotationExpr::resolve)
                                    .filter(resolvedAnno -> QualifierConstants.REQUEST_PARAM
                                            .equals(resolvedAnno.getId()))
                                    .ifPresent(resolvedAnno -> requestParams.add(parameter));
                            parameter.getAnnotationByName("PathVariable").map(AnnotationExpr::resolve)
                                    .filter(resolvedAnno -> QualifierConstants.PATH_VARIABLE
                                            .equals(resolvedAnno.getId()))
                                    .ifPresent(resolvedAnno -> pathVariables.add(parameter));
                        }
                        entry.requestParams(requestParams);
                        entry.pathVariables(pathVariables);

                        result.add(entry);
                    });
        });
        log.info("(Summary) {} Spring MVC handlers has collected.", result.size());
        return result;
    }

    private Map<String, Method> listDeclaredMethodAsMap(Class<?> reflectController) {
        Map<String, Method> declaredMethods = Maps.newHashMap();
        Arrays.stream(reflectController.getDeclaredMethods())
                .forEach(method -> declaredMethods.put(MethodQualifier.getShortestQualifiedSignature(method), method));
        return declaredMethods;
    }

    private boolean isFilteredController(ClassOrInterfaceDeclaration coid, HandlerFilter handlerFilter) {
        // is filtered
        if (!MoreObjects.firstNonNull(handlerFilter, new DefaultHandlerFilter()).filter(coid)) {
            return false;
        }

        // is controller
        if (hasNoneKindOfController(coid) && hasNoneKindOfRequestMapping(coid)) {
            return false;
        }
        for (AnnotationExpr anno : coid.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolvedAnno = anno.resolve();
                if (isKindOfController(resolvedAnno) || isKindOfRequestMapping(resolvedAnno)) {
                    return true;
                }
            } catch (UnsolvedSymbolException e) {
                log.warn(e);
            }
        }
        return false;
    }

    private boolean isFilteredHandler(MethodDeclaration method, HandlerFilter handlerFilter) {
        // is filtered
        if (!MoreObjects.firstNonNull(handlerFilter, new DefaultHandlerFilter()).filter(method)) {
            return false;
        }

        // is handler
        if (hasNoneKindOfRequestMapping(method)) {
            return false;
        }
        for (AnnotationExpr anno : method.getAnnotations()) {
            try {
                ResolvedAnnotationDeclaration resolveAnno = anno.resolve();
                if (isKindOfRequestMapping(resolveAnno)) {
                    return true;
                }
            } catch (UnsolvedSymbolException e) {
                log.warn(e);
            }
        }
        return false;
    }

    private boolean hasNoneKindOfController(ClassOrInterfaceDeclaration clazz) {
        return !clazz.getAnnotationByName("Controller").isPresent() && !clazz.getAnnotationByName("RestController")
                .isPresent();
    }

    private boolean isKindOfController(ResolvedAnnotationDeclaration resolvedAnno) {
        return QualifierConstants.CONTROLLER.equals(resolvedAnno.getId()) || resolvedAnno
                .hasDirectlyAnnotation(QualifierConstants.CONTROLLER);
    }

    private boolean hasNoneKindOfRequestMapping(NodeWithAnnotations<?> node) {
        return !node.getAnnotationByName("RequestMapping").isPresent() && !node.getAnnotationByName("DeleteMapping")
                .isPresent() && !node.getAnnotationByName("GetMapping").isPresent() && !node
                .getAnnotationByName("PatchMapping").isPresent() && !node.getAnnotationByName("PostMapping").isPresent()
                && !node.getAnnotationByName("PutMapping").isPresent();
    }

    private boolean isKindOfRequestMapping(ResolvedAnnotationDeclaration resolvedAnno) {
        return QualifierConstants.REQUEST_MAPPING.equals(resolvedAnno.getId()) || resolvedAnno
                .hasDirectlyAnnotation(QualifierConstants.REQUEST_MAPPING);
    }

    private String qualifierForClassLoader(ClassOrInterfaceDeclaration controller) {
        StringBuilder qualifierForClassLoader = new StringBuilder(64);
        this.qualifierForClassLoader(qualifierForClassLoader, controller);
        return qualifierForClassLoader.toString();
    }

    private void qualifierForClassLoader(StringBuilder qualifier, TypeDeclaration<?> node) {
        node.getParentNode().ifPresent(parent -> {
            if (parent instanceof TypeDeclaration) {
                this.qualifierForClassLoader(qualifier, (TypeDeclaration<?>) parent);
                qualifier.append("$");
                qualifier.append(node.getNameAsString());
            } else {
                node.getFullyQualifiedName().ifPresent(qualifier::append);
            }
        });
    }

}
