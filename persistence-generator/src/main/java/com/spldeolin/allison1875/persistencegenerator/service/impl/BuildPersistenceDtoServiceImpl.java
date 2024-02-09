package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.ast.AstForest;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.JavaTypeNamingDto;
import com.spldeolin.allison1875.persistencegenerator.facade.javabean.PropertyDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.javabean.PersistenceDto;
import com.spldeolin.allison1875.persistencegenerator.service.BuildPersistenceDtoService;
import com.spldeolin.allison1875.persistencegenerator.service.CommentService;
import com.spldeolin.allison1875.persistencegenerator.service.JdbcTypeService;
import com.spldeolin.allison1875.persistencegenerator.service.QueryInformationSchemaService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-12
 */
@Singleton
@Slf4j
public class BuildPersistenceDtoServiceImpl implements BuildPersistenceDtoService {

    @Inject
    private PersistenceGeneratorConfig config;

    @Inject
    private QueryInformationSchemaService queryInformationSchemaService;

    @Inject
    private JdbcTypeService jdbcTypeService;

    @Inject
    private CommentService commentService;

    @Override
    public List<PersistenceDto> build(AstForest astForest) {
        // 查询information_schema.COLUMNS、information_schema.TABLES表
        List<InformationSchemaDto> infoSchemas = queryInformationSchemaService.query();
        String deleteFlag = getDeleteFlagName();

        Map<String, PersistenceDto> persistences = Maps.newHashMap();
        for (InformationSchemaDto infoSchema : infoSchemas) {
            PersistenceDto dto = new PersistenceDto();
            String domainName = MoreStringUtils.underscoreToUpperCamel(infoSchema.getTableName());
            dto.setTableName(infoSchema.getTableName());
            dto.setEntityName(domainName + endWith());
            dto.setMapperName(domainName + "Mapper");
            dto.setDescrption(commentService.resolveTableComment(infoSchema));
            dto.setIdProperties(Lists.newArrayList());
            dto.setNonIdProperties(Lists.newArrayList());
            dto.setKeyProperties(Lists.newArrayList());
            dto.setProperties(Lists.newArrayList());
            persistences.put(infoSchema.getTableName(), dto);
        }
        for (InformationSchemaDto infoSchema : infoSchemas) {
            PersistenceDto persistenceDto = persistences.get(infoSchema.getTableName());
            String columnName = infoSchema.getColumnName();
            if (config.getHiddenColumns().contains(columnName)) {
                continue;
            }
            PropertyDto property = new PropertyDto();
            property.setColumnName(columnName);
            property.setPropertyName(MoreStringUtils.underscoreToLowerCamel(columnName));
            JavaTypeNamingDto javaType = jdbcTypeService.jdbcType2javaType(infoSchema, astForest, persistenceDto);
            if (javaType == null) {
                log.warn("出现了预想外的类型 columnName={} dataType={} columnType={}", infoSchema.getColumnName(),
                        infoSchema.getDataType(), infoSchema.getColumnType());
                continue;
            }
            property.setJavaType(javaType);
            property.setDescription(commentService.resolveColumnComment(infoSchema));
            property.setLength(infoSchema.getCharacterMaximumLength());
            property.setNotnull("NO".equals(infoSchema.getIsNullable()));
            property.setDefaultV(infoSchema.getColumnDefault());

            persistenceDto.getProperties().add(property);
            if ("PRI".equalsIgnoreCase(infoSchema.getColumnKey())) {
                persistenceDto.getIdProperties().add(property);
            } else {
                persistenceDto.getNonIdProperties().add(property);
                if (columnName.endsWith("_id") && !config.getNotKeyColumns().contains(columnName)) {
                    persistenceDto.getKeyProperties().add(property);
                }
            }

            if (columnName.equals(deleteFlag)) {
                persistenceDto.setIsDeleteFlagExist(true);
            }

            String hash = StringUtils.upperCase(HashingUtils.hashString(persistenceDto.toString()));
            persistenceDto.setLotNo(String.format("PG%s-%s", Allison1875.SHORT_VERSION, hash));
        }

        reportWhileNoDeleleFlag(deleteFlag, persistences);

        return Lists.newArrayList(persistences.values());
    }

    private void reportWhileNoDeleleFlag(String deleteFlag, Map<String, PersistenceDto> persistences) {
        persistences.values().stream().filter(dto -> !dto.getIsDeleteFlagExist())
                .forEach(dto -> log.info("数据表[{}]没有逻辑删除标识符[{}]", dto.getTableName(), deleteFlag));
    }

    private String getDeleteFlagName() {
        String sql = config.getNotDeletedSql();
        if (StringUtils.isEmpty(sql) || StringUtils.isEmpty(config.getDeletedSql())) {
            return null;
        }
        if (!sql.contains("=")) {
            log.warn("notDeletedSql非法，不是等式，认为没有删除标识符 [{}]", sql);
            return null;
        }
        return sql.split("=")[0].trim();
    }

    private String endWith() {
        return config.getIsEntityEndWithEntity() ? "Entity" : "";
    }

}