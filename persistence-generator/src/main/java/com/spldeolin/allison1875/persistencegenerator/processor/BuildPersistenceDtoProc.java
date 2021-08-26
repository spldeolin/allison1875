package com.spldeolin.allison1875.persistencegenerator.processor;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.LotNo;
import com.spldeolin.allison1875.base.LotNo.ModuleAbbr;
import com.spldeolin.allison1875.base.ast.AstForest;
import com.spldeolin.allison1875.base.util.JsonUtils;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.handle.CommentHandle;
import com.spldeolin.allison1875.persistencegenerator.handle.JdbcTypeHandle;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-12
 */
@Singleton
@Log4j2
public class BuildPersistenceDtoProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    @Inject
    private QueryInformationSchemaProc queryInformationSchemaProc;

    @Inject
    private JdbcTypeHandle jdbcTypeHandle;

    @Inject
    private CommentHandle commentHandle;

    public Collection<PersistenceDto> process(AstForest astForest) {
        // 查询information_schema.COLUMNS、information_schema.TABLES表
        Collection<InformationSchemaDto> infoSchemas = queryInformationSchemaProc.process();
        String deleteFlag = getDeleteFlagName();

        Map<String, PersistenceDto> persistences = Maps.newHashMap();
        for (InformationSchemaDto infoSchema : infoSchemas) {
            PersistenceDto dto = new PersistenceDto();
            String domainName = MoreStringUtils.underscoreToUpperCamel(infoSchema.getTableName());
            dto.setTableName(infoSchema.getTableName());
            dto.setEntityName(domainName + endWith());
            dto.setMapperName(domainName + "Mapper");
            dto.setDescrption(commentHandle.resolveTableComment(infoSchema));
            dto.setIdProperties(Lists.newArrayList());
            dto.setNonIdProperties(Lists.newArrayList());
            dto.setKeyProperties(Lists.newArrayList());
            dto.setProperties(Lists.newArrayList());
            dto.setLotNo(LotNo.build(ModuleAbbr.PG, JsonUtils.toJson(dto), true));
            persistences.put(infoSchema.getTableName(), dto);
        }
        for (InformationSchemaDto infoSchema : infoSchemas) {
            String columnName = infoSchema.getColumnName();
            if (persistenceGeneratorConfig.getHiddenColumns().contains(columnName)) {
                continue;
            }
            PropertyDto property = new PropertyDto();
            property.setColumnName(columnName);
            property.setPropertyName(MoreStringUtils.underscoreToLowerCamel(columnName));
            JavaTypeNamingDto javaType = jdbcTypeHandle.jdbcType2javaType(infoSchema, astForest);
            if (javaType == null) {
                log.warn("出现了预想外的类型 columnName={} dataType={} columnType={}", infoSchema.getColumnName(),
                        infoSchema.getDataType(), infoSchema.getColumnType());
                continue;
            }
            property.setJavaType(javaType);
            property.setDescription(commentHandle.resolveColumnComment(infoSchema));
            property.setLength(infoSchema.getCharacterMaximumLength());
            property.setNotnull("NO".equals(infoSchema.getIsNullable()));
            property.setDefaultV(infoSchema.getColumnDefault());
            PersistenceDto persistence = persistences.get(infoSchema.getTableName());

            persistence.getProperties().add(property);
            if ("PRI".equalsIgnoreCase(infoSchema.getColumnKey())) {
                persistence.getIdProperties().add(property);
            } else {
                persistence.getNonIdProperties().add(property);
                if (columnName.endsWith("_id") && !persistenceGeneratorConfig.getNotKeyColumns().contains(columnName)) {
                    persistence.getKeyProperties().add(property);
                }
            }

            if (columnName.equals(deleteFlag)) {
                persistence.setIsDeleteFlagExist(true);
            }
        }
        persistences.values().stream().filter(dto -> !dto.getIsDeleteFlagExist())
                .forEach(dto -> log.info("数据表[{}]没有逻辑删除标识符[{}]", dto.getTableName(), deleteFlag));
        return persistences.values();
    }

    private String getDeleteFlagName() {
        String sql = persistenceGeneratorConfig.getNotDeletedSql();
        if (StringUtils.isEmpty(sql) || StringUtils.isEmpty(persistenceGeneratorConfig.getDeletedSql())) {
            return null;
        }
        if (!sql.contains("=")) {
            log.warn("notDeletedSql非法，不是等式，认为没有删除标识符 [{}]", sql);
            return null;
        }
        return sql.split("=")[0].trim();
    }

    private String endWith() {
        return persistenceGeneratorConfig.getIsEntityEndWithEntity() ? "Entity" : "";
    }

}