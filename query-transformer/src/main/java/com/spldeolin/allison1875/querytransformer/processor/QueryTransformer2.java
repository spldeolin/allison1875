package com.spldeolin.allison1875.querytransformer.processor;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.CuAbsentException;
import com.spldeolin.allison1875.base.exception.FieldAbsentException;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Imports;
import com.spldeolin.allison1875.base.util.ast.Locations;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.querytransformer.enums.OperatorEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ConditionDto;
import com.spldeolin.allison1875.querytransformer.javabean.QueryMeta;

/**
 * @author Deolin 2020-10-06
 */
public class QueryTransformer2 implements Allison1875MainProcessor {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(QueryTransformer.class);

    @Override
    public void process(AstForest astForest) {
        for (CompilationUnit cu : astForest) {
            for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class)) {
                if (mce.getNameAsString().equals("over") && mce.getParentNode()
                        .filter(parent -> parent instanceof VariableDeclarator).isPresent()) {

                    ClassOrInterfaceDeclaration queryDesign = findQueryDesign(cu, mce);
                    if (queryDesign == null) {
                        continue;
                    }

                    QueryMeta queryMeta = JsonUtils
                            .toObject(queryDesign.getOrphanComments().get(0).getContent(), QueryMeta.class);


                    Deque<String> parts = Queues.newArrayDeque();
                    collectCondition(parts, mce);
                    if (parts.size() < 3) {
                        log.warn("QueryDesign编写方式不正确");
                    }
                    if (!Objects.equals(parts.pollFirst(), "over")) {
                        log.warn("QueryDesign编写方式不正确");
                    }
                    String queryMethodName = parts.pollLast();
                    if (queryMethodName == null || !queryMethodName.startsWith("\"") || !queryMethodName
                            .endsWith("\"")) {
                        log.warn("QueryDesign的design方法必须使用String字面量作为实际参数");
                        continue;
                    }
                    queryMethodName = queryMethodName.substring(1, queryMethodName.length() - 1);
                    if (!Objects.equals(parts.pollLast(), "design")) {
                        log.warn("QueryDesign编写方式不正确");
                    }

                    Collection<ConditionDto> conditions = Lists.newArrayList();
                    parts.descendingIterator().forEachRemaining(part -> {
                        ConditionDto condition;
                        if (queryMeta.getPropertyNames().contains(part)) {
                            condition = new ConditionDto();
                            conditions.add(condition);
                            condition.propertyName(part);
                            condition.columnName(StringUtils.lowerCamelToUnderscore(part));
                            condition.dollarVar("#{" + part + "}");
                        } else {
                            condition = Iterables.getLast(conditions);
                            if (OperatorEnum.isValid(part)) {
                                condition.operator(part);
                            } else {
                                condition.varName(part);
                            }
                        }
                    });

                    ClassOrInterfaceDeclaration entity = findEntity(cu, queryMeta);
                    if (entity == null) {
                        continue;
                    }

                    ClassOrInterfaceDeclaration mapper = findMapper(cu, queryMeta);
                    if (mapper == null) {
                        continue;
                    }

                    // create queryMethod for mapper
                    MethodDeclaration queryMethod = new MethodDeclaration();
                    queryMethod
                            .setType(StaticJavaParser.parseType(String.format("List<%s>", queryMeta.getEntityName())));
                    queryMethod.setName(queryMethodName);
                    for (ConditionDto condition : conditions) {
                        OperatorEnum operator = OperatorEnum.of(condition.operator());
                        if (operator == OperatorEnum.NOT_NULL || operator == OperatorEnum.IS_NULL) {
                            continue;
                        }
                        String propertyName = condition.propertyName();
                        String propertyType = entity.getFieldByName(propertyName).orElseThrow(FieldAbsentException::new)
                                .getCommonType().toString();
                        Parameter parameter = new Parameter();
                        parameter.addAnnotation(
                                StaticJavaParser.parseAnnotation(String.format("@Param(\"%s\")", propertyName)));
                        parameter.setType(propertyType);
                        parameter.setName(propertyName);
                        queryMethod.addParameter(parameter);
                        queryMethod.setBody(null);
                    }
                    mapper.getMembers().add(0, queryMethod);
                    Saves.save(mapper.findCompilationUnit().orElseThrow(CuAbsentException::new));

                    // overwirte init
                    VariableDeclarator vd = (VariableDeclarator) mce.getParentNode().get();
                    MethodCallExpr callQueryMethod = StaticJavaParser.parseExpression(
                            StringUtils.lowerFirstLetter(mapper.getNameAsString()) + "." + queryMethodName + "()")
                            .asMethodCallExpr();
                    for (ConditionDto condition : conditions) {
                        OperatorEnum operator = OperatorEnum.of(condition.operator());
                        if (operator == OperatorEnum.NOT_NULL || operator == OperatorEnum.IS_NULL) {
                            continue;
                        }
                        callQueryMethod.addArgument(condition.varName());
                    }
                    vd.setInitializer(callQueryMethod);

                    // 确保service import 和 autowired 了 mapper
                    vd.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(service -> {
                        if (!service.getFieldByName(StringUtils.lowerFirstLetter(mapper.getNameAsString()))
                                .isPresent()) {
                            service.getMembers().add(0, StaticJavaParser.parseBodyDeclaration(
                                    String.format("@Autowired private %s %s;", mapper.getNameAsString(),
                                            StringUtils.lowerFirstLetter(mapper.getNameAsString()))));
                            Imports.ensureImported(service, queryMeta.getMapperQualifier());
                            Imports.ensureImported(service, "org.springframework.beans.factory.annotation.Autowired");
                        }
                    });

                    Saves.save(cu);
                }
            }
        }

    }

    private ClassOrInterfaceDeclaration findEntity(CompilationUnit cu, QueryMeta queryMeta) {
        try {
            String mapperQualifier = queryMeta.getEntityQualifier();
            Path mapperPath = Locations.getStorage(cu).getSourceRoot()
                    .resolve(mapperQualifier.replace('.', File.separatorChar) + ".java");
            return StaticJavaParser.parse(mapperPath).getTypes().get(0).asClassOrInterfaceDeclaration();
        } catch (Exception e) {
            log.warn("寻找Entity失败", e);
            return null;
        }
    }

    private ClassOrInterfaceDeclaration findMapper(CompilationUnit cu, QueryMeta queryMeta) {
        try {
            String mapperQualifier = queryMeta.getMapperQualifier();
            Path mapperPath = Locations.getStorage(cu).getSourceRoot()
                    .resolve(mapperQualifier.replace('.', File.separatorChar) + ".java");
            return StaticJavaParser.parse(mapperPath).getTypes().get(0).asClassOrInterfaceDeclaration();
        } catch (Exception e) {
            log.warn("寻找Mapper失败", e);
            return null;
        }
    }

    private ClassOrInterfaceDeclaration findQueryDesign(CompilationUnit cu, MethodCallExpr mce) {
        ClassOrInterfaceDeclaration queryDesign;
        try {
            String queryDesignQualifier = mce.findAll(NameExpr.class).get(0).calculateResolvedType().describe();
            Path queryDesignPath = Locations.getStorage(cu).getSourceRoot()
                    .resolve(queryDesignQualifier.replace('.', File.separatorChar) + ".java");
            CompilationUnit queryDesignCu = StaticJavaParser.parse(queryDesignPath);
            queryDesign = queryDesignCu.getType(0).asClassOrInterfaceDeclaration();
        } catch (Exception e) {
            log.warn("QueryDesign编写方式不正确", e);
            return null;
        }
        return queryDesign;
    }


    private void collectCondition(Deque<String> parts, Expression scope) {
        scope.ifMethodCallExpr(mce -> {
            String operator = mce.getNameAsString();
            parts.add(operator);
            NodeList<Expression> arguments = scope.asMethodCallExpr().getArguments();
            if (arguments.size() > 0) {
                parts.add(arguments.get(0).toString());
            }
            mce.getScope().ifPresent(scopeEx -> this.collectCondition(parts, scopeEx));
        });

        scope.ifFieldAccessExpr(fae -> {
            String propertyName = scope.asFieldAccessExpr().getNameAsString();
            parts.add(propertyName);
            this.collectCondition(parts, fae.getScope());
        });
    }

}