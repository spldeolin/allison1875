package com.spldeolin.allison1875.querytransformer.service.impl;

import static com.spldeolin.allison1875.common.constant.BaseConstant.DOUBLE_INDENT;
import static com.spldeolin.allison1875.common.constant.BaseConstant.SINGLE_INDENT;
import static com.spldeolin.allison1875.common.constant.BaseConstant.TREBLE_INDENT;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.PrimitiveType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.CommonConfig;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.service.AntiDuplicationService;
import com.spldeolin.allison1875.common.service.ImportExprService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.CompilationUnitUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.constant.KeywordConstant.ChainInitialMethod;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.DesignMetaDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.querytransformer.config.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.dto.AssignmentDTO;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.GenerateMethodToMapperArgs;
import com.spldeolin.allison1875.querytransformer.dto.GenerateMethodToMapperXmlArgs;
import com.spldeolin.allison1875.querytransformer.dto.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.GenerateReturnTypeRetval;
import com.spldeolin.allison1875.querytransformer.dto.JoinClauseDTO;
import com.spldeolin.allison1875.querytransformer.dto.JoinConditionDTO;
import com.spldeolin.allison1875.querytransformer.dto.JoinedPropertyDTO;
import com.spldeolin.allison1875.querytransformer.dto.SearchConditionDTO;
import com.spldeolin.allison1875.querytransformer.dto.SortPropertyDTO;
import com.spldeolin.allison1875.querytransformer.dto.XmlSourceFile;
import com.spldeolin.allison1875.querytransformer.enums.ComparisonOperatorEnum;
import com.spldeolin.allison1875.querytransformer.enums.OrderSequenceEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnStyleEnum;
import com.spldeolin.allison1875.querytransformer.service.MapperLayerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-10-10
 */
@Singleton
@Slf4j
public class MapperLayerServiceImpl implements MapperLayerService {

    public static final String SINGLE_INDENT_WITH_AND = SINGLE_INDENT + "  AND ";

    @Inject
    private CommonConfig commonConfig;

    @Inject
    private QueryTransformerConfig queryTransformerConfig;

    @Inject
    private AntiDuplicationService antiDuplicationService;

    @Inject
    private ImportExprService importExprService;

    @Override
    public void generateMethodToMapper(GenerateMethodToMapperArgs args) {
        ClassOrInterfaceDeclaration mapper = this.findMapper(args.getMapperQualifier(), args.getMethodAddedMappers());
        if (mapper == null) {
            return;
        }
        ChainAnalysisDTO chainAnalysis = args.getChainAnalysis();

        if (args.getChainAnalysis().getReturnStyle() == ReturnStyleEnum.PAGE) {
            // 除了query还需要生成count方法
            String methodName = chainAnalysis.getCountMethodNameForPage();
            methodName = antiDuplicationService.getNewMethodNameIfExist(methodName, mapper);
            log.info("anti duplication worked completed, new method name '{}' update to ChainAnalysisDTO.methodName, "
                    + "old={}", methodName, chainAnalysis.getMethodName());
            chainAnalysis.setCountMethodNameForPage(methodName);

            MethodDeclaration method = new MethodDeclaration();
            if (commonConfig.getEnableLotNoAnnounce()) {
                method.setJavadocComment(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo());
            }
            method.setType("long");
            method.setName(methodName);
            method.setParameters(new NodeList<>(args.getCloneParameters()));
            method.setBody(null);
            mapper.getMembers().add(method);
        }

        String methodName = chainAnalysis.getMethodName();
        methodName = antiDuplicationService.getNewMethodNameIfExist(methodName, mapper);
        log.info(
                "anti duplication worked completed, new method name '{}' update to ChainAnalysisDTO.methodName, old={}",
                methodName, chainAnalysis.getMethodName());
        chainAnalysis.setMethodName(methodName);

        MethodDeclaration method = new MethodDeclaration();
        if (commonConfig.getEnableLotNoAnnounce()) {
            method.setJavadocComment(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo());
        }
        // 增加Mybatis @MapKey注解
        if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.MAP) {
            method.addAnnotation(StaticJavaParser.parseAnnotation(
                    String.format("@org.apache.ibatis.annotations.MapKey(\"%s\")",
                            chainAnalysis.getMapOrGroupKeyProperty().getPropertyName())));
        }
        method.setType(args.getClonedReturnType());
        method.setName(methodName);
        method.setParameters(new NodeList<>(args.getCloneParameters()));
        method.setBody(null);
        mapper.getMembers().add(method);

