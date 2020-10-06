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
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.spldeolin.allison1875.base.ancestor.Allison1875MainProcessor;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.base.util.ast.Locations;
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

                    ClassOrInterfaceDeclaration queryDesign;
                    try {
                        String queryDesignQualifier = mce.findAll(NameExpr.class).get(0).calculateResolvedType()
                                .describe();
                        Path queryDesignPath = Locations.getStorage(cu).getSourceRoot()
                                .resolve(queryDesignQualifier.replace('.', File.separatorChar) + ".java");
                        CompilationUnit queryDesignCu = StaticJavaParser.parse(queryDesignPath);
                        queryDesign = queryDesignCu.getType(0).asClassOrInterfaceDeclaration();
                    } catch (Exception e) {
                        log.warn("QueryDesign编写方式不正确", e);
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

                    // overwirte init
                    VariableDeclarator vd = (VariableDeclarator) mce.getParentNode().get();
                    vd.setInitializer(StaticJavaParser.parseExpression(""));
                }
            }
        }

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