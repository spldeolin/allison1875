package com.spldeolin.allison1875.da.core.processor;

import java.util.Collection;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.spldeolin.allison1875.base.constant.QualifierConstants;
import com.spldeolin.allison1875.da.core.enums.MethodType;
import com.spldeolin.allison1875.da.core.processor.result.RequestMappingProcessResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2019-12-23
 */
@Log4j2
public class RequestMappingProcessor {

    public RequestMappingProcessResult process(ClassOrInterfaceDeclaration controller, MethodDeclaration handler) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        Collection<RequestMappingDto> fromController = parseRequestMappings(controller.getAnnotations());
        Collection<RequestMappingDto> fromHandler = parseRequestMappings(handler.getAnnotations());

        Set<String> combinePaths = Sets.newHashSet();
        Set<MethodType> combineMethods = Sets.newHashSet();
        for (RequestMappingDto dto1 : fromController) {
            for (RequestMappingDto dto2 : fromHandler) {
                combineMethods.addAll(dto1.getMethods());
                combineMethods.addAll(dto2.getMethods());
                for (String path1 : emptyToOne(dto1.getPaths())) {
                    for (String path2 : emptyToOne(dto2.getPaths())) {
                        combinePaths.add(antPathMatcher.combine(path1, path2));
                    }
                }
            }
        }
        if (fromController.size() == 0) {
            for (RequestMappingDto dto : fromHandler) {
                combineMethods.addAll(dto.getMethods());
                for (String path : dto.getPaths()) {
                    combinePaths.add(antPathMatcher.combine("", path));
                }
            }
        }
        if (fromHandler.size() == 0) {
            for (RequestMappingDto dto : fromController) {
                combineMethods.addAll(dto.getMethods());
                for (String path : dto.getPaths()) {
                    combinePaths.add(antPathMatcher.combine("", path));
                }
            }
        }

        return new RequestMappingProcessResult().methodTypes(combineMethods).uris(combinePaths);
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
            String annoQualifier = annotation.resolve().getId();
            RequestMappingDto dto;
            if (QualifierConstants.REQUEST_MAPPING.equals(annoQualifier)) {
                dto = this.parseRequestMapping(annotation, false);
            } else {
                Optional<MethodType> methodType = MethodType.ofAnnotationQualifier(annoQualifier);
                if (methodType.isPresent()) {
                    dto = this.parseRequestMapping(annotation, true).setMethods(methodType.get().inCollection());
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
            dto.setPaths(this.collectPaths(memberValue));
        });

        annotation.ifNormalAnnotationExpr(normal -> normal.getPairs().forEach(pair -> {
            if (pair.getNameAsString().equals("value")) {
                dto.setPaths(this.collectPaths(pair.getValue()));
            }
            if (pair.getNameAsString().equals("path")) {
                dto.getPaths().addAll(this.collectPaths(pair.getValue()));
            }
            if (!isSpecificMethod && pair.getNameAsString().equals("method")) {
                dto.getMethods().addAll(this.collectionMethods(pair.getValue()));
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
    private Collection<MethodType> collectionMethods(Expression memberValue) {
        Collection<MethodType> result = Lists.newArrayList();
        memberValue.ifArrayInitializerExpr(arrayInit -> arrayInit.getValues().forEach(arrayEle -> arrayEle
                .ifFieldAccessExpr(
                        fieldAccess -> MethodType.ofValue(fieldAccess.getNameAsString()).ifPresent(result::add))));
        memberValue.ifFieldAccessExpr(
                fieldAccess -> MethodType.ofValue(fieldAccess.getNameAsString()).ifPresent(result::add));
        return result;
    }

    @Data
    @Accessors(chain = true)
    private static class RequestMappingDto {

        private Collection<MethodType> methods = Lists.newArrayList();

        private Collection<String> paths = Lists.newArrayList();

    }

}
