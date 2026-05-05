package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.ast.AstForestContext;
import com.spldeolin.allison1875.common.config.Config;
import com.spldeolin.allison1875.common.constant.BaseConstant;
import com.spldeolin.allison1875.common.dto.DataModelArg;
import com.spldeolin.allison1875.common.dto.DataModelGeneration;
import com.spldeolin.allison1875.common.dto.FieldArg;
import com.spldeolin.allison1875.common.exception.Allison1875Exception;
import com.spldeolin.allison1875.common.service.DataModelService;
import com.spldeolin.allison1875.persistencegenerator.dto.TableAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.persistencegenerator.service.EntityGeneratorService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-18
 */
@Singleton
@Slf4j
public class EntityGeneratorServiceImpl implements EntityGeneratorService {

    @Inject
    private Config config;

    @Inject
    private DataModelService dataModelGeneratorService;

    @Override
    public DataModelGeneration generateEntity(TableAnalysisDTO persistence) {
        DataModelArg arg = new DataModelArg();
        arg.setSourceRoot(AstForestContext.get().getSourceRoot());
        arg.setPackageName(config.getEntityPackage());
        arg.setClassName(persistence.getEntityName());
        arg.setDescription(concatEntityDescription(persistence));
        arg.setAuthor(config.getAuthor());
        arg.setMoreOperation((cu, dataModel) -> {
        });
        for (PropertyDTO property : persistence.getProperties()) {
            FieldArg fieldArg = new FieldArg();
            fieldArg.setDescription(cancatPropertyDescription(property));
            fieldArg.setTypeQualifier(property.getJavaType().getQualifier());
            fieldArg.setFieldName(property.getPropertyName());
            fieldArg.setMoreOperation((cu, fd) -> {
                // 默认值
                buildFieldInitExpr(persistence, property).ifPresent(
                        initExpr -> fd.getVariable(0).setInitializer(initExpr));

            });
            arg.getFieldArgs().add(fieldArg);
        }
        arg.setDataModelExistenceResolution(config.getEntityExistenceResolution());
        return dataModelGeneratorService.generateDataModel(arg);
    }

    private Optional<Expression> buildFieldInitExpr(TableAnalysisDTO tableAnalysis, PropertyDTO property) {
        String defaultValue = property.getDefaultValue();
        if (defaultValue == null) {
            return Optional.empty();
        }
        String typeQualifier = property.getJavaType().getQualifier();
        if (Boolean.class.getName().equals(typeQualifier)) {
            return Optional.of(new BooleanLiteralExpr(BooleanUtils.toBoolean(defaultValue)));
        }
        if (String.class.getName().equals(typeQualifier)) {
            return Optional.of(new StringLiteralExpr(defaultValue.replaceAll("^'|'$", "")));
        }
        if (Byte.class.getName().equals(typeQualifier) || Integer.class.getName().equals(typeQualifier)) {
            return Optional.of(new IntegerLiteralExpr(defaultValue));
        }
        if (Long.class.getName().equals(typeQualifier)) {
            return Optional.of(new LongLiteralExpr(defaultValue));
        }
        if (Date.class.getName().equals(typeQualifier)) {
            if (defaultValue.equals("CURRENT_TIMESTAMP")) {
                return Optional.of(new ObjectCreationExpr().setType("Date"));
            } else {
                throw new Allison1875Exception("暂不支持非CURRENT_TIMESTAMP的默认值");
            }
        }
        if (BigDecimal.class.getName().equals(typeQualifier)) {
            return Optional.of(new ObjectCreationExpr().setType("BigDecimal").addArgument(defaultValue));
        }

        return Optional.empty();
    }

    private String concatEntityDescription(TableAnalysisDTO persistence) {
        String result = persistence.getDescrption() + BaseConstant.JAVA_DOC_NEW_LINE + persistence.getTableName();
        if (config.getEnableNoModifyAnnounce()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE;
            result += BaseConstant.JAVA_DOC_NEW_LINE + BaseConstant.NO_MODIFY_ANNOUNCE;
        }
        return result;
    }

    private String cancatPropertyDescription(PropertyDTO property) {
        String result = property.getDescription();
        result += BaseConstant.JAVA_DOC_NEW_LINE + property.getColumnName();
        if (property.getLength() != null && property.getLength() != 0) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + "长度：" + property.getLength();
        }
        if (property.getNotnull()) {
            result += BaseConstant.JAVA_DOC_NEW_LINE + "不能为null";
        }
        if (property.getDefaultValue() != null) {
            String defaultV = property.getDefaultValue();
            if (!"CURRENT_TIMESTAMP".equals(defaultV)) {
                defaultV = "'" + defaultV + "'";
            }
            result += BaseConstant.JAVA_DOC_NEW_LINE + "默认：" + defaultV;
        }
        return result;
    }

}