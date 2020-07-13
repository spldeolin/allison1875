package com.spldeolin.allison1875.persistencegenerator;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2020-07-11
 */
@Data
@Log4j2
@Accessors(chain = true)
public class PersistenceGeneratorConfig {

    private static final PersistenceGeneratorConfig instace;

    static {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            instace = mapper.readValue(ClassLoader.getSystemResourceAsStream("persistence-generator-config.yml"),
                    PersistenceGeneratorConfig.class);
        } catch (IOException e) {
            log.error("PersistenceGeneratorConfig static block failed.", e);
            throw new ConfigLoadingException();
        }
    }

    private String jdbcUrl;

    private String userName;

    private String password;

    private String schema;

    private String author;

    private String mapperPackage;

    private String mapperXmlPath;

    private String entityPackage;

    private String repositoryPackage;

    private String sourceRoot;

    public static PersistenceGeneratorConfig getInstace() {
        return instace;
    }

}