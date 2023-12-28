package com.spldeolin.allison1875.persistencegenerator.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.constant.AnnotationConstant;
import com.spldeolin.allison1875.base.constant.BaseConstant;
import com.spldeolin.allison1875.base.exception.QualifierAbsentException;
import com.spldeolin.allison1875.base.factory.JavabeanFactory;
import com.spldeolin.allison1875.base.factory.javabean.FieldArg;
import com.spldeolin.allison1875.base.factory.javabean.JavabeanArg;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.EntityGeneration;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.GenerateEntityService;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
@Log4j2
public class GenerateEntityServiceImpl implements GenerateEntityService {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    @Override
    public EntityGeneration process(PersistenceDto persistence, AstForest astForest) {
        JavabeanArg arg = new JavabeanArg();
        arg.setAstForest(astForest);
        arg.setPackageName(persistenceGeneratorConfig.getEntityPackage());
        arg.setClassName(persistence.getEntityName());
        arg.setDescription(concatEntityDescription(persistence));
        arg.setAuthorName(persistenceGeneratorConfig.getAuthor());
        arg.setLotNo(persistence.getLotNo());
        arg.setMore4Javabean((cu, javabean) -> {
            // 追加父类，并追加EqualsAndHashCode注解（如果需要的话）
            String superEntityQualifier = persistenceGeneratorConfig.getSuperEntityQualifier();
            if (StringUtils.isNotEmpty(superEntityQualifier)) {
                cu.addImport(superEntityQualifier);
                cu.addImport(AnnotationConstant.EQUALS_AND_HASH_CODE_QUALIFIER);
                cu.getImports().removeIf(ipt -> ipt.getNameAsString().equals(AnnotationConstant.ACCESSORS_QUALIFIER));
                String superEntityName = superEntityQualifier.substring(superEntityQualifier.lastIndexOf('.') + 1);
                javabean.addExtendedType(superEntityName);
                javabean.addAnnotation(AnnotationConstant.EQUALS_AND_HASH_CODE);
                javabean.getAnnotations().removeIf(anno -> anno.getNameAsString().equals("Accessors"));
            }
            if (BooleanUtils.isTrue(persistenceGeneratorConfig.getEnableEntityImplementSerializable())) {
                cu.addImport("java.io.Serializable");
                javabean.addImplementedType("Serializable");
                javabean.getMembers().addFirst(StaticJavaParser.parseBodyDeclaration(
                        "private static final long serialVersionUID = " + RandomUtils.nextLong() + "L;"));
            }
            if (BooleanUtils.isTrue(persistenceGeneratorConfig.getEnableEntityImplementCloneable())) {
                javabean.addImplementedType("Cloneable");
                javabean.getMembers().addLast(StaticJavaParser.parseBodyDeclaration(
                        "@Override public Object clone() throws CloneNotSupportedException { return super.clone(); }"));
            }
        });
        for (PropertyDto property : persistence.getProperties()) {
            if (persistenceGeneratorConfig.getHiddenColumns().contains(property.getColumnName())
                    || persistenceGeneratorConfig.getAlreadyInSuperEntity().contains(property.getColumnName())) {
                continue;
            }
            FieldArg fieldArg = new FieldArg();
            fieldArg.setTypeQualifier(property.getJavaType().getQualifier());
            fieldArg.setDescription(cancatPropertyDescription(property));
            fieldArg.setTypeName(property.getJavaType().getSimpleName());
            fieldArg.setFieldName(property.getPropertyName());
            arg.getFieldArgs().add(fieldArg);
        }
        CompilationUnit cu = JavabeanFactory.buildCu(arg);
        if (cu == null) {
            return new EntityGeneration().setSameNameAndLotNoPresent(true);
        }


        EntityGeneration result = new EntityGeneration();
        result.setJavabeanArg(arg);
        result.setEntity(cu.getPrimaryType().orElseThrow(RuntimeException::new).asClassOrInterfaceDeclaration());
        result.setEntityName(arg.getClassName());
        result.setEntityQualifier(cu.getPrimaryType().orElseThrow(QualifierAbsentException::new).getFullyQualifiedName()
                .orElseThrow(QualifierAbsentException::new));
        result.setEntityCu(cu);
        return result;
    }

    private String concatEntityDescription(PersistenceDto persistence) {
        return persistence.getDescrption() + BaseConstant.NEW_LINE + "<p>" + persistence.getTableName()
                + Strings.repeat(BaseConstant.NEW_LINE, 2) + persistence.getLotNo().asJavadocDescription();
    }

    private String cancatPropertyDescription(PropertyDto property) {
        String result = property.getDescription();
        result += BaseConstant.NEW_LINE + "<p>" + property.getColumnName();
        if (property.getLength() != null && property.getLength() != 0) {
            result += BaseConstant.NEW_LINE + "<p>长度：" + property.getLength();
        }
        if (property.getNotnull()) {
            result += BaseConstant.NEW_LINE + "<p>不能为null";
        }
        if (property.getDefaultV() != null) {
            String defaultV = property.getDefaultV();
            if (!"CURRENT_TIMESTAMP".equals(defaultV)) {
                defaultV = "'" + defaultV + "'";
            }
            result += BaseConstant.NEW_LINE + "<p>默认：" + defaultV;
        }
        return result;
    }

}