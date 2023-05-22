package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.constant.ImportConstants;
import com.spldeolin.allison1875.base.exception.ParentAbsentException;
import com.spldeolin.allison1875.base.util.EqualsUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import com.spldeolin.allison1875.querytransformer.enums.ReturnClassifyEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.MapOrMultimapBuiltDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-09
 */
@Log4j2
@Singleton
public class TransformMethodCallProc {

    public String methodCallExpr(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation) {
        String result =
                MoreStringUtils.lowerFirstLetter(designMeta.getMapperName()) + "." + chainAnalysis.getMethodName()
                        + "(";
        if (parameterTransformation != null && parameterTransformation.getIsJavabean()) {
            result += MoreStringUtils.lowerFirstLetter(
                    parameterTransformation.getParameters().get(0).getTypeAsString());
        } else {
            Set<PhraseDto> phrases = chainAnalysis.getUpdatePhrases();
            phrases.addAll(chainAnalysis.getByPhrases());
            result += phrases.stream().filter(p -> p.getPredicate() != PredicateEnum.IS_NULL
                            && p.getPredicate() != PredicateEnum.NOT_NULL).map(p -> p.getObjectExpr().toString())
                    .collect(Collectors.joining(", "));
        }
        result += ")";
        log.info("Method Call built [{}]", result);

        return result;
    }

    public String argumentBuildStmts(ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation) {
        if (parameterTransformation == null || !parameterTransformation.getIsJavabean()) {
            return null;
        }

        log.info("build Javabean setter call");
        String javabeanTypeName = parameterTransformation.getParameters().get(0).getTypeAsString();
        String javabeanVarName = MoreStringUtils.lowerFirstLetter(javabeanTypeName);
        StringBuilder result = new StringBuilder();
        result.append(String.format("final %s %s = new %s();", javabeanTypeName, javabeanVarName, javabeanTypeName));
        for (PhraseDto updatePhrase : chainAnalysis.getUpdatePhrases()) {
            result.append("\n").append(chainAnalysis.getIndent()).append(javabeanVarName).append(".set")
                    .append(MoreStringUtils.upperFirstLetter(updatePhrase.getVarName())).append("(")
                    .append(updatePhrase.getObjectExpr()).append(");");
        }
        for (PhraseDto byPhrase : chainAnalysis.getByPhrases()) {
            if (EqualsUtils.equalsAny(byPhrase.getPredicate(), PredicateEnum.IS_NULL, PredicateEnum.NOT_NULL)) {
                continue;
            }
            result.append("\n").append(chainAnalysis.getIndent()).append(javabeanVarName).append(".set")
                    .append(MoreStringUtils.upperFirstLetter(byPhrase.getVarName())).append("(")
                    .append(byPhrase.getObjectExpr()).append(");");
        }
        return result.toString();
    }

    public MapOrMultimapBuiltDto mapOrMultimapBuildStmts(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ResultTransformationDto resultTransformation) {
        Map<String, PropertyDto> properties = designMeta.getProperties();

        if (chainAnalysis.getReturnClassify() == ReturnClassifyEnum.each) {
            String propertyName = chainAnalysis.getChain().getArgument(0).asFieldAccessExpr().getNameAsString();
            String propertyTypeName = properties.get(propertyName).getJavaType().getSimpleName();
            String elementTypeName = StringUtils.substringAfterLast(resultTransformation.getElementTypeQualifier(),
                    ".");

            boolean isAssignWithoutType = (chainAnalysis.getChain().getParentNode().get().getParentNode()
                    .filter(gp -> gp instanceof ExpressionStmt)).isPresent();

            String code;
            if (isAssignWithoutType) {
                code = String.format("%s = Maps.newHashMap();", calcResultVarName(chainAnalysis));
            } else {
                code = String.format("final Map<%s, %s> %s = Maps.newHashMap();", propertyTypeName, elementTypeName,
                        calcResultVarName(chainAnalysis));
            }
            code += "\n" + chainAnalysis.getIndent() + String.format("%sList.forEach(one -> %s.put(one.get%s(), one));",
                    chainAnalysis.getMethodName(), calcResultVarName(chainAnalysis),
                    MoreStringUtils.upperFirstLetter(propertyName));
            MapOrMultimapBuiltDto result = new MapOrMultimapBuiltDto();
            result.setCode(code);
            result.getImports().add(ImportConstants.MAP.getNameAsString());
            result.getImports().add(ImportConstants.MAPS.getNameAsString());
            return result;
        }

        if (chainAnalysis.getReturnClassify() == ReturnClassifyEnum.multiEach) {
            String propertyName = chainAnalysis.getChain().getArgument(0).asFieldAccessExpr().getNameAsString();
            String propertyTypeName = properties.get(propertyName).getJavaType().getSimpleName();
            String elementTypeName = StringUtils.substringAfterLast(resultTransformation.getElementTypeQualifier(),
                    ".");

            boolean isAssignWithoutType = (chainAnalysis.getChain().getParentNode()
                    .orElseThrow(ParentAbsentException::new).getParentNode()
                    .filter(gp -> gp instanceof ExpressionStmt)).isPresent();

            String code;
            if (isAssignWithoutType) {
                code = String.format("%s = ArrayListMultimap.create();", calcResultVarName(chainAnalysis));
            } else {
                code = String.format("final Multimap<%s, %s> %s = ArrayListMultimap.create();", propertyTypeName,
                        elementTypeName, calcResultVarName(chainAnalysis));
            }
            code += "\n" + chainAnalysis.getIndent() + String.format("%sList.forEach(one -> %s.put(one.get%s(), one));",
                    chainAnalysis.getMethodName(), calcResultVarName(chainAnalysis),
                    MoreStringUtils.upperFirstLetter(propertyName));
            MapOrMultimapBuiltDto result = new MapOrMultimapBuiltDto();
            result.setCode(code);
            result.getImports().add(ImportConstants.MULTIMAP.getNameAsString());
            result.getImports().add(ImportConstants.ARRAY_LIST_MULTIMAP.getNameAsString());
            return result;
        }

        return null;
    }

    private String calcResultVarName(ChainAnalysisDto chainAnalysis) {
        String varName = chainAnalysis.getMethodName();
        if (chainAnalysis.getChain().getParentNode().filter(p -> p instanceof AssignExpr).isPresent()) {
            varName = ((AssignExpr) chainAnalysis.getChain().getParentNode().get()).getTarget().toString();
        }
        return varName;
    }

}