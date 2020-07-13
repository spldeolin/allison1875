package com.spldeolin.allison1875.persistencegenerator.processor;

import java.util.Collection;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
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
public class InformationSchemaQueryProcessor {

    @Getter
    private Collection<InformationSchemaDto> columns;

    public InformationSchemaQueryProcessor process() {
        PersistenceGeneratorConfig conf = PersistenceGeneratorConfig.getInstace();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(conf.getJdbcUrl());
        dataSource.setUsername(conf.getUserName());
        dataSource.setPassword(conf.getPassword());

        ResultSetHandler<List<InformationSchemaDto>> rsh = new BeanListHandler<>(InformationSchemaDto.class,
                new BasicRowProcessor(new GenerousBeanProcessor()));

        QueryRunner runner = new QueryRunner(dataSource);
        String sql = "SELECT t1.TABLE_NAME, t2.TABLE_COMMENT, t1.COLUMN_NAME, t1.IS_NULLABLE, t1.DATA_TYPE, t1"
                + ".COLUMN_TYPE, t1.COLUMN_COMMENT, t1.CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS t1, "
                + "information_schema.TABLES t2 WHERE t1.TABLE_SCHEMA = ? AND t2.TABLE_NAME = t1.TABLE_NAME GROUP BY "
                + "t1.TABLE_NAME, t1.COLUMN_NAME ORDER BY t1.TABLE_NAME, t1.ORDINAL_POSITION";
        try {
            this.columns = runner.query(sql, rsh, conf.getSchema());
        } catch (Throwable e) {
            log.error("ColumnMetaProcessor.process", e);
            columns = Lists.newArrayList();
        } finally {
            dataSource.close();
        }
        return this;
    }

}