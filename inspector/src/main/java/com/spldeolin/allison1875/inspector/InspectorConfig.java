package com.spldeolin.allison1875.inspector;

import java.nio.file.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ext.NioPathDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spldeolin.allison1875.base.exception.ConfigLoadingException;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Allison1875[inspector]的配置
 *
 * @author Deolin 2020-02-18
 */
@Data
@Log4j2
public final class InspectorConfig {

    private static final InspectorConfig instance;

    static {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimpleModule forNioPath = new SimpleModule();
        forNioPath.addDeserializer(Path.class, new NioPathDeserializer());
        mapper.registerModule(forNioPath);

        try {
            instance = mapper.readValue(ClassLoader.getSystemResourceAsStream("inspector-config.yml"),
                    InspectorConfig.class);
        } catch (Exception e) {
            log.error("InspectorConfig static block failed.", e);
            throw new ConfigLoadingException();
        }
    }

    /**
     * 分页包装类的全限定名
     */
    private String commonPageTypeQualifier;

    /**
     * 周知JSON目录的路径
     */
    private Path publicAckJsonDirectoryPath;

    /**
     * 检查结果CSV文件输出目录的路径
     */
    private Path lawlessCsvOutputDirectoryPath;

    private InspectorConfig() {
    }

    public static InspectorConfig getInstance() {
        return instance;
    }

}
