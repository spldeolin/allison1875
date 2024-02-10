package com.spldeolin.allison1875.sqlapigenerator.service.impl;

import java.util.stream.IntStream;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.AnnotationConstant;
import com.spldeolin.allison1875.common.exception.QualifierAbsentException;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.sqlapigenerator.service.AddAutowiredService;

/**
 * @author Deolin 2024-01-23
 */
@Singleton
public class AddAutowiredServiceImpl implements AddAutowiredService {

    @Override
    public void ensureAuwired(ClassOrInterfaceDeclaration toBeAutowired, ClassOrInterfaceDeclaration coid) {
        String qualifier = toBeAutowired.getFullyQualifiedName()
                .orElseThrow(() -> new QualifierAbsentException(toBeAutowired));

        String fieldVarName = MoreStringUtils.lowerFirstLetter(toBeAutowired.getNameAsString());
        if (!coid.getFieldByName(fieldVarName).isPresent()) {
            NodeList<BodyDeclaration<?>> members = coid.getMembers();
            int lastIndexOfFieldDeclaration = IntStream.range(0, members.size())
                    .filter(i -> members.get(i) instanceof FieldDeclaration).reduce((first, second) -> second)
                    .orElse(-1);
            FieldDeclaration field = new ClassOrInterfaceDeclaration().addField(qualifier, fieldVarName,
                    Keyword.PRIVATE);
            field.addAnnotation(AnnotationConstant.AUTOWIRED_FULL);
            members.add(lastIndexOfFieldDeclaration + 1, field);
        }
    }

}