package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Iterables;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMetaDto;
import com.spldeolin.allison1875.querytransformer.service.AutowiredMapperAdderService;

/**
 * @author Deolin 2021-06-30
 */
@Singleton
public class AutowiredMapperAdderServiceImpl implements AutowiredMapperAdderService {

    @Override
    public void addAutowiredMapper(MethodCallExpr chain, DesignMetaDto designMeta) {
        // ensure mapper autowired
        chain.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(service -> {
            if (!service.getFieldByName(MoreStringUtils.lowerFirstLetter(designMeta.getMapperName())).isPresent()) {
                BodyDeclaration<?> autowiredField = StaticJavaParser.parseBodyDeclaration(
                        "@org.springframework.beans.factory.annotation.Autowired private "
                                + designMeta.getMapperQualifier() + " " + MoreStringUtils.lowerFirstLetter(
                                designMeta.getMapperName() + ";"));

                List<FieldDeclaration> fields = service.getFields();
                if (CollectionUtils.isNotEmpty(fields)) {
                    service.getMembers().addAfter(autowiredField, Iterables.getLast(fields));
                } else {
                    service.getMembers().add(0, autowiredField);
                }
            }
        });
    }

}