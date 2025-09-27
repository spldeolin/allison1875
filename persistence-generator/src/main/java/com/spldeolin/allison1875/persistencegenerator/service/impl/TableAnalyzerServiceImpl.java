package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
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
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.Allison1875;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.common.util.HashingUtils;
import com.spldeolin.allison1875.common.util.MoreStringUtils;
import com.spldeolin.allison1875.persistencegenerator.config.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.dto.IndexDTO;
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

    static {
        System.setProperty("org.jooq.no-logo", "true");
    }

    @Inject
    private PersistenceGeneratorConfig config;

    @Override
    public List<TableAnalysisDTO> analyzeTable() {
        List<TableAnalysisDTO> tableAnalyses = Lists.newArrayList();

        // 数据源为jdbcUrl
        if (StringUtils.isNotEmpty(config.getJdbcUrl())) {
            log.info("analyze tables from jdbc, url={}", config.getJdbcUrl());
            tableAnalyses = analyzeFromInformationSchema();
        }

        // 数据源为ddl
        if (StringUtils.isEmpty(config.getJdbcUrl()) && StringUtils.isNotEmpty(config.getDdl())) {
            log.info("analyze tables from ddl");
            tableAnalyses = analyzeFromDdl();
        }

        // 设置LotNo
        tableAnalyses.forEach(tableAnalysis -> tableAnalysis.setLotNo(
                String.format("PG%s-%s", Allison1875.SHORT_VERSION,
                        StringUtils.upperCase(HashingUtils.hashString(tableAnalysis.toString())))));
        return tableAnalyses;
    }

    private List<TableAnalysisDTO> analyzeFromInformationSchema() {
        try (Connection conn = DriverManager.getConnection(config.getJdbcUrl(), config.getUserName(),
                config.getPassword())) {
            DSLContext dsl = DSL.using(conn, SQLDialect.MYSQL);
            String tableNameCond = "IS NOT NULL";
            List<String> tables = config.getTables();
            if (CollectionUtils.isNotEmpty(tables)) {
                tables = tables.stream().map(one -> "'" + one + "'").collect(Collectors.toList());
                tableNameCond = Joiner.on(',').appendTo(new StringBuilder("IN ("), tables).append(")").toString();
            }

            // 查询字段
            Result<Record> columns = queryColumns(dsl, config.getSchema(), tableNameCond);

            // 查询索引
            Result<Record> indices = queryIndices(dsl, config.getSchema(), tableNameCond);

            // 聚合结果
            return aggregateResults(columns, indices);
        } catch (Exception e) {
            log.error("QueryInformationSchemaProc.process", e);
            return Lists.newArrayList();
        }
    }

    private List<TableAnalysisDTO> aggregateResults(Result<Record> columnRecords, Result<Record> indexRecords) {
        Map<String/*tableName*/, TableAnalysisDTO> tableMap = new LinkedHashMap<>();
        Table<String/*tableName*/, String/*columnName*/, PropertyDTO> propertyMap = HashBasedTable.create();
        Table<String/*tableName*/, String/*indexName*/, IndexDTO> indexMap = HashBasedTable.create();

        for (Record record : columnRecords) {
            String tableName = record.getValue("TABLE_NAME", String.class);
            String columnName = record.getValue("COLUMN_NAME", String.class);

            TableAnalysisDTO tableAnalysis = tableMap.get(tableName);
            if (tableAnalysis == null) {
                tableAnalysis = new TableAnalysisDTO();
                tableAnalysis.setTableName(tableName);
                tableAnalysis.setEntityName(MoreStringUtils.toUpperCamel(tableName) + endWith());
                tableAnalysis.setMapperName(MoreStringUtils.toUpperCamel(tableName) + "Mapper");
                tableAnalysis.setDescrption(record.getValue("TABLE_COMMENT", String.class));
                tableAnalysis.setIsAllPropertiesNotNull(true);
                tableMap.put(tableName, tableAnalysis);
            }

            PropertyDTO property = new PropertyDTO();
            property.setColumnName(columnName);
            property.setPropertyName(MoreStringUtils.toLowerCamel(columnName));
            property.setJavaType(jdbcType2javaType(record.getValue("COLUMN_TYPE", String.class),
                    record.getValue("DATA_TYPE", String.class)));
            property.setDescription(record.getValue("COLUMN_COMMENT", String.class));
            property.setLength(record.getValue("CHARACTER_MAXIMUM_LENGTH", Long.class));
            property.setNotnull("NO".equals(record.getValue("IS_NULLABLE", String.class)));
            property.setDefaultValue(record.getValue("COLUMN_DEFAULT", String.class));
            property.setIsAutoIncrement(record.getValue("EXTRA") != null && record.getValue("EXTRA", String.class)
                    .contains("auto_increment"));
            if ("PRI".equalsIgnoreCase(record.getValue("COLUMN_KEY", String.class))) {
                tableAnalysis.getIdProperties().add(property);
            } else {
                tableAnalysis.getNonIdProperties().add(property);
            }
            tableAnalysis.getProperties().add(property);
            propertyMap.put(tableName, columnName, property);
            if (!property.getNotnull()) {
                tableAnalysis.setIsAllPropertiesNotNull(false);
            }
        }

        for (Record record : indexRecords) {
            String tableName = record.getValue("TABLE_NAME", String.class);
            String indexName = record.getValue("INDEX_NAME", String.class);

            IndexDTO index = indexMap.get(tableName, indexName);
            if (index == null) {
                index = new IndexDTO();
                index.setIndexName(indexName);
                index.setIsUnique(record.getValue("NON_UNIQUE", Integer.class) == 0);
                tableMap.get(tableName).getIndices().add(index);
                indexMap.put(tableName, indexName, index);
            }

            PropertyDTO property = propertyMap.get(tableName, record.get("COLUMN_NAME", String.class));
            index.getProperties().add(property);
        }
        return Lists.newArrayList(tableMap.values());
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

    private String endWith() {
        return config.getIsEntityEndWithEntity() ? "Entity" : "";
    }

    private Result<Record> queryColumns(DSLContext dsl, String tableSchema, String tableNameCond) {
        String sql = "SELECT " + "t1.TABLE_NAME, " + "t2.TABLE_COMMENT, " + "t1.COLUMN_NAME, " + "t1.DATA_TYPE, "
                + "t1.COLUMN_TYPE, " + "t1.COLUMN_COMMENT, " + "t1.COLUMN_KEY, " + "t1.CHARACTER_MAXIMUM_LENGTH, "
                + "t1.IS_NULLABLE, " + "t1.COLUMN_DEFAULT, " + "t1.EXTRA, " + "t1.ORDINAL_POSITION "
                + "FROM information_schema.COLUMNS t1 " + "JOIN information_schema.TABLES t2 "
                + "  ON t1.TABLE_SCHEMA = t2.TABLE_SCHEMA " + " AND t1.TABLE_NAME = t2.TABLE_NAME "
                + "WHERE t1.TABLE_SCHEMA = ? " + "  AND t1.TABLE_NAME " + tableNameCond + " "
                + "ORDER BY t1.TABLE_NAME, t1.ORDINAL_POSITION";
        return dsl.fetch(sql, tableSchema);
    }

    private Result<Record> queryIndices(DSLContext dsl, String tableSchema, String tableNameCond) {
        String sql = "SELECT " + "s.TABLE_NAME, " + "s.COLUMN_NAME, " + "s.INDEX_NAME, " + "s.NON_UNIQUE, "
                + "s.INDEX_TYPE, " + "s.SEQ_IN_INDEX " + "FROM information_schema.STATISTICS s "
                + "WHERE s.TABLE_SCHEMA = ? " + "  AND s.TABLE_NAME " + tableNameCond
                + " AND s.INDEX_NAME != 'PRIMARY' " + "ORDER BY s.TABLE_NAME, s.INDEX_NAME, s.SEQ_IN_INDEX";
        return dsl.fetch(sql, tableSchema);
    }

    private List<TableAnalysisDTO> analyzeFromDdl() {
        List<TableAnalysisDTO> tableAnalyses = Lists.newArrayList();
        SQLUtils.parseStatements(config.getDdl(), JdbcConstants.MYSQL).stream()
                .filter(stmt -> stmt instanceof SQLCreateTableStatement).map(stmt -> ((SQLCreateTableStatement) stmt))
                .forEach(createTable -> {
                    TableAnalysisDTO tableAnalysis = new TableAnalysisDTO();
                    SQLExpr tableSourceExpr = createTable.getTableSource().getExpr();
                    String tableName = ((SQLName) tableSourceExpr).getSimpleName().replace("`", "");
                    tableAnalysis.setTableName(tableName);
                    String upperCamelTableName = MoreStringUtils.toUpperCamel(tableName);
                    tableAnalysis.setEntityName(upperCamelTableName + endWith());
                    tableAnalysis.setMapperName(upperCamelTableName + "Mapper");
                    if (createTable.getComment() != null) {
                        tableAnalysis.setDescrption(((SQLTextLiteralExpr) createTable.getComment()).getText());
                    }
                    tableAnalysis.setIsAllPropertiesNotNull(true);
                    for (SQLColumnDefinition columnDef : createTable.getColumnDefinitions()) {
                        PropertyDTO property = new PropertyDTO();
                        String columnName = columnDef.getName().getSimpleName().replace("`", "");
                        property.setColumnName(columnName);
                        property.setPropertyName(MoreStringUtils.toLowerCamel(columnName));
                        property.setJavaType(jdbcType2javaType(columnDef.getDataType().toString(),
                                columnDef.getDataType().getName()));
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
                        property.setIsAutoIncrement(columnDef.isAutoIncrement());
                        if ((createTable.getPrimaryKeyNames() != null && createTable.getPrimaryKeyNames()
                                .contains(columnName)) || columnDef.isPrimaryKey()) {
                            tableAnalysis.getIdProperties().add(property);
                        } else {
                            tableAnalysis.getNonIdProperties().add(property);
                        }
                        tableAnalysis.getProperties().add(property);
                        if (columnName.equals(getDeleteFlagName())) {
                            tableAnalysis.setIsDeleteFlagExist(true);
                        }
                        if (!property.getNotnull()) {
                            tableAnalysis.setIsAllPropertiesNotNull(false);
                        }
                    }
                    for (PropertyDTO prop : tableAnalysis.getProperties()) {
                        if (prop.getJavaType() == null) {
                            log.warn("unsupport jbdcType, column={}.{}", tableAnalysis.getTableName(),
                                    prop.getColumnName());
                            return;
                        }
                    }
                    tableAnalyses.add(tableAnalysis);
                });
        return tableAnalyses;
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

}