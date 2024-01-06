package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Iterables;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.service.AppendAutowiredMapperService;

/**
 * @author Deolin 2021-06-30
 */
@Singleton
public class AppendAutowiredMapperServiceImpl implements AppendAutowiredMapperService {

    @Override
    public void append(MethodCallExpr chain, DesignMeta designMeta) {
        // ensure mapper autowired
        chain.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(service -> {
            if (!service.getFieldByName(MoreStringUtils.lowerFirstLetter(designMeta.getMapperName())).isPresent()) {
                BodyDeclaration<?> autowiredField = StaticJavaParser.parseBodyDeclaration(
                        "@Autowired private " + designMeta.getMapperName() + " " + MoreStringUtils.lowerFirstLetter(
                                designMeta.getMapperName() + ";"));

                List<FieldDeclaration> fields = service.getFields();
                if (fields.size() > 0) {
                    service.getMembers().addAfter(autowiredField, Iterables.getLast(fields));
                } else {
                    service.getMembers().add(0, autowiredField);
                }
            }
        });
    }

}