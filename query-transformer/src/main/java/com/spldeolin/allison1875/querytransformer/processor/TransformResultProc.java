package com.spldeolin.allison1875.querytransformer.processor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import com.spldeolin.allison1875.querytransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.querytransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.querytransformer.javabean.ResultTransformationDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2021-06-01
 */
@Singleton
@Log4j2
public class TransformResultProc {

    @Inject
    private QueryTransformerConfig config;

    public ResultTransformationDto transform(ChainAnalysisDto chainAnalysis, DesignMeta designMeta,
            AstForest astForest) {
        if (chainAnalysis.getChain().getParentNode().isPresent()) {
            if (chainAnalysis.getChain().getParentNode().get().getParentNode()
                    .filter(parent -> parent instanceof VariableDeclarationExpr).isPresent()) {
                VariableDeclarationExpr vde = (VariableDeclarationExpr) chainAnalysis.getChain().getParentNode().get()
                        .getParentNode().get();
                Type vdeType = vde.getCommonType();
                return new ResultTransformationDto().setResultType(vdeType).setIsSpecifiedEntity(true);
            }
        }

        if (!chainAnalysis.isQueryOrUpdate()) {
            return new ResultTransformationDto().setResultType(PrimitiveType.intType()).setIsSpecifiedEntity(false);
        }

        Map<String, PropertyDto> properties = designMeta.getProperties();

        Set<PhraseDto> phrases = chainAnalysis.getQueryPhrases();
        log.info("queryPhrases.size()={}", chainAnalysis.getQueryPhrases().size());
        if (phrases.size() > 1) {
            // 指定了2个及以上属性，生成一个Javabean作为返回值类型
            JavabeanArg javabeanArg = new JavabeanArg();
            javabeanArg.setAstForest(astForest);
            javabeanArg.setPackageName(config.getMapperRecordQualifier());
            javabeanArg.setClassName(MoreStringUtils.upperFirstLetter(chainAnalysis.getMethodName()) + "Record");
            List<String> varNames = Lists.newArrayList();
            for (PhraseDto phrase : phrases) {
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
            TypeDeclaration<?> resultType = cu.getPrimaryType().orElseThrow(RuntimeException::new);
            ResultTransformationDto result = new ResultTransformationDto();
            result.setOneImport(resultType.getFullyQualifiedName().orElseThrow(QualifierAbsentException::new));
            if (chainAnalysis.isReturnManyOrOne()) {
                result.setResultType(StaticJavaParser.parseType("List<" + resultType.getNameAsString() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(resultType.getNameAsString()));
            }
            result.setIsSpecifiedEntity(false);
            return result;

        } else if (phrases.size() == 1) {
            // 指定了1个属性，使用该属性类型作为返回值类型
            String propertyName = Iterables.getOnlyElement(phrases).getSubjectPropertyName();
            JavaTypeNamingDto javaType = properties.get(propertyName).getJavaType();
            ResultTransformationDto result = new ResultTransformationDto();
            result.setOneImport(javaType.getQualifier());
            if (chainAnalysis.isReturnManyOrOne()) {
                result.setResultType(StaticJavaParser.parseType("List<" + javaType.getSimpleName() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(javaType.getSimpleName()));
            }
            result.setIsSpecifiedEntity(false);
            return result;

        } else {
            // 没有指定属性，使用Entity作为返回值类型
            ResultTransformationDto result = new ResultTransformationDto();
            result.setOneImport(designMeta.getEntityQualifier());
            if (chainAnalysis.isReturnManyOrOne()) {
                result.setResultType(StaticJavaParser.parseType("List<" + designMeta.getEntityName() + ">"));
            } else {
                result.setResultType(StaticJavaParser.parseType(designMeta.getEntityName()));
            }
            result.setIsSpecifiedEntity(false);
            return result;
        }
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