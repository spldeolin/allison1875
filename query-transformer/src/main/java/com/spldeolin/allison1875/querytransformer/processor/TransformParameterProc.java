package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.factory.JavabeanFactory;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.base.util.ast.Saves;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.DesignMeta;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.querytransformer.QueryTransformerConfig;
import com.spldeolin.allison1875.querytransformer.enums.PredicateEnum;
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

    @Nullable
    public ParameterTransformationDto transform(ChainAnalysisDto chainAnalysis, DesignMeta designMeta,
            AstForest astForest) {
        Map<String, PropertyDto> properties = designMeta.getProperties();

        List<String> imports = Lists.newArrayList();
        List<Parameter> params = Lists.newArrayList();
        boolean isJavabean = false;

        Set<PhraseDto> phrases = Sets.newHashSet(chainAnalysis.getUpdatePhrases());
        phrases.addAll(chainAnalysis.getByPhrases());
        log.info("phrases.size()={}", phrases.size());
        if (phrases.size() > 3) {
            JavabeanArg javabeanArg = new JavabeanArg();
            javabeanArg.setAstForest(astForest);
            javabeanArg.setPackageName(config.getMapperConditionQualifier());
            javabeanArg.setClassName(MoreStringUtils.upperFirstLetter(chainAnalysis.getMethodName()) + "Cond");
            List<String> varNames = Lists.newArrayList();
            for (PhraseDto phrase : phrases) {
                if (phrase.getPredicate() == PredicateEnum.IS_NULL || phrase.getPredicate() == PredicateEnum.NOT_NULL) {
                    continue;
                }
                String propertyName = phrase.getSubjectPropertyName();
                String varName = sureNotToRepeat(propertyName, varNames, 1);
                JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
                FieldArg fieldArg = new FieldArg();
                fieldArg.setTypeQualifier(javaType.getQualifier());
                fieldArg.setDescription(properties.get(propertyName).getDescription());
                fieldArg.setTypeName(javaType.getSimpleName());
                fieldArg.setFieldName(varName);
                javabeanArg.getFieldArgs().add(fieldArg);
            }
            CompilationUnit cu = JavabeanFactory.buildCu(javabeanArg);
            Saves.add(cu);
            TypeDeclaration<?> cond = cu.getPrimaryType().orElseThrow(RuntimeException::new);
            Parameter param = new Parameter();
            param.setType(cond.getNameAsString());
            param.setName(MoreStringUtils.lowerFirstLetter(cond.getNameAsString()));
            params.add(param);
            imports.add(cond.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
            isJavabean = true;
        } else if (phrases.size() > 0) {
            List<String> varNames = Lists.newArrayList();
            for (PhraseDto phrase : phrases) {
                String propertyName = phrase.getSubjectPropertyName();
                String varName = sureNotToRepeat(propertyName, varNames, 1);
                JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
                Parameter param = new Parameter();
                param.addAnnotation(StaticJavaParser.parseAnnotation(String.format("@Param(\"%s\")", varName)));
                param.setType(javaType.getSimpleName());
                param.setName(varName);
                imports.add(javaType.getQualifier());
                params.add(param);
            }
        } else {
            return null;
        }

        return new ParameterTransformationDto().setImports(imports).setParameters(params).setIsJavabean(isJavabean);
    }

    private String sureNotToRepeat(String name, List<String> names, int index) {
        if (!names.contains(name)) {
            names.add(name);
            return name;
        }
        index++;
        return this.sureNotToRepeat(name + index, names, index);
    }

}