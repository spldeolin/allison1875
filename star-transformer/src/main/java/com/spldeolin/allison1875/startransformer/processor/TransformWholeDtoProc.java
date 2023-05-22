package com.spldeolin.allison1875.startransformer.processor;

import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.ast.CompilationUnit;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.factory.JavabeanFactory;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.startransformer.StarTransformerConfig;
import com.spldeolin.allison1875.startransformer.javabean.ChainAnalysisDto;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDto;

/**
 * @author Deolin 2023-05-22
 */
@Singleton
public class TransformWholeDtoProc {

    @Inject
    private StarTransformerConfig config;

    public CompilationUnit transformWholeDto(JavabeanArg javabeanArg, AstForest astForest, ChainAnalysisDto analysis) {
        javabeanArg.setAstForest(astForest);
        javabeanArg.setPackageName(config.getWholeDtoPackge());
        javabeanArg.setDescription("");
        javabeanArg.setClassName(analysis.getWholeDtoName());
        FieldArg cftFieldArg = new FieldArg();
        cftFieldArg.setTypeQualifier(analysis.getCftEntityQualifier());
        cftFieldArg.setTypeName(analysis.getCftEntityName());
        cftFieldArg.setFieldName(this.entityNameToVarName(analysis.getCftEntityName()));
        javabeanArg.getFieldArgs().add(cftFieldArg);
        for (PhraseDto phrase : analysis.getPhrases()) {
            FieldArg dtFieldArg = new FieldArg();
            dtFieldArg.setTypeQualifier(phrase.getDtEntityQualifier());
            if (phrase.getIsOneToOne()) {
                dtFieldArg.setTypeName(phrase.getDtEntityName());
                dtFieldArg.setFieldName(this.entityNameToVarName(phrase.getDtEntityName()));
            } else {
                dtFieldArg.setTypeName("List<" + phrase.getDtEntityName() + ">");
                dtFieldArg.setFieldName(English.plural(this.entityNameToVarName(phrase.getDtEntityName())));
            }
            javabeanArg.getFieldArgs().add(dtFieldArg);
            if (CollectionUtils.isNotEmpty(phrase.getKeys()) || CollectionUtils.isNotEmpty(phrase.getMkeys())) {
                for (String key : phrase.getKeys()) {
                    FieldArg keyFieldArg = new FieldArg();
                    keyFieldArg.setTypeName(
                            "Map<" + phrase.getEntityFieldTypesEachFieldName().get(key) + "," + phrase.getDtEntityName()
                                    + ">");
                    keyFieldArg.setFieldName(English.plural(this.entityNameToVarName(phrase.getDtEntityName())) + "Each"
                            + StringUtils.capitalize(key));
                    javabeanArg.getFieldArgs().add(keyFieldArg);
                }
                for (String mkey : phrase.getMkeys()) {
                    FieldArg mkeyFieldArg = new FieldArg();
                    mkeyFieldArg.setTypeName("Multimap<" + phrase.getEntityFieldTypesEachFieldName().get(mkey) + ","
                            + phrase.getDtEntityName() + ">");
                    mkeyFieldArg.setFieldName(
                            English.plural(this.entityNameToVarName(phrase.getDtEntityName())) + "Each"
                                    + StringUtils.capitalize(mkey));
                    javabeanArg.getFieldArgs().add(mkeyFieldArg);
                }
            }
        }
        return JavabeanFactory.buildCu(javabeanArg);
    }

    private String entityNameToVarName(String entityName) {
        return MoreStringUtils.lowerFirstLetter(StringUtils.removeEnd(entityName, "Entity"));
    }

}