package com.spldeolin.allison1875.persistencegenerator.processor;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import jodd.db.DbOom;
import jodd.db.DbSession;
import jodd.db.ThreadDbSessionHolder;
import jodd.db.ThreadDbSessionProvider;
import jodd.db.oom.DbOomQuery;
import jodd.db.pool.CoreConnectionPool;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-12
 */
@Singleton
@Log4j2
public class QueryInformationSchemaProc {

    @Inject
    private PersistenceGeneratorConfig persistenceGeneratorConfig;

    public Collection<InformationSchemaDto> process() {
        CoreConnectionPool dscp = new CoreConnectionPool();
        dscp.setDriver("com.mysql.jdbc.Driver");
        dscp.setUrl(persistenceGeneratorConfig.getJdbcUrl());
        dscp.setUser(persistenceGeneratorConfig.getUserName());
        dscp.setPassword(persistenceGeneratorConfig.getPassword());

        ThreadDbSessionHolder.set(new DbSession(dscp));
        DbOom dbOom = DbOom.create().withConnectionProvider(dscp).withSessionProvider(new ThreadDbSessionProvider())
                .get().connect();

        Collection<InformationSchemaDto> infoSchemas;
        try {
            String sql = Resources.toString(Resources.getResource("information_schema.sql"), StandardCharsets.UTF_8);

            String part = "IS NOT NULL";
            Collection<String> tables = persistenceGeneratorConfig.getTables();
            if (CollectionUtils.isNotEmpty(tables)) {
                tables = tables.stream().map(one -> "'" + one + "'").collect(Collectors.toList());
                part = Joiner.on(',').appendTo(new StringBuilder("IN ("), tables).append(")").toString();
            }
            sql = sql.replace("${tableNames}", part);
            sql = sql.replace("${tableSchema}", "'" + persistenceGeneratorConfig.getSchema() + "'");

            DbOomQuery query = dbOom.query(sql);
            infoSchemas = query.list(resultSet -> {
                InformationSchemaDto result = new InformationSchemaDto();
                result.setTableName(resultSet.getString("TABLE_NAME"));
                result.setTableComment(resultSet.getString("TABLE_COMMENT"));
                result.setColumnName(resultSet.getString("COLUMN_NAME"));
                result.setDataType(resultSet.getString("DATA_TYPE"));
                result.setColumnType(resultSet.getString("COLUMN_TYPE"));
                result.setColumnComment(resultSet.getString("COLUMN_COMMENT"));
                result.setColumnKey(resultSet.getString("COLUMN_KEY"));
                result.setCharacterMaximumLength(resultSet.getLong("CHARACTER_MAXIMUM_LENGTH"));
                result.setIsNullable(resultSet.getString("IS_NULLABLE"));
                result.setColumnDefault(resultSet.getString("COLUMN_DEFAULT"));
                return result;
            });
        } catch (Throwable e) {
            log.error("QueryInformationSchemaProc.process", e);
            infoSchemas = Lists.newArrayList();
        }
        return infoSchemas;
    }

}