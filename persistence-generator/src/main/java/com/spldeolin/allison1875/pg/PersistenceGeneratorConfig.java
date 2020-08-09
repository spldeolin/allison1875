package com.spldeolin.allison1875.pg;

import java.util.Collection;
import com.spldeolin.allison1875.base.util.YamlUtils;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Data
@Log4j2
public class PersistenceGeneratorConfig {

    @Getter
    private static final PersistenceGeneratorConfig instace = YamlUtils
            .toObject("persistence-generator-config.yml", PersistenceGeneratorConfig.class);

    private String jdbcUrl;

    private String userName;

    private String password;

    private String author;

    private String schema;

    private Collection<String> tables;

    private String mapperPackage;

    private String mapperXmlPath;

    private String entityPackage;

    private String repositoryPackage;

    private String sourceRoot;

    private Boolean isEntityUsingAlias;

    private Boolean isEntityEndWithEntity;

    private String deletedSql;

    private String notDeletedSql;

    private PersistenceGeneratorConfig() {
    }

}