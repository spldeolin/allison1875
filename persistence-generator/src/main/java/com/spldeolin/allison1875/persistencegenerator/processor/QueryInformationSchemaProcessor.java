package com.spldeolin.allison1875.persistencegenerator.processor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.spldeolin.allison1875.persistencegenerator.PersistenceGeneratorConfig;
import com.spldeolin.allison1875.persistencegenerator.javabean.InformationSchemaDto;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-12
 */
@Log4j2
public class QueryInformationSchemaProcessor {

    @Getter
    private Collection<InformationSchemaDto> infoSchemas;

    public QueryInformationSchemaProcessor process() {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstace();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(conf.getJdbcUrl());
        dataSource.setUsername(conf.getUserName());
        dataSource.setPassword(conf.getPassword());

        ResultSetHandler<List<InformationSchemaDto>> rsh = new BeanListHandler<>(InformationSchemaDto.class,
                new BasicRowProcessor(new GenerousBeanProcessor()));

        QueryRunner runner = new QueryRunner(dataSource);

        try {
            String sql = FileUtils.readFileToString(new File(Resources.getResource("information_schema.sql").getFile()),
                    StandardCharsets.UTF_8);
            this.infoSchemas = runner.query(sql, rsh, conf.getSchema());
        } catch (Throwable e) {
            log.error("ColumnMetaProcessor.process", e);
            infoSchemas = Lists.newArrayList();
        } finally {
            dataSource.close();
        }
        return this;
    }

}