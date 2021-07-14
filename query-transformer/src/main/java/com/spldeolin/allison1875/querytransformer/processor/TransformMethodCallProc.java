package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Set;
import java.util.stream.Collectors;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
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
            result += MoreStringUtils
                    .lowerFirstLetter(parameterTransformation.getParameters().get(0).getTypeAsString());
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
            if (byPhrase.getPredicate() == PredicateEnum.IS_NULL || byPhrase.getPredicate() == PredicateEnum.NOT_NULL) {
                continue;
            }
            result.append("\n").append(chainAnalysis.getIndent()).append(javabeanVarName).append(".set")
                    .append(MoreStringUtils.upperFirstLetter(byPhrase.getVarName())).append("(")
                    .append(byPhrase.getObjectExpr()).append(");");
        }
        return result.toString();
    }

}