package com.spldeolin.allison1875.querytransformer.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import static com.spldeolin.allison1875.common.util.StaticJavaParserUtils.parseStatement;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.querytransformer.dto.Binary;
import com.spldeolin.allison1875.querytransformer.dto.ChainAnalysisDTO;
import com.spldeolin.allison1875.querytransformer.dto.ExpandParamRetval;
import com.spldeolin.allison1875.querytransformer.dto.ExpandedFieldDTO;
import com.spldeolin.allison1875.querytransformer.dto.GenerateParamRetval;
import com.spldeolin.allison1875.querytransformer.enums.ReturnStyleEnum;
import com.spldeolin.allison1875.querytransformer.service.TransformMethodCallService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2021-06-09
 */
@Slf4j
@Singleton
public class TransformMethodCallServiceImpl implements TransformMethodCallService {

    @Override
    public String methodCallExpr(String mapperVarName, ChainAnalysisDTO chainAnalysis,
            GenerateParamRetval paramGeneration) {
        String result = mapperVarName + "." + chainAnalysis.getMethodName() + "(";
        if (paramGeneration.getIsParamDTO()) {
            String paramDTOQualifier = paramGeneration.getParameters().get(0).getTypeAsString();
            result += MoreStringUtils.toLowerCamel(MoreStringUtils.splitAndGetLastPart(paramDTOQualifier, "."));
        } else {
            result += chainAnalysis.getBinariesAsArgs().stream().filter(b -> b.getArgument() != null)
                    .map(p -> p.getArgument().toString()).collect(Collectors.joining(", "));
            if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
                result += result.endsWith("(") ? "" : " ,";
                result += chainAnalysis.getOffsetExpr() + ", ";
                result += chainAnalysis.getLimitExpr();
            }
        }
        result += ")";
        log.info("Method Call built [{}]", result);

        return result;
    }

    @Override
    public List<Statement> argumentBuildStmts(ChainAnalysisDTO chainAnalysis, GenerateParamRetval paramGeneration) {
        log.info("build ParamDTO setters call");
        String paramDTOTypeQualifier = paramGeneration.getParameters().get(0).getTypeAsString();
        String paramDTOVarName = MoreStringUtils.toLowerCamel(
                MoreStringUtils.splitAndGetLastPart(paramDTOTypeQualifier, "."));
        List<Statement> result = Lists.newArrayList();
        result.add(parseStatement(
                "final " + paramDTOTypeQualifier + " " + paramDTOVarName + " = new " + paramDTOTypeQualifier + "();"));
        for (Binary binariesAsArg : chainAnalysis.getBinariesAsArgs()) {
            result.add(parseStatement(
                    paramDTOVarName + ".set" + MoreStringUtils.toUpperCamel(binariesAsArg.getVarName()) + "("
                            + binariesAsArg.getArgument() + ");"));
        }
        if (chainAnalysis.getReturnStyle() == ReturnStyleEnum.PAGE) {
            result.add(parseStatement(
                    paramDTOVarName + ".setOffset(" + chainAnalysis.getOffsetExpr() + ");"));
            result.add(parseStatement(
                    paramDTOVarName + ".setLimit(" + chainAnalysis.getLimitExpr() + ");"));
        }
        if (paramGeneration.getExpandParamRetval() != null) {
            for (ExpandedFieldDTO expandedField : paramGeneration.getExpandParamRetval().getExpandedFields()) {
                result.add(parseStatement(
                        paramDTOVarName + ".set" + MoreStringUtils.toUpperCamel(expandedField.getFieldName()) + "("
                                + expandedField.getSourceExpression() + ");"));
            }
        }
        return result;
    }

}