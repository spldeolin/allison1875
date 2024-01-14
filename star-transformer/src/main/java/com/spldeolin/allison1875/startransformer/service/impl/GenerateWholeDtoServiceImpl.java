package com.spldeolin.allison1875.startransformer.service.impl;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import com.github.javaparser.StaticJavaParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.enums.FileExistenceResolutionEnum;
import com.spldeolin.allison1875.common.javabean.FieldArg;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.startransformer.StarTransformerConfig;
import com.spldeolin.allison1875.startransformer.javabean.PhraseDto;
import com.spldeolin.allison1875.startransformer.javabean.StarAnalysisDto;
import com.spldeolin.allison1875.startransformer.service.GenerateWholeDtoService;

/**
 * @author Deolin 2023-05-22
 */
@Singleton
public class GenerateWholeDtoServiceImpl implements GenerateWholeDtoService {

    @Inject
    private StarTransformerConfig config;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Override
    public JavabeanGeneration generate(AstForest astForest, StarAnalysisDto analysis) {
        JavabeanArg javabeanArg = new JavabeanArg();
        javabeanArg.setAstForest(astForest);
        javabeanArg.setPackageName(config.getWholeDtoPackge());
        javabeanArg.setClassName(analysis.getWholeDtoName());
        if (config.getEnableLotNoAnnounce()) {
            javabeanArg.setDescription(BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + analysis.getLotNo());
        }
        javabeanArg.setAuthorName(config.getAuthor());
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
        javabeanArg.setMore4Javabean((cu, javabean) -> {
            if (config.getEnableImplementSerializable()) {
                cu.addImport("java.io.Serializable");
                javabean.addImplementedType("Serializable");
                javabean.getMembers().addFirst(StaticJavaParser.parseBodyDeclaration(
                        "private static final long serialVersionUID = " + RandomUtils.nextLong() + "L;"));
            }
        });
        javabeanArg.setJavabeanExistenceResolution(FileExistenceResolutionEnum.RENAME);
        return javabeanGeneratorService.generate(javabeanArg);
    }

    private String entityNameToVarName(String entityName) {
        return MoreStringUtils.lowerFirstLetter(StringUtils.removeEnd(entityName, "Entity"));
    }

}