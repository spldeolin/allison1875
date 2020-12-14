package com.spldeolin.allison1875.persistencegenerator.processor;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.enums.JdbcTypeEnum;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
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

    public Collection<PersistenceDto> process() {
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
            dto.setDescrption(infoSchema.getTableComment());
            dto.setIdProperties(Lists.newArrayList());
            dto.setNonIdProperties(Lists.newArrayList());
            dto.setKeyProperties(Lists.newArrayList());
            dto.setProperties(Lists.newArrayList());
            persistences.put(infoSchema.getTableName(), dto);
        }
        for (InformationSchemaDto columnMeta : infoSchemas) {
            String columnName = columnMeta.getColumnName();
            if (persistenceGeneratorConfig.getHiddenColumns().contains(columnName)) {
                continue;
            }
            PropertyDto property = new PropertyDto();
            property.setColumnName(columnName);
            property.setPropertyName(MoreStringUtils.underscoreToLowerCamel(columnName));
            JdbcTypeEnum jdbcTypeEnum = calcJavaType(columnMeta);
            if (jdbcTypeEnum == null) {
                continue;
            }
            Class<?> javaType = jdbcTypeEnum.getJavaType();
            property.setJavaType(javaType);
            property.setDescription(columnMeta.getColumnComment());
            property.setLength(columnMeta.getCharacterMaximumLength());
            property.setNotnull("NO".equals(columnMeta.getIsNullable()));
            property.setDefaultV(columnMeta.getColumnDefault());
            PersistenceDto persistence = persistences.get(columnMeta.getTableName());

            persistence.getProperties().add(property);
            if ("PRI".equalsIgnoreCase(columnMeta.getColumnKey())) {
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

    private static JdbcTypeEnum calcJavaType(InformationSchemaDto columnMeta) {
        JdbcTypeEnum jdbcTypeEnum = JdbcTypeEnum.likeColumnType(columnMeta.getColumnType());
        if (jdbcTypeEnum == null) {
            jdbcTypeEnum = JdbcTypeEnum.ofDataType(columnMeta.getDataType());
        }
        if (jdbcTypeEnum == null) {
            log.warn("出现了预想外的类型 columnName={} dataType={} columnType={}", columnMeta.getColumnName(),
                    columnMeta.getDataType(), columnMeta.getColumnType());
            return null;
        }
        return jdbcTypeEnum;
    }

}