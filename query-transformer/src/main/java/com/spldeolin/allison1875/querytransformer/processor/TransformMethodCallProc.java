package com.spldeolin.allison1875.querytransformer.processor;

import java.util.Set;
import java.util.stream.Collectors;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
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

    public String process(DesignMeta designMeta, ChainAnalysisDto chainAnalysis,
            ParameterTransformationDto parameterTransformation, ResultTransformationDto resultTransformation) {
        String result = "";
        if (!resultTransformation.getIsEntityOrRecord()) {
            result += resultTransformation.getResultType() + " " + chainAnalysis.getMethodName() + " = ";
        }

        result += MoreStringUtils.lowerFirstLetter(designMeta.getMapperName()) + "." + chainAnalysis.getMethodName()
                + "(";

        if (parameterTransformation.getIsJavabean()) {
            result += MoreStringUtils
                    .lowerFirstLetter(parameterTransformation.getParameters().get(0).getTypeAsString());
        } else {
            Set<PhraseDto> phrases = chainAnalysis.getUpdatePhrases();
            phrases.addAll(chainAnalysis.getByPhrases());
            result += phrases.stream().map(p -> p.getObjectExpr().toString()).collect(Collectors.joining(", "));
        }
        result += ")";

        return result;
    }

    public String argumentBuild(ChainAnalysisDto chainAnalysis, ParameterTransformationDto parameterTransformation) {
        if (!parameterTransformation.getIsJavabean()) {
            return null;
        }
        String javabeanTypeName = parameterTransformation.getParameters().get(0).getTypeAsString();
        String javabeanVarName = MoreStringUtils.lowerFirstLetter(javabeanTypeName);
        StringBuilder result = new StringBuilder(
                String.format("%s %s = new %s();", javabeanTypeName, javabeanVarName, javabeanTypeName));
        for (PhraseDto updatePhrase : chainAnalysis.getUpdatePhrases()) {
            result.append("\n").append(javabeanVarName).append(".set")
                    .append(MoreStringUtils.upperFirstLetter(updatePhrase.getSubjectPropertyName())).append("(")
                    .append(updatePhrase.getObjectExpr()).append(");");
        }
        for (PhraseDto byPhrase : chainAnalysis.getByPhrases()) {
            result.append("\n").append(javabeanVarName).append(".set")
                    .append(MoreStringUtils.upperFirstLetter(byPhrase.getSubjectPropertyName())).append("(")
                    .append(byPhrase.getObjectExpr()).append(");");
        }
        return result.toString();
    }

}