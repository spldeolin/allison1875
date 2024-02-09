package com.spldeolin.allison1875.sqlapigenerator.service.impl;

import java.util.stream.IntStream;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.constant.AnnotationConstant;
import com.spldeolin.allison1875.common.constant.ImportConstant;
import com.spldeolin.allison1875.common.exception.CuAbsentException;
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
        CompilationUnit cu = coid.findCompilationUnit().orElseThrow(() -> new CuAbsentException(coid));
        String qualifier = toBeAutowired.getFullyQualifiedName()
                .orElseThrow(() -> new QualifierAbsentException(toBeAutowired));
        cu.addImport(qualifier);
        cu.addImport(ImportConstant.GOOGLE_COMMON_COLLECTION);
        cu.addImport(ImportConstant.JAVA_UTIL);
        cu.addImport(ImportConstant.JAVA_TIME);
        cu.addImport(ImportConstant.SPRING_AUTOWIRED);

        String fieldVarName = MoreStringUtils.lowerFirstLetter(toBeAutowired.getNameAsString());
        if (!coid.getFieldByName(fieldVarName).isPresent()) {
            NodeList<BodyDeclaration<?>> members = coid.getMembers();
            int lastIndexOfFieldDeclaration = IntStream.range(0, members.size())
                    .filter(i -> members.get(i) instanceof FieldDeclaration).reduce((first, second) -> second)
                    .orElse(-1);
            FieldDeclaration field = new ClassOrInterfaceDeclaration().addField(toBeAutowired.getNameAsString(),
                    fieldVarName, Keyword.PRIVATE);
            field.addAnnotation(AnnotationConstant.AUTOWIRED);
            members.add(lastIndexOfFieldDeclaration + 1, field);
        }
    }

}