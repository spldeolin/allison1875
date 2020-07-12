package com.spldeolin.allison1875.persistencegenerator;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spldeolin.allison1875.base.BaseConfig;
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

    private static final BaseConfig instace;

    static {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            instace = mapper.readValue(ClassLoader.getSystemResourceAsStream("base-config.yml"), BaseConfig.class);
        } catch (IOException e) {
            log.error("BaseConfig static block failed.", e);
            throw new ConfigLoadingException();
        }
    }

    private String host;

    private Integer port;

    private String userName;

    private String password;

}