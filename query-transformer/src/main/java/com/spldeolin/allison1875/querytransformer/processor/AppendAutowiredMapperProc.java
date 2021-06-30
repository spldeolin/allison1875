package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import java.util.Set;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.google.common.collect.Iterables;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Saves.Replace;
import com.spldeolin.allison1875.base.util.ast.TokenRanges;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;

/**
 * @author Deolin 2021-06-30
 */
@Singleton
public class AppendAutowiredMapperProc {

    public void append(Set<String> autowiredMappers, List<Replace> replaces, MethodCallExpr chain,
            DesignMeta designMeta) {
        // ensure mapper autowired
        if (!autowiredMappers.contains(designMeta.getMapperQualifier())) {
            chain.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(service -> {
                if (!service.getFieldByName(MoreStringUtils.lowerFirstLetter(designMeta.getMapperName())).isPresent()) {
                    List<FieldDeclaration> fields = service.getFields();
                    if (fields.size() > 0) {
                        String lastFieldCode = TokenRanges.getRawCode(Iterables.getLast(fields));
                        String indent = TokenRanges.getStartIndent(Iterables.getLast(fields));
                        String autowiredField = String
                                .format("@Autowired\n%sprivate %s %s;", indent, designMeta.getMapperName(),
                                        MoreStringUtils.lowerFirstLetter(designMeta.getMapperName()));
                        replaces.add(new Replace(lastFieldCode, lastFieldCode + "\n\n" + indent + autowiredField));
                    } else {
                        List<MethodDeclaration> methods = service.getMethods();
                        if (methods.size() > 0) {
                            String firstMethodCode = TokenRanges.getRawCode(methods.get(0));
                            String indent = TokenRanges.getStartIndent(methods.get(0));
                            String autowiredField = String
                                    .format("@Autowired\n%sprivate %s %s;", indent, designMeta.getMapperName(),
                                            MoreStringUtils.lowerFirstLetter(designMeta.getMapperName()));
                            replaces.add(
                                    new Replace(firstMethodCode, autowiredField + "\n\n" + indent + firstMethodCode));
                        }
                    }
                }
            });
            autowiredMappers.add(designMeta.getMapperQualifier());
        }
    }

}