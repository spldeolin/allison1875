package com.spldeolin.allison1875.da.deprecated.core.processor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.da.deprecated.core.enums.MethodTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * URI和请求方法等路由信息
 *
 * @author Deolin 2019-12-23
 */
@Log4j2
@Accessors(fluent = true)
class RouteProcessor {

    @Setter
    private ClassOrInterfaceDeclaration controller;

    @Setter
    private MethodDeclaration handler;

    @Getter
    private Set<MethodTypeEnum> methodTypes = Sets.newHashSet();

    @Getter
    private List<String> uris = Lists.newLinkedList();

    RouteProcessor process() {
        checkStatus();

        AntPathMatcher antPathMatcher = new AntPathMatcher();
        Collection<RequestMappingDto> fromController = parseRequestMappings(controller.getAnnotations());
        Collection<RequestMappingDto> fromHandler = parseRequestMappings(handler.getAnnotations());

        for (RequestMappingDto dto1 : fromController) {
            for (RequestMappingDto dto2 : fromHandler) {
                methodTypes.addAll(dto1.methods);
                methodTypes.addAll(dto2.methods);
                for (String path1 : emptyToOne(dto1.paths)) {
                    for (String path2 : emptyToOne(dto2.paths)) {
                        uris.add(antPathMatcher.combine(path1, path2));
                    }
                }
            }
        }
        if (fromController.size() == 0) {
            for (RequestMappingDto dto : fromHandler) {
                methodTypes.addAll(dto.methods);
                for (String path : dto.paths) {
                    uris.add(antPathMatcher.combine("", path));
                }
            }
        }
        if (fromHandler.size() == 0) {
            for (RequestMappingDto dto : fromController) {
                methodTypes.addAll(dto.methods);
                for (String path : dto.paths) {
                    uris.add(antPathMatcher.combine("", path));
                }
            }
        }

        for (int i = 0; i < uris.size(); i++) {
            String uri = uris.get(i);
            if (!uri.startsWith("/")) {
                uris.set(i, "/" + uri);
            }
        }

        return this;
    }

    private void checkStatus() {
        if (controller == null) {
            throw new IllegalStateException("controller cannot be absent.");
        }
        if (handler == null) {
            throw new IllegalStateException("handler cannot be absent.");
        }
    }

    private Collection<String> emptyToOne(Collection<String> paths) {
        if (paths.size() == 0) {
            paths.add("");
        }
        return paths;
    }

    private Collection<RequestMappingDto> parseRequestMappings(NodeList<AnnotationExpr> annotations) {
        Collection<RequestMappingDto> result = Lists.newArrayList();
        for (AnnotationExpr annotation : annotations) {
            ResolvedAnnotationDeclaration resolve = annotation.resolve();

            String annoQualifier = resolve.getId();
            RequestMappingDto dto;
            if (QualifierConstants.REQUEST_MAPPING.equals(annoQualifier)) {
                dto = this.parseRequestMapping(annotation, false);
            } else {
                Optional<MethodTypeEnum> methodType = MethodTypeEnum.ofAnnotationQualifier(annoQualifier);
                if (methodType.isPresent()) {
                    dto = this.parseRequestMapping(annotation, true);
                    dto.methods = methodType.get().inCollection();
                } else {
                    continue;
                }
            }
            result.add(dto);
        }
        return result;
    }

    private RequestMappingDto parseRequestMapping(AnnotationExpr annotation, boolean isSpecificMethod) {
        RequestMappingDto dto = new RequestMappingDto();
        annotation.ifSingleMemberAnnotationExpr(single -> {
            Expression memberValue = single.getMemberValue();
            dto.paths = this.collectPaths(memberValue);
        });

        annotation.ifNormalAnnotationExpr(normal -> normal.getPairs().forEach(pair -> {
            if (pair.getNameAsString().equals("value")) {
                dto.paths = this.collectPaths(pair.getValue());
            }
            if (pair.getNameAsString().equals("path")) {
                dto.paths.addAll(this.collectPaths(pair.getValue()));
            }
            if (!isSpecificMethod && pair.getNameAsString().equals("method")) {
                dto.methods.addAll(this.collectionMethods(pair.getValue()));
            }
        }));
        return dto;
    }

    private Collection<String> collectPaths(Expression memberValue) {
        Collection<String> result = Lists.newArrayList();
        memberValue.ifArrayInitializerExpr(arrayInit -> result
                .addAll(arrayInit.getValues().stream().map(arrayEle -> arrayEle.asStringLiteralExpr().asString())
                        .collect(Collectors.toList())));
        memberValue.ifStringLiteralExpr(stringLite -> result.add(stringLite.asString()));
        return result;
    }

    @RequestMapping(method = RequestMethod.GET)
    private Collection<MethodTypeEnum> collectionMethods(Expression memberValue) {
        Collection<MethodTypeEnum> result = Lists.newArrayList();
        memberValue.ifArrayInitializerExpr(arrayInit -> arrayInit.getValues().forEach(arrayEle -> arrayEle
                .ifFieldAccessExpr(
                        fieldAccess -> MethodTypeEnum.ofValue(fieldAccess.getNameAsString()).ifPresent(result::add))));
        memberValue.ifFieldAccessExpr(
                fieldAccess -> MethodTypeEnum.ofValue(fieldAccess.getNameAsString()).ifPresent(result::add));
        return result;
    }

    private static class RequestMappingDto {

        private Collection<MethodTypeEnum> methods = Lists.newArrayList();

        private Collection<String> paths = Lists.newArrayList();

    }

}
