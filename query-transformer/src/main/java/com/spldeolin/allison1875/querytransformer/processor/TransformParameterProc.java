package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import java.util.Set;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.ParameterTransformationDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-01
 */
@Singleton
@Log4j2
public class TransformParameterProc {

    @Inject
    private QueryTransformerConfig config;

    public ParameterTransformationDto transform(ChainAnalysisDto chainAnalysis, DesignMeta designMeta,
            ClassOrInterfaceDeclaration queryChainCoid) {
        List<Parameter> params = Lists.newArrayList();
        Set<PhraseDto> phrases = chainAnalysis.getByPhrases();
        phrases.addAll(chainAnalysis.getUpdatePhrases());
        if (phrases.size() > 3) {

        } else {
            List<String> varNames = Lists.newArrayList();
            for (PhraseDto phrase : phrases) {
                Parameter param = new Parameter();
                String subjectPropertyName = phrase.getSubjectPropertyName();


//                FieldDeclaration field = queryChainCoid.getFieldByName(subjectPropertyName).orElse();
//                param.addAnnotation(StaticJavaParser.parseAnnotation(String.format("@Param(\"%s\")", designMeta
//                .get)));
            }

        }

        return new ParameterTransformationDto();
    }

}