        CompilationUnit cu = mapper.findCompilationUnit()
                .orElseThrow(() -> new Allison1875Exception("cannot find cu for " + mapper.getName()));
        importExprService.extractQualifiedTypeToImport(cu);
        CompilationUnitUtils.writeJava(cu);
    }

    @Override
    public void generateMethodToMapperXml(GenerateMethodToMapperXmlArgs args) {
        ChainAnalysisDTO chainAnalysis = args.getChainAnalysis();
        DesignMetaDTO designMeta = args.getDesignMeta();
        GenerateParamRetval generateParamRetval = args.getGenerateParamRetval();
        GenerateReturnTypeRetval generateReturnTypeRetval = args.getGenerateReturnTypeRetval();

        for (String mapperPath : designMeta.getMapperPaths()) {
            XmlSourceFile mapperXml = this.findMapperXml(mapperPath, args.getMethodAddedMapperXmls());
            if (mapperXml == null) {
                continue;
            }

            List<String> xmlLines = Lists.newArrayList();
            xmlLines.add("");
            if (chainAnalysis.getChainInitialMethod() == ChainInitialMethod.SELECT) {
                // QUERY
                ArrayList<JoinClauseDTO> joinClauses = Lists.newArrayList(chainAnalysis.getJoinClauses());
                boolean join = !joinClauses.isEmpty();

                if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
                    // 分页场景除了query还需要生成count方法
                    xmlLines.add(concatLotNoComment(chainAnalysis));
                    String startTag = this.concatSelectStartTag(null, chainAnalysis.getCountMethodNameForPage(),
                            generateParamRetval,
                            new GenerateReturnTypeRetval().setResultType(PrimitiveType.longType()));
                    xmlLines.add(startTag);
                    // select部分
                    xmlLines.add(SINGLE_INDENT + "SELECT COUNT(*)");
                    // from部分
                    xmlLines.add(SINGLE_INDENT + "FROM " + designMeta.getTableName() + (join ? " t1" : ""));
                    for (int i = 0; i < joinClauses.size(); i++) {
                        xmlLines.addAll(concatJoinSection(joinClauses.get(i), i));
                    }
                    // where部分
                    xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, true));
                    xmlLines.add("</select>");
                }

                xmlLines.add(concatLotNoComment(chainAnalysis));
                String startTag = this.concatSelectStartTag(designMeta.getEntityQualifier(),
                        chainAnalysis.getMethodName(), generateParamRetval, generateReturnTypeRetval);
                xmlLines.add(startTag);

                // select 部分
                if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.COUNT) {
                    xmlLines.add(SINGLE_INDENT + "SELECT COUNT(*)");
                } else if (CollectionUtils.isEmpty(chainAnalysis.getSelectProperties())) {
                    if (join) {
                        // 有join时，最外层的select_expr需要加上t1.
                        xmlLines.add(SINGLE_INDENT + "SELECT");
                        for (PropertyDTO property : designMeta.getProperties().values()) {
                            xmlLines.add(DOUBLE_INDENT + "t1." + property.getColumnName() + " AS "
                                    + property.getPropertyName() + ",");
                        }
                        // 有join时，还需要select joinedProperties
                        for (int i = 0; i < joinClauses.size(); i++) {
                            JoinClauseDTO joinClause = joinClauses.get(i);
                            for (JoinedPropertyDTO joinedProp : joinClause.getJoinedProperties()) {
                                xmlLines.add(
                                        DOUBLE_INDENT + "t" + (i + 2) + "." + joinedProp.getProperty().getColumnName()
                                                + " AS " + joinedProp.getVarName() + ",");
                            }
                        }
                    } else {
                        xmlLines.add(SINGLE_INDENT + "SELECT <include refid=\"all\"/>");
                    }
                } else {
                    xmlLines.add(SINGLE_INDENT + "SELECT");
                    for (PropertyDTO property : chainAnalysis.getSelectProperties()) {
                        xmlLines.add(DOUBLE_INDENT + (join ? "t1." : "") + property.getColumnName() + " AS "
                                + property.getPropertyName() + ",");
                    }
                    // 删除最后一个语句中，最后的逗号
                    int last = xmlLines.size() - 1;
                    xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
                }
                // from部分
                xmlLines.add(SINGLE_INDENT + "FROM " + designMeta.getTableName() + (join ? " t1" : ""));
                for (int i = 0; i < joinClauses.size(); i++) {
                    xmlLines.addAll(concatJoinSection(joinClauses.get(i), i));
                }
                // where部分
                xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, true));
                // order by部分
                if (CollectionUtils.isNotEmpty(chainAnalysis.getSortProperties())) {
                    xmlLines.add(SINGLE_INDENT + "ORDER BY");
                    for (SortPropertyDTO sortProp : chainAnalysis.getSortProperties()) {
                        PropertyDTO property = designMeta.getProperties().get(sortProp.getPropertyName());
                        xmlLines.add(DOUBLE_INDENT + (join ? "t1." : "") + property.getColumnName() + (
                                sortProp.getOrderSequence() == OrderSequenceEnum.DESC ? " DESC," : ","));
                    }
                    // 删除最后一个语句中，最后的逗号
                    int last = xmlLines.size() - 1;
                    xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
                }
                // limit部分
                if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.ONE) {
                    xmlLines.add(SINGLE_INDENT + "LIMIT 1");
                }
                if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
                    xmlLines.add(SINGLE_INDENT + "LIMIT #{offset}, #{limit}");
                }

                xmlLines.add("</select>");
            } else if (chainAnalysis.getChainInitialMethod() == ChainInitialMethod.UPDATE) {
                // UPDATE
                xmlLines.add(concatLotNoComment(chainAnalysis));
                String startTag = concatUpdateStartTag(chainAnalysis, generateParamRetval);
                xmlLines.add(startTag);
                xmlLines.add(SINGLE_INDENT + "UPDATE " + designMeta.getTableName());
                xmlLines.add(SINGLE_INDENT + "SET");
                for (AssignmentDTO assignment : chainAnalysis.getAssignments()) {
                    PropertyDTO property = designMeta.getProperties().get(assignment.getProperty().getPropertyName());
                    xmlLines.add(DOUBLE_INDENT + property.getColumnName() + " = #{" + assignment.getVarName() + "},");
                }
                // 删除最后一个语句中，最后的逗号
                int last = xmlLines.size() - 1;
                xmlLines.set(last, MoreStringUtils.replaceLast(xmlLines.get(last), ",", ""));
                xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, true));
                xmlLines.add("</update>");
            } else if (chainAnalysis.getChainInitialMethod() == ChainInitialMethod.DELETE) {
                // DROP
                xmlLines.add(concatLotNoComment(chainAnalysis));
                String startTag = concatDeleteStartTag(chainAnalysis, generateParamRetval);
                xmlLines.add(startTag);
                xmlLines.add(SINGLE_INDENT + "DELETE FROM " + designMeta.getTableName());
                xmlLines.addAll(concatWhereSection(designMeta, chainAnalysis, false));
                xmlLines.add("</delete>");
            } else {
                throw new Allison1875Exception("impossible unless bug.");
            }

            List<String> newLines = Lists.newArrayList();
            List<String> lines = mapperXml.getContentLines();
            Collections.reverse(lines);
            for (String line : lines) {
                newLines.add(line);
                if (line.contains("</mapper>")) {
                    Collections.reverse(xmlLines);
                    for (String xmlLine : xmlLines) {
                        if (StringUtils.isNotBlank(xmlLine)) {
                            newLines.add(SINGLE_INDENT + xmlLine);
                        }
                    }
                    newLines.add("");
                }
            }
            Collections.reverse(newLines);

            // 作为本次queryChain的处理结果记录到map
            mapperXml.setContentLines(newLines);
        }
    }

    private List<String> concatJoinSection(JoinClauseDTO joinClause, int i) {
        i += 2;
        List<String> xmlLines = Lists.newArrayList();
        String joinSql = DOUBLE_INDENT + joinClause.getJoinType().getSql() + " " + joinClause.getJoinedDesignMeta()
                .getTableName() + " t" + i + " ON ";
        if (joinClause.getJoinConditions().size() == 1) {
            JoinConditionDTO joinCond = Iterables.getOnlyElement(joinClause.getJoinConditions());
            String onBinary = concatOnBinary(i, joinCond);
            xmlLines.add(joinSql + onBinary);
        } else {
            // join有多个joinCond时，每个joinCond占1行
            xmlLines.add(joinSql + "(");
            for (JoinConditionDTO joinCond : joinClause.getJoinConditions()) {
                xmlLines.add(TREBLE_INDENT + concatOnBinary(i, joinCond));
            }
            xmlLines.add(DOUBLE_INDENT + ")");
        }
        return xmlLines;
    }

    private static String concatOnBinary(int i, JoinConditionDTO joinCond) {
        String onBinary = "t" + i + "." + joinCond.getProperty().getColumnName();
        switch (joinCond.getComparisonOperator()) {
            case EQUALS:
                onBinary += " = ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1." + joinCond.getComparedProperty().getColumnName();
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case NOT_EQUALS:
                onBinary += " != ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1." + joinCond.getComparedProperty().getColumnName();
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case IN:
                // 只可能为argument
                onBinary += " IN (<foreach collection='" + joinCond.getVarName()
                        + "' item='one' separator=','>#{one}</foreach>)";
                break;
            case NOT_IN:
                // 只可能为argument
                onBinary += " NOT IN (<foreach collection='" + joinCond.getVarName()
                        + "' item='one' separator=','>#{one}</foreach>)";
                break;
            case GREATER_THEN:
                onBinary += " > ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1." + joinCond.getComparedProperty().getColumnName();
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case GREATER_OR_EQUALS:
                onBinary += " >= ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1." + joinCond.getComparedProperty().getColumnName();
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case LESS_THEN:
                onBinary += " < ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1." + joinCond.getComparedProperty().getColumnName();
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case LESS_OR_EQUALS:
                onBinary += " <= ";
                if (joinCond.getComparedProperty() != null) {
                    onBinary += "t1." + joinCond.getComparedProperty().getColumnName();
                } else {
                    onBinary += "#{" + joinCond.getVarName() + "}";
                }
                break;
            case NOT_NULL:
                // 只可能为argument
                onBinary += " IS NOT NULL";
                break;
            case IS_NULL:
                // 只可能为argument
                onBinary += " IS NULL";
                break;
            case LIKE:
                // 只可能为argument
                onBinary += " LIKE CONCAT('%', #{" + joinCond.getVarName() + "}, '%')";
                break;
        }
        return onBinary;
    }


    private ClassOrInterfaceDeclaration findMapper(String mapperQualifier,
            Map<String, ClassOrInterfaceDeclaration> methodAddedMappers) {
        // 尝试先从其他queryChain的处理结果中获取mapper
        if (methodAddedMappers.containsKey(mapperQualifier)) {
            return methodAddedMappers.get(mapperQualifier);
        }
        Path sourceRoot = Optional.ofNullable(queryTransformerConfig.getPersistenceSourcePath()).map(File::toPath)
                .orElse(AstForestContext.get().getSourceRoot());
        Optional<CompilationUnit> cu = CompilationUnitUtils.tryFindCu(sourceRoot, mapperQualifier);
        if (!cu.isPresent()) {
            return null;
        }
        Optional<TypeDeclaration<?>> pt = cu.get().getPrimaryType();
        if (!pt.isPresent()) {
            return null;
        }
        if (!pt.get().isClassOrInterfaceDeclaration()) {
            return null;
        }
        ClassOrInterfaceDeclaration mapper = pt.get().asClassOrInterfaceDeclaration();

        // 作为本次queryChain的处理结果记录到map
        methodAddedMappers.put(mapperQualifier, mapper);

        return mapper;
    }

    private XmlSourceFile findMapperXml(String mapperPath, Map<String, XmlSourceFile> methodAddedMapperXmls) {
        // 尝试先从其他queryChain的处理结果中获取mapperXml
        if (methodAddedMapperXmls.containsKey(mapperPath)) {
            return methodAddedMapperXmls.get(mapperPath);
        }

        File mapperXml = new File(mapperPath);
        if (queryTransformerConfig.getPersistenceSourcePath() != null) {
            mapperXml = queryTransformerConfig.getPersistenceSourcePath().toPath().resolve(mapperPath).toFile();
        }
        if (!mapperXml.exists()) {
            return null;
        }
        XmlSourceFile result = new XmlSourceFile(mapperXml);

        // 作为本次queryChain的处理结果记录到map
        methodAddedMapperXmls.put(mapperPath, result);

        return result;
    }

    private List<String> concatWhereSection(DesignMetaDTO designMeta, ChainAnalysisDTO chainAnalysis,
            boolean needNotDeletedSql) {
        List<String> xmlLines = Lists.newArrayList();
        boolean join = !chainAnalysis.getJoinClauses().isEmpty();
        xmlLines.add(SINGLE_INDENT + "<where>");
        if (needNotDeletedSql && designMeta.getNotDeletedSql() != null) {
            xmlLines.add(SINGLE_INDENT + "  AND " + designMeta.getNotDeletedSql());
        }
        for (SearchConditionDTO searchCond : chainAnalysis.getSearchConditions()) {
            PropertyDTO property = searchCond.getProperty();
            String varName = searchCond.getVarName();
            String dollarVar = "#{" + varName + "}";

            String ifTag = SINGLE_INDENT + "<if test=\"" + varName + " != null";
            if (property.getJavaType().getQualifier().equals("java.lang.String")) {
                ifTag += " and " + varName + " != ''";
            }
            ifTag += "\">";
            switch (searchCond.getComparisonOperator()) {
                case EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + property.getColumnName() + " = "
                                + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + property.getColumnName() + " = "
                                + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + property.getColumnName() + " != "
                                + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + property.getColumnName() + " != "
                                + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case IN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + property.getColumnName()
                                + " IN (<foreach collection='" + varName
                                + "' item='one' separator=','>#{one}</foreach>)");
                    } else {
                        xmlLines.add(SINGLE_INDENT + "<if test=\"" + varName + " != null\">");
                        xmlLines.add(DOUBLE_INDENT + "<if test=\"" + varName + ".size() > 0\">");
                        xmlLines.add(TREBLE_INDENT + "AND " + (join ? "t1." : "") + property.getColumnName()
                                + " IN (<foreach collection='" + varName
                                + "' item='one' separator=','>#{one}</foreach>)");
                        xmlLines.add(DOUBLE_INDENT + "</if>");
                        xmlLines.add(DOUBLE_INDENT + "<if test=\"" + varName + ".size() == 0\">");
                        xmlLines.add(TREBLE_INDENT + "AND 1 != 1");
                        xmlLines.add(DOUBLE_INDENT + "</if>");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_IN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + property.getColumnName()
                                + " NOT IN (<foreach collection='" + varName
                                + "' item='one' separator=','>#{one}</foreach>)");
                    } else {
                        xmlLines.add(
                                SINGLE_INDENT + String.format("<if test=\"%s != null and %s.size() > 0\">", varName,
                                        varName));
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + property.getColumnName()
                                + " NOT IN (<foreach collection='" + varName
                                + "' item='one' separator=','>#{one}</foreach>)");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case GREATER_THEN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + property.getColumnName() + " > "
                                + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + property.getColumnName() + " > "
                                + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case GREATER_OR_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + property.getColumnName() + " >= "
                                + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + property.getColumnName() + " >= "
                                + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case LESS_THEN:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + property.getColumnName() + " &lt; "
                                + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + property.getColumnName() + " &lt; "
                                + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case LESS_OR_EQUALS:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + property.getColumnName() + " &lt;= "
                                + dollarVar);
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + property.getColumnName() + " &lt;= "
                                + dollarVar);
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
                case NOT_NULL:
                    xmlLines.add(
                            SINGLE_INDENT + "  AND " + (join ? "t1." : "") + property.getColumnName() + " IS NOT NULL");
                    break;
                case IS_NULL:
                    xmlLines.add(
                            SINGLE_INDENT + "  AND " + (join ? "t1." : "") + property.getColumnName() + " IS NULL");
                    break;
                case LIKE:
                    if (chainAnalysis.getIsByForced()) {
                        xmlLines.add(SINGLE_INDENT_WITH_AND + (join ? "t1." : "") + property.getColumnName()
                                + " LIKE CONCAT('%', " + dollarVar + ", '%')");
                    } else {
                        xmlLines.add(ifTag);
                        xmlLines.add(DOUBLE_INDENT + "AND " + (join ? "t1." : "") + property.getColumnName()
                                + " LIKE CONCAT('%', " + dollarVar + ", '%')");
                        xmlLines.add(SINGLE_INDENT + "</if>");
                    }
                    break;
            }
        }
        xmlLines.add(SINGLE_INDENT + "</where>");
        if (xmlLines.size() == 2) {
            // 代表<where></where>中没有内容
            return Lists.newArrayList();
        }
        return xmlLines;
    }

    private String concatLotNoComment(ChainAnalysisDTO chainAnalysis) {
        if (commonConfig.getEnableLotNoAnnounce()) {
            return "<!-- " + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + chainAnalysis.getLotNo() + " -->";
        }
        return "";
    }

    private String concatSelectStartTag(String entityQualifier, String methodName, GenerateParamRetval paramGeneration,
            GenerateReturnTypeRetval resultGeneration) {
        String startTag = "<select id='" + methodName + "'";
        if (paramGeneration.getParameters().size() == 1) {
            Parameter onlyParam = paramGeneration.getParameters().get(0);
            if (onlyParam.getAnnotations().stream()
                    .noneMatch(a -> a.getNameAsString().equals("org.apache.ibatis.annotations.Param"))) {
                startTag += " parameterType='" + onlyParam.getTypeAsString() + "'";
            }
        }
        if (resultGeneration.getElementTypeQualifier() != null && !resultGeneration.getElementTypeQualifier()
                .equals(entityQualifier)) {
            startTag += " resultType='" + resultGeneration.getElementTypeQualifier() + "'>";
        } else if (resultGeneration.getResultType().equals(PrimitiveType.longType())) {
            startTag += " resultType='long'>";
        } else if (resultGeneration.getResultType().equals(PrimitiveType.intType())) {
            startTag += " resultType='int'>";
        } else {
            startTag += " resultMap='all'>";
        }
        return startTag;
    }

    private String concatUpdateStartTag(ChainAnalysisDTO chainAnalysis, GenerateParamRetval paramGeneration) {
        String startTag = "<update id='" + chainAnalysis.getMethodName() + "'";
        if (paramGeneration.getParameters().size() == 1) {
            startTag += " parameterType='" + paramGeneration.getParameters().get(0).getTypeAsString() + "'";
        }
        startTag += ">";
        return startTag;
    }

    private String concatDeleteStartTag(ChainAnalysisDTO chainAnalysis, GenerateParamRetval paramGeneration) {
        String startTag = "<delete id='" + chainAnalysis.getMethodName() + "'";
        if (paramGeneration.getParameters().size() == 1) {
            ComparisonOperatorEnum comparison = Iterables.getOnlyElement(chainAnalysis.getSearchConditions())
                    .getComparisonOperator();
            if (comparison == ComparisonOperatorEnum.IN || comparison == ComparisonOperatorEnum.NOT_IN) {
                startTag += ">";
                return startTag;
            }
        }
        startTag += " parameterType='" + paramGeneration.getParameters().get(0).getTypeAsString() + "'";
        startTag += ">";
        return startTag;
    }

}