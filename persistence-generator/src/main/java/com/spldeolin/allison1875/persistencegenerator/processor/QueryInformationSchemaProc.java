package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.io.IOUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.spldeolin.allison1875.base.util.CollectionUtils;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.zaxxer.hikari.HikariDataSource;
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
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(persistenceGeneratorConfig.getJdbcUrl());
        dataSource.setUsername(persistenceGeneratorConfig.getUserName());
        dataSource.setPassword(persistenceGeneratorConfig.getPassword());

        ResultSetHandler<List<InformationSchemaDto>> rsh = new BeanListHandler<>(InformationSchemaDto.class,
                new BasicRowProcessor(new GenerousBeanProcessor()));

        QueryRunner runner = new QueryRunner(dataSource);

        Collection<InformationSchemaDto> infoSchemas;
        try {
            InputStream is = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("information_schema.sql"),
                    "impossible unless bug.");
            String sql = IOUtils.toString(is, StandardCharsets.UTF_8);

            String part = "IS NOT NULL";
            Collection<String> tables = persistenceGeneratorConfig.getTables();
            if (CollectionUtils.isNotEmpty(tables)) {
                tables = tables.stream().map(one -> "'" + one + "'").collect(Collectors.toList());
                part = Joiner.on(',').appendTo(new StringBuilder("IN ("), tables).append(")").toString();
            }
            sql = sql.replaceFirst("\\{}", part);

            infoSchemas = runner.query(sql, rsh, persistenceGeneratorConfig.getSchema());
        } catch (Throwable e) {
            log.error("ColumnMetaProc.process", e);
            infoSchemas = Lists.newArrayList();
        } finally {
            dataSource.close();
        }
        return infoSchemas;
    }

}