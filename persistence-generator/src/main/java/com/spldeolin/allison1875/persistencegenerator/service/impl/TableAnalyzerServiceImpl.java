package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLTextLiteralExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.dto.InformationSchemaDTO;
import com.spldeolin.allison1875.persistencegenerator.dto.TableAnalysisDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.JavaTypeDTO;
import com.spldeolin.allison1875.persistencegenerator.facade.dto.PropertyDTO;
import com.spldeolin.allison1875.persistencegenerator.service.TableAnalyzerService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-12
 */
@Singleton
@Slf4j
public class TableAnalyzerServiceImpl implements TableAnalyzerService {

    @Inject
    private PersistenceGeneratorConfig config;

    @Override
    public List<TableAnalysisDTO> analyzeTable() {
        final List<TableAnalysisDTO> tableAnalyses = Lists.newArrayList();

        // 数据源为jdbcUrl
        if (StringUtils.isNotEmpty(config.getJdbcUrl())) {
            log.info("analyze tables from jdbc, url={}", config.getJdbcUrl());
            // 查询information_schema.COLUMNS、information_schema.TABLES表
            List<InformationSchemaDTO> infoSchemas = this.queryInformationSchema();
            infoSchemas.stream().collect(Collectors.groupingBy(InformationSchemaDTO::getTableComment)).forEach(
                    (tableName, sameTableInfoSchemas) -> tableAnalyses.add(analyzeFromSameTable(sameTableInfoSchemas)));
        }

        // 数据源为ddl
        if (StringUtils.isNotEmpty(config.getDdl())) {
            log.info("analyze tables from ddl");
            SQLUtils.parseStatements(config.getDdl(), JdbcConstants.MYSQL).stream()
                    .filter(stmt -> stmt instanceof SQLCreateTableStatement)
                    .map(stmt -> ((SQLCreateTableStatement) stmt))
                    .forEach(createTable -> tableAnalyses.add(analyzeFromDdl(createTable)));
        }

        // 设置LotNo
        tableAnalyses.forEach(tableAnalysis -> tableAnalysis.setLotNo(
                String.format("PG%s-%s", Allison1875.SHORT_VERSION,
                        StringUtils.upperCase(HashingUtils.hashString(tableAnalysis.toString())))));

        return tableAnalyses;
    }

    public TableAnalysisDTO analyzeFromSameTable(List<InformationSchemaDTO> infoSchemas) {
        TableAnalysisDTO tableAnalysis = new TableAnalysisDTO();
        tableAnalysis.setTableName(infoSchemas.get(0).getTableName());
        String upperCamelTableName = MoreStringUtils.toUpperCamel(infoSchemas.get(0).getTableName());
        tableAnalysis.setEntityName(upperCamelTableName + endWith());
        tableAnalysis.setMapperName(upperCamelTableName + "Mapper");
        tableAnalysis.setDescrption(infoSchemas.get(0).getTableComment());
        for (InformationSchemaDTO infoSchema : infoSchemas) {
            PropertyDTO property = new PropertyDTO();
            property.setColumnName(infoSchema.getColumnName());
            property.setPropertyName(MoreStringUtils.toLowerCamel(infoSchema.getColumnName()));
            property.setJavaType(jdbcType2javaType(infoSchema.getColumnType(), infoSchema.getDataType()));
            property.setDescription(infoSchema.getColumnComment());
            property.setLength(infoSchema.getCharacterMaximumLength());
            property.setNotnull("NO".equals(infoSchema.getIsNullable()));
            property.setDefaultValue(infoSchema.getColumnDefault());
            if ("PRI".equalsIgnoreCase(infoSchema.getColumnKey())) {
                tableAnalysis.getIdProperties().add(property);
            } else {
                tableAnalysis.getNonIdProperties().add(property);
                if (infoSchema.getColumnName().endsWith("_id")) {
                    tableAnalysis.getKeyProperties().add(property);
                }
            }
            tableAnalysis.getProperties().add(property);
            if (infoSchema.getColumnName().equalsIgnoreCase(getDeleteFlagName())) {
                tableAnalysis.setIsDeleteFlagExist(true);
            }
        }
        return tableAnalysis;
    }

