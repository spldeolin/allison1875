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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.classloader.WarOrFatJarClassLoaderFactory;
import com.spldeolin.allison1875.base.collection.ast.StaticAstContainer;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.da.core.definition.HandlerDefinition;
import com.spldeolin.allison1875.da.core.strategy.ControllerFilter;
import com.spldeolin.allison1875.da.core.strategy.HandlerFilter;
import com.spldeolin.allison1875.da.core.strategy.ResponseBodyTypeParser;
import com.spldeolin.allison1875.da.core.util.MethodQualifiers;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-01-02
 */
@Log4j2
@Accessors(fluent = true)
public class HandlerProcessor {

    @Setter
    private ControllerFilter controllerFilter = controller -> true;

    @Setter
    private HandlerFilter handlerFilter = handler -> true;

    @Setter
    private ResponseBodyTypeParser responseBodyTypeParser = handler -> handler.getType().resolve();

    @Getter
    private Collection<HandlerDefinition> handlerDefinitions = Lists.newLinkedList();

    HandlerProcessor process() {
        StaticAstContainer.getClassOrInterfaceDeclarations().stream().filter(this::isFilteredController)
                .forEach(controller -> {

                    // reflect controller
                    Class<?> reflectController;
                    String name = this.qualifierForClassLoader(controller);
                    try {
                        reflectController = WarOrFatJarClassLoaderFactory.getClassLoader().loadClass(name);
                    } catch (ClassNotFoundException e) {
                        log.warn("class[{}] not found", name);
                        return;
                    }

                    Map<String, Method> declaredMethods = listDeclaredMethodAsMap(reflectController);
                    controller.getMethods().stream().filter(this::isFilteredHandler).forEach(handler -> {
                        HandlerDefinition entry = new HandlerDefinition();

                        // controller
                        entry.controller(controller);

                        // handler
                        String shortestQualifiedSignature = MethodQualifiers.getShortestQualifiedSignature(handler);
                        entry.shortestQualifiedSignature(shortestQualifiedSignature);
                        entry.handler(handler);
                        Method reflectHandler = declaredMethods.get(shortestQualifiedSignature);
                        if (reflectHandler == null) {
                            log.warn("method[{}] not found", shortestQualifiedSignature);
                            return;
                        }

                        // result
                        if (responseBodyTypeParser != null) {
                            entry.responseBodyResolvedType(responseBodyTypeParser.parse(handler));
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
                                    resolvedAnno -> entry.requestBodyResolvedType(parameter.getType().resolve()));
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

                        handlerDefinitions.add(entry);
                    });
                });
        log.info("(Summary) {} Spring MVC handlers has collected.", handlerDefinitions.size());
        return this;
    }

    private Map<String, Method> listDeclaredMethodAsMap(Class<?> reflectController) {
        Map<String, Method> declaredMethods = Maps.newHashMap();
        Arrays.stream(reflectController.getDeclaredMethods())
                .forEach(method -> declaredMethods.put(MethodQualifiers.getShortestQualifiedSignature(method), method));
        return declaredMethods;
    }

    private boolean isFilteredController(ClassOrInterfaceDeclaration coid) {
        // is filtered
        if (!controllerFilter.filter(coid)) {
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

    private boolean isFilteredHandler(MethodDeclaration method) {
        // is filtered
        if (!handlerFilter.filter(method)) {
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
