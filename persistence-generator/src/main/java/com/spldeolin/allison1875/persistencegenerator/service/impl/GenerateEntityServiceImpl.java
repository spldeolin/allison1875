package com.spldeolin.allison1875.persistencegenerator.service.impl;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.constant.AnnotationConstant;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.javabean.FieldArg;
import com.spldeolin.allison1875.common.javabean.JavabeanArg;
import com.spldeolin.allison1875.common.javabean.JavabeanGeneration;
import com.spldeolin.allison1875.common.service.JavabeanGeneratorService;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.GenerateEntityService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
@Slf4j
public class GenerateEntityServiceImpl implements GenerateEntityService {

    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private JavabeanGeneratorService javabeanGeneratorService;

    @Override
    public JavabeanGeneration generate(PersistenceDto persistence, AstForest astForest) {
        JavabeanArg arg = new JavabeanArg();
        arg.setAstForest(astForest);
        arg.setPackageName(config.getEntityPackage());
        arg.setClassName(persistence.getEntityName());
        arg.setDescription(concatEntityDescription(persistence));
        arg.setAuthorName(config.getAuthor());
        arg.setMore4Javabean((cu, javabean) -> {
            // 追加父类，并追加EqualsAndHashCode注解（如果需要的话）
            String superEntityQualifier = config.getSuperEntityQualifier();
            if (StringUtils.isNotEmpty(superEntityQualifier)) {
                cu.addImport(superEntityQualifier);
                String superEntityName = superEntityQualifier.substring(superEntityQualifier.lastIndexOf('.') + 1);
                javabean.addExtendedType(superEntityName);
                javabean.addAnnotation(AnnotationConstant.EQUALS_AND_HASH_CODE);
                javabean.getAnnotations().removeIf(anno -> anno.getNameAsString().equals("Accessors"));
            }
            if (config.getEnableEntityImplementSerializable()) {
                cu.addImport("java.io.Serializable");
                javabean.addImplementedType("Serializable");
                javabean.getMembers().addFirst(StaticJavaParser.parseBodyDeclaration(
                        "private static final long serialVersionUID = " + RandomUtils.nextLong() + "L;"));
            }
            if (config.getEnableEntityImplementCloneable()) {
                javabean.addImplementedType("Cloneable");
                javabean.getMembers().addLast(StaticJavaParser.parseBodyDeclaration(
                        "@Override public Object clone() throws CloneNotSupportedException { return super.clone(); }"));
            }
        });
        for (PropertyDto property : persistence.getProperties()) {
            if (config.getHiddenColumns().contains(property.getColumnName()) || config.getAlreadyInSuperEntity()
                    .contains(property.getColumnName())) {
                continue;
            }
            FieldArg fieldArg = new FieldArg();
            fieldArg.setTypeQualifier(property.getJavaType().getQualifier());
            fieldArg.setDescription(cancatPropertyDescription(property));
            fieldArg.setTypeName(property.getJavaType().getSimpleName());
            fieldArg.setFieldName(property.getPropertyName());
            arg.getFieldArgs().add(fieldArg);
        }
        arg.setJavabeanExistenceResolution(config.getEntityExistenceResolution());
        return javabeanGeneratorService.generate(arg);
    }

    private String concatEntityDescription(PersistenceDto persistence) {
        String result = persistence.getDescrption() + BaseConstant.JAVA_DOC_NEW_LINE + persistence.getTableName();
        if (config.getEnableNoModifyAnnounce() || config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE;
        }
        if (config.getEnableNoModifyAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        if (config.getEnableLotNoAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.LOT_NO_ANNOUNCE_PREFIXION + persistence.getLotNo();
        }
        return result;
    }

    private String cancatPropertyDescription(PropertyDto property) {
        String result = property.getDescription();
        result += BaseConstant.JAVA_DOC_NEW_LINE + property.getColumnName();
        if (property.getLength() != null && property.getLength() != 0) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + "长度：" + property.getLength();
        }
        if (property.getNotnull()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + "不能为null";
        }
        if (property.getDefaultV() != null) {
            String defaultV = property.getDefaultV();
            if (!"CURRENT_TIMESTAMP".equals(defaultV)) {
                defaultV = "'" + defaultV + "'";
            }
            result += BaseConstant.JAVA_DOC_NEW_LINE + "默认：" + defaultV;
        }
        return result;
    }

}