    public TableAnalysisDTO analyzeFromDdl(SQLCreateTableStatement createTableStmt) {
        TableAnalysisDTO tableAnalysis = new TableAnalysisDTO();
        SQLExpr tableSourceExpr = createTableStmt.getTableSource().getExpr();
        String tableName = ((SQLName) tableSourceExpr).getSimpleName().replace("`", "");
        tableAnalysis.setTableName(tableName);
        String upperCamelTableName = MoreStringUtils.toUpperCamel(tableName);
        tableAnalysis.setEntityName(upperCamelTableName + endWith());
        tableAnalysis.setMapperName(upperCamelTableName + "Mapper");
        if (createTableStmt.getComment() != null) {
            tableAnalysis.setDescrption(((SQLTextLiteralExpr) createTableStmt.getComment()).getText());
        }
        for (SQLColumnDefinition columnDef : createTableStmt.getColumnDefinitions()) {
            PropertyDTO property = new PropertyDTO();
            String columnName = columnDef.getName().getSimpleName().replace("`", "");
            property.setColumnName(columnName);
            property.setPropertyName(MoreStringUtils.toLowerCamel(columnName));
            property.setJavaType(
                    jdbcType2javaType(columnDef.getDataType().toString(), columnDef.getDataType().getName()));
            if (columnDef.getComment() != null) {
                property.setDescription(((SQLTextLiteralExpr) columnDef.getComment()).getText());
            } else {
                property.setDescription("");
            }
            property.setLength(getLength(columnDef));
            property.setNotnull(columnDef.containsNotNullConstraint());
            if (columnDef.getDefaultExpr() != null) {
                property.setDefaultValue(columnDef.getDefaultExpr().toString());
            }
            if (createTableStmt.getPrimaryKeyNames() != null && createTableStmt.getPrimaryKeyNames()
                    .contains(columnName)) {
                tableAnalysis.getIdProperties().add(property);
            } else {
                tableAnalysis.getNonIdProperties().add(property);
                if (columnName.endsWith("_id")) {
                    tableAnalysis.getKeyProperties().add(property);
                }
            }
            tableAnalysis.getProperties().add(property);
            if (columnName.equals(getDeleteFlagName())) {
                tableAnalysis.setIsDeleteFlagExist(true);
            }
        }
        return tableAnalysis;
    }

    private JavaTypeDTO jdbcType2javaType(String columnType, String dataType) {
        if (columnType == null || dataType == null) {
            throw new IllegalArgumentException("illegal argument.");
        }
        if (StringUtils.containsIgnoreCase(columnType, "tinyint(1)")) {
            return new JavaTypeDTO().setClass(Boolean.class);
        }
        if (StringUtils.equalsAnyIgnoreCase(dataType, "varchar", "char", "text", "longtext")) {
            return new JavaTypeDTO().setClass(String.class);
        }
        if ("tinyint".equalsIgnoreCase(dataType)) {
            return new JavaTypeDTO().setClass(Byte.class);
        }
        if ("int".equalsIgnoreCase(dataType)) {
            return new JavaTypeDTO().setClass(Integer.class);
        }
        if ("bigint".equalsIgnoreCase(dataType)) {
            return new JavaTypeDTO().setClass(Long.class);
        }
        if ("date".equalsIgnoreCase(dataType)) {
            return new JavaTypeDTO().setClass(Date.class);
        }
        if ("time".equalsIgnoreCase(dataType)) {
            return new JavaTypeDTO().setClass(Date.class);
        }
        if ("datetime".equalsIgnoreCase(dataType)) {
            return new JavaTypeDTO().setClass(Date.class);
        }
        if ("timestamp".equalsIgnoreCase(dataType)) {
            return new JavaTypeDTO().setClass(Date.class);
        }
        if ("decimal".equalsIgnoreCase(dataType)) {
            return new JavaTypeDTO().setClass(BigDecimal.class);
        }
        return null;
    }

    private static Long getLength(SQLColumnDefinition column) {
        List<SQLExpr> arguments = column.getDataType().getArguments();
        if (!arguments.isEmpty()) {
            SQLExpr sqlExpr = arguments.get(0);
            if (sqlExpr instanceof SQLIntegerExpr) {
                return ((SQLIntegerExpr) sqlExpr).getNumber().longValue();
            }
        }
        return null;
    }

    private List<InformationSchemaDTO> queryInformationSchema() {
        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getUserName(),
                config.getPassword())) {
            String sql = Resources.toString(Resources.getResource("information_schema.sql"), StandardCharsets.UTF_8);
            String part = "IS NOT NULL";
            List<String> tables = config.getTables();
            if (CollectionUtils.isNotEmpty(tables)) {
                tables = tables.stream().map(one -> "'" + one + "'").collect(Collectors.toList());
                part = Joiner.on(',').appendTo(new StringBuilder("IN ("), tables).append(")").toString();
            }
            sql = sql.replace("${tableNames}", part);
            sql = sql.replace("${tableSchema}", "'" + config.getSchema() + "'");

            System.setProperty("org.jooq.no-logo", "true");
            Result<Record> records = DSL.using(conn, SQLDialect.MYSQL).fetch(sql);
            return records.into(InformationSchemaDTO.class);
        } catch (Exception e) {
            log.error("QueryInformationSchemaProc.process", e);
            return Lists.newArrayList();
        }
    }

    private String getDeleteFlagName() {
        String sql = config.getNotDeletedSql();
        if (StringUtils.isEmpty(sql) || StringUtils.isEmpty(config.getDeletedSql())) {
            return null;
        }
        if (!sql.contains("=")) {
            log.warn("notDeletedSql is not a equation, ignore,  '{}'", sql);
            return null;
        }
        return sql.split("=")[0].trim();
    }

    private String endWith() {
        return config.getIsEntityEndWithEntity() ? "Entity" : "";
    }

}