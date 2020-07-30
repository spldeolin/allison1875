package com.spldeolin.allison1875.persistencegenerator.processor;

import java.util.Collection;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spldeolin.allison1875.base.util.StringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.enums.JdbcTypeEnum;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PropertyDto;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-12
 */
@Log4j2
public class BuildPersistenceDtoProcessor {

    @Getter
    private Collection<PersistenceDto> persistences;

    public BuildPersistenceDtoProcessor process() {
        // 查询information_schema.COLUMNS、information_schema.TABLES表
        Collection<InformationSchemaDto> infoSchemas = new QueryInformationSchemaProcessor().process().getInfoSchemas();

        Map<String, PersistenceDto> persistences = Maps.newHashMap();
        for (InformationSchemaDto infoSchema : infoSchemas) {
            PersistenceDto dto = new PersistenceDto();
            String domainName = StringUtils.underscoreToUpperCamel(infoSchema.getTableName());
            dto.setTableName(infoSchema.getTableName());
            dto.setEntityName(domainName + endWith());
            dto.setMapperName(domainName + "Mapper");
            dto.setDescrption(infoSchema.getTableComment());
            dto.setPkProperties(Lists.newArrayList());
            dto.setNonPkProperties(Lists.newArrayList());
            dto.setProperties(Lists.newArrayList());
            persistences.put(infoSchema.getTableName(), dto);
        }
        for (InformationSchemaDto columnMeta : infoSchemas) {
            PropertyDto property = new PropertyDto();
            property.setColumnName(columnMeta.getColumnName());
            property.setPropertyName(StringUtils.underscoreToLowerCamel(columnMeta.getColumnName()));
            JdbcTypeEnum jdbcTypeEnum = calcJavaType(columnMeta);
            if (jdbcTypeEnum == null) {
                continue;
            }
            Class<?> javaType = jdbcTypeEnum.getJavaType();
            property.setJavaType(javaType);
            property.setDescription(columnMeta.getColumnComment());
            PersistenceDto persistence = persistences.get(columnMeta.getTableName());

            persistence.getProperties().add(property);
            if ("PRI".equalsIgnoreCase(columnMeta.getColumnKey())) {
                persistence.getPkProperties().add(property);
            } else {
                persistence.getNonPkProperties().add(property);
            }
        }
        this.persistences = persistences.values();
        return this;
    }

    private String endWith() {
        return PersistenceGeneratorConfig.getInstace().getIsEntityUsingAlias() ? "Entity" : "";
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