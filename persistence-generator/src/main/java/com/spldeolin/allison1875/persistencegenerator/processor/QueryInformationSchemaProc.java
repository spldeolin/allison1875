package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.io.IOUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-12
 */
@Log4j2
public class QueryInformationSchemaProc {

    @Getter
    private Collection<InformationSchemaDto> infoSchemas;

    public QueryInformationSchemaProc process() {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstance();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(conf.getJdbcUrl());
        dataSource.setUsername(conf.getUserName());
        dataSource.setPassword(conf.getPassword());

        ResultSetHandler<List<InformationSchemaDto>> rsh = new BeanListHandler<>(InformationSchemaDto.class,
                new BasicRowProcessor(new GenerousBeanProcessor()));

        QueryRunner runner = new QueryRunner(dataSource);

        try {
            InputStream is = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("information_schema.sql"),
                    "impossible unless bug.");
            String sql = IOUtils.toString(is, StandardCharsets.UTF_8);

            String part = "IS NOT NULL";
            Collection<String> tables = conf.getTables();
            if (CollectionUtils.isNotEmpty(tables)) {
                tables = tables.stream().map(one -> "'" + one + "'").collect(Collectors.toList());
                part = Joiner.on(',').appendTo(new StringBuilder("IN ("), tables).append(")").toString();
            }
            sql = sql.replaceFirst("\\{}", part);

            this.infoSchemas = runner.query(sql, rsh, conf.getSchema());
        } catch (Throwable e) {
            log.error("ColumnMetaProc.process", e);
            infoSchemas = Lists.newArrayList();
        } finally {
            dataSource.close();
        }
        return this;
    }

}