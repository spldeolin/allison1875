package com.spldeolin.allison1875.persistencegenerator.service.impl;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.common.util.CollectionUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.spldeolin.allison1875.persistencegenerator.service.QueryInformationSchemaService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Deolin 2020-07-12
 */
@Singleton
@Slf4j
public class QueryInformationSchemaServiceImpl implements QueryInformationSchemaService {

    @Inject
    private PersistenceGeneratorConfig config;

    @Override
    public List<InformationSchemaDto> query() {
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

            Result<Record> records = DSL.using(conn, SQLDialect.MYSQL).fetch(sql);
            return records.into(InformationSchemaDto.class);
        } catch (Exception e) {
            log.error("QueryInformationSchemaProc.process", e);
            return Lists.newArrayList();
        }
    }